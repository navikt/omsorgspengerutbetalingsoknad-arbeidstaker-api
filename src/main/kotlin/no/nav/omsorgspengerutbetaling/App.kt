package no.nav.omsorgspengerutbetaling

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.metrics.micrometer.*
import io.ktor.routing.*
import io.prometheus.client.hotspot.DefaultExports
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.helse.dusseldorf.ktor.auth.allIssuers
import no.nav.helse.dusseldorf.ktor.auth.clients
import no.nav.helse.dusseldorf.ktor.auth.multipleJwtIssuers
import no.nav.helse.dusseldorf.ktor.client.HttpRequestHealthCheck
import no.nav.helse.dusseldorf.ktor.client.HttpRequestHealthConfig
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.core.*
import no.nav.helse.dusseldorf.ktor.health.HealthReporter
import no.nav.helse.dusseldorf.ktor.health.HealthRoute
import no.nav.helse.dusseldorf.ktor.health.HealthService
import no.nav.helse.dusseldorf.ktor.jackson.JacksonStatusPages
import no.nav.helse.dusseldorf.ktor.jackson.dusseldorfConfigured
import no.nav.helse.dusseldorf.ktor.metrics.MetricsRoute
import no.nav.helse.dusseldorf.ktor.metrics.init
import no.nav.omsorgspengerutbetaling.arbeidsgiver.ArbeidsgivereGateway
import no.nav.omsorgspengerutbetaling.arbeidsgiver.ArbeidsgivereService
import no.nav.omsorgspengerutbetaling.arbeidsgiver.arbeidsgiverApis
import no.nav.omsorgspengerutbetaling.general.auth.IdTokenStatusPages
import no.nav.omsorgspengerutbetaling.general.systemauth.AccessTokenClientResolver
import no.nav.omsorgspengerutbetaling.kafka.KafkaProducer
import no.nav.omsorgspengerutbetaling.mellomlagring.MellomlagringService
import no.nav.omsorgspengerutbetaling.mellomlagring.mellomlagringApis
import no.nav.omsorgspengerutbetaling.redis.RedisConfig
import no.nav.omsorgspengerutbetaling.redis.RedisStore
import no.nav.omsorgspengerutbetaling.soker.SøkerGateway
import no.nav.omsorgspengerutbetaling.soker.SøkerService
import no.nav.omsorgspengerutbetaling.soker.søkerApis
import no.nav.omsorgspengerutbetaling.soknad.SøknadService
import no.nav.omsorgspengerutbetaling.soknad.arbeidstakerutbetalingsøknadApis
import no.nav.omsorgspengerutbetaling.vedlegg.K9MellomlagringGateway
import no.nav.omsorgspengerutbetaling.vedlegg.VedleggService
import no.nav.omsorgspengerutbetaling.vedlegg.vedleggApis
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

private val logger: Logger = LoggerFactory.getLogger("nav.omsorgpengerutbetalingsoknadArbeidstakerApi")

fun Application.omsorgpengerutbetalingsoknadArbeidstakerApi() {
    val appId = environment.config.id()
    logProxyProperties()
    DefaultExports.initialize()

    System.setProperty("dusseldorf.ktor.serializeProblemDetailsWithContentNegotiation", "true")

    val configuration = Configuration(environment.config)
    val accessTokenClientResolver = AccessTokenClientResolver(environment.config.clients())

    install(ContentNegotiation) {
        jackson {
            omsorgspengerKonfiguert()
        }
    }

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        allowNonSimpleContentTypes = true
        allowCredentials = true
        log.info("Configuring CORS")
        configuration.getWhitelistedCorsAddreses().forEach {
            log.info("Adding host {} with scheme {}", it.host, it.scheme)
            host(host = it.authority, schemes = listOf(it.scheme))
        }
    }

    val idTokenProvider = IdTokenProvider(cookieName = configuration.getCookieName())
    val issuers = configuration.issuers()

    install(Authentication) {
        multipleJwtIssuers(
            issuers = issuers,
            extractHttpAuthHeader = { call ->
                idTokenProvider.getIdToken(call)
                    .somHttpAuthHeader()
            }
        )
    }

    install(StatusPages) {
        DefaultStatusPages()
        JacksonStatusPages()
        IdTokenStatusPages()
    }

    install(Routing) {

        val k9MellomlagringGateway = K9MellomlagringGateway(
            baseUrl = configuration.getK9MellomlagringUrl(),
            accessTokenClient = accessTokenClientResolver.accessTokenClient(),
            k9MellomlagringScope = configuration.getK9MellomlagringScopes()
        )

        val vedleggService = VedleggService(k9MellomlagringGateway = k9MellomlagringGateway)

        val sokerGateway = SøkerGateway(baseUrl = configuration.getK9OppslagUrl())

        val arbeidsgivereGateway = ArbeidsgivereGateway(baseUrl = configuration.getK9OppslagUrl())

        val søkerService = SøkerService(søkerGateway = sokerGateway)

        val kafkaProducer = KafkaProducer(kafkaConfig = configuration.getKafkaConfig())

        environment.monitor.subscribe(ApplicationStopping) {
            logger.info("Stopper Kafka Producer.")
            kafkaProducer.stop()
            logger.info("Kafka Producer Stoppet.")
        }

        authenticate(*issuers.allIssuers()) {

            søkerApis(
                søkerService = søkerService,
                idTokenProvider = idTokenProvider
            )

            arbeidsgiverApis(
                arbeidsgivereService = ArbeidsgivereService(
                    arbeidsgivereGateway = arbeidsgivereGateway
                ),
                idTokenProvider = idTokenProvider
            )

            mellomlagringApis(
                mellomlagringService = MellomlagringService(
                    redisStore = RedisStore(
                        redisClient = RedisConfig.redisClient(
                            redisHost = configuration.getRedisHost(),
                            redisPort = configuration.getRedisPort()
                        )
                    ),
                    passphrase = configuration.getStoragePassphrase()
                ),
                idTokenProvider = idTokenProvider
            )

            vedleggApis(
                vedleggService = vedleggService,
                idTokenProvider = idTokenProvider
            )

            arbeidstakerutbetalingsøknadApis(
                idTokenProvider = idTokenProvider,
                søkerService = søkerService,
                søknadService = SøknadService(
                    søkerService = søkerService,
                    vedleggService = vedleggService,
                    kafkaProducer = kafkaProducer,
                    k9MellomLagringIngress = configuration.getK9MellomlagringIngress()
                )
            )
        }

        val healthService = HealthService(
            healthChecks = setOf(
                kafkaProducer,
                HttpRequestHealthCheck(
                    mapOf(
                        Url.buildURL(
                            baseUrl = configuration.getK9MellomlagringUrl(),
                            pathParts = listOf("health")
                        ) to HttpRequestHealthConfig(expectedStatus = HttpStatusCode.OK)
                    )
                )
            )
        )

        HealthReporter(
            app = appId,
            healthService = healthService,
            frequency = Duration.ofMinutes(1)
        )

        DefaultProbeRoutes()
        MetricsRoute()
        HealthRoute(
            healthService = healthService
        )
    }

    install(MicrometerMetrics) {
        init(appId)
    }

    intercept(ApplicationCallPipeline.Monitoring) {
        call.request.log()
    }

    install(CallId) {
        generated()
    }

    install(CallLogging) {
        correlationIdAndRequestIdInMdc()
        logRequests()
        mdc("id_token_jti") { call ->
            try {
                idTokenProvider.getIdToken(call).getId()
            } catch (cause: Throwable) {
                null
            }
        }
    }
}

internal fun ObjectMapper.omsorgspengerKonfiguert() = dusseldorfConfigured().apply {}

internal fun k9DokumentKonfigurert() = jacksonObjectMapper().dusseldorfConfigured().apply {
    propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
}

internal fun k9SelvbetjeningOppslagKonfigurert() = jacksonObjectMapper().dusseldorfConfigured().apply {
    propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
}
