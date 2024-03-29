package no.nav.omsorgspengerutbetaling.arbeidsgiver

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import io.ktor.http.*
import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.core.Retry
import no.nav.helse.dusseldorf.ktor.metrics.Operation
import no.nav.omsorgspengerutbetaling.general.CallId
import no.nav.omsorgspengerutbetaling.general.oppslag.K9OppslagGateway
import no.nav.omsorgspengerutbetaling.k9SelvbetjeningOppslagKonfigurert
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ArbeidsgivereGateway(
    baseUrl: URI
) : K9OppslagGateway(baseUrl) {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger("nav.ArbeidsgivereGateway")
        private const val HENTE_ARBEIDSGIVERE_OPERATION = "hente-arbeidsgivere"
        private val objectMapper = k9SelvbetjeningOppslagKonfigurert()
        private val attributer = Pair("a", listOf( "arbeidsgivere[].organisasjoner[].organisasjonsnummer",
            "arbeidsgivere[].organisasjoner[].navn"))
    }

    internal suspend fun hentArbeidsgivere(
        idToken: IdToken,
        callId: CallId,
        fraOgMed: LocalDate,
        tilOgMed: LocalDate
    ) : Arbeidsgivere {
        val arbeidsgivereUrl = Url.buildURL(
            baseUrl = baseUrl,
            pathParts = listOf("meg"),
            queryParameters = mapOf(
                attributer,
                Pair("fom", listOf(DateTimeFormatter.ISO_LOCAL_DATE.format(fraOgMed))),
                Pair("tom", listOf(DateTimeFormatter.ISO_LOCAL_DATE.format(tilOgMed)))
            )
        ).toString()

        val httpRequest = generateHttpRequest(idToken, arbeidsgivereUrl, callId)

        val arbeidsgivereOppslagRespons = Retry.retry(
            operation = HENTE_ARBEIDSGIVERE_OPERATION,
            initialDelay = Duration.ofMillis(200),
            factor = 2.0,
            logger = logger
        ) {
            val (request, _, result) = Operation.monitored(
                app = "omsorgspengerutbetalingsoknad-arbeidstaker-api",
                operation = HENTE_ARBEIDSGIVERE_OPERATION,
                resultResolver = { 200 == it.second.statusCode }
            ) { httpRequest.awaitStringResponseResult() }

            result.fold(
                { success -> objectMapper.readValue<ArbeidsgivereOppslagRespons>(success)},
                { error ->
                    logger.error("Error response = '${error.response.body().asString("text/plain")}' fra '${request.url}'")
                    logger.error(error.toString())
                    throw IllegalStateException("Feil ved henting av arbeidsgiver.")
                }
            )
        }
        return arbeidsgivereOppslagRespons.arbeidsgivere
    }
}
