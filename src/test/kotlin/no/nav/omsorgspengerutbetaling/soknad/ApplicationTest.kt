package no.nav.omsorgspengerutbetaling.soknad

import com.github.fppt.jedismock.RedisServer
import com.github.tomakehurst.wiremock.http.Cookie
import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.helse.dusseldorf.ktor.core.fromResources
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.omsorgspengerutbetaling.TestConfiguration
import no.nav.omsorgspengerutbetaling.TestUtils.Companion.getAuthCookie
import no.nav.omsorgspengerutbetaling.felles.Bekreftelser
import no.nav.omsorgspengerutbetaling.felles.FraværÅrsak
import no.nav.omsorgspengerutbetaling.felles.JaNei
import no.nav.omsorgspengerutbetaling.felles.Utbetalingsperiode
import no.nav.omsorgspengerutbetaling.handleRequestUploadImage
import no.nav.omsorgspengerutbetaling.jpegUrl
import no.nav.omsorgspengerutbetaling.mellomlagring.started
import no.nav.omsorgspengerutbetaling.pdUrl
import no.nav.omsorgspengerutbetaling.soknad.SøknadUtils.defaultSøknad
import no.nav.omsorgspengerutbetaling.wiremock.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.skyscreamer.jsonassert.JSONAssert
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.time.Duration
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

private const val fnr = "290990123456"
private const val ikkeMyndigFnr = "12125012345"

// Se https://github.com/navikt/dusseldorf-ktor#f%C3%B8dselsnummer
private val gyldigFodselsnummerA = "02119970078"
private val ikkeMyndigDato = "2050-12-12"

class SøknadApplicationTest {

    private companion object {

        private val logger: Logger = LoggerFactory.getLogger(SøknadApplicationTest::class.java)

        val wireMockServer = WireMockBuilder()
            .withAzureSupport()
            .withNaisStsSupport()
            .withLoginServiceSupport()
            .omsorgspengesoknadApiConfig()
            .build()
            .stubK9DokumentHealth()
            .stubOmsorgspengerutbetalingsoknadMottakHealth()
            .stubOppslagHealth()
            .stubLeggSoknadTilProsessering("/v1/soknad")
            .stubK9OppslagSoker()
            .stubK9Mellomlagring()

        val redisServer: RedisServer = RedisServer
            .newRedisServer()
            .started()

        fun getConfig(): ApplicationConfig {

            val fileConfig = ConfigFactory.load()
            val testConfig = ConfigFactory.parseMap(
                TestConfiguration.asMap(
                    wireMockServer = wireMockServer,
                    redisServer = redisServer
                )
            )
            val mergedConfig = testConfig.withFallback(fileConfig)

            return HoconApplicationConfig(mergedConfig)
        }


        val engine = TestApplicationEngine(createTestEnvironment {
            config = getConfig()
        })


        @BeforeAll
        @JvmStatic
        fun buildUp() {
            engine.start(wait = true)
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            logger.info("Tearing down")
            wireMockServer.stop()
            redisServer.stop()
            logger.info("Tear down complete")
        }
    }

    @Test
    fun `Sende soknad`() {
        with(engine) {
            val cookie = getAuthCookie(gyldigFodselsnummerA)
            val jpegUrl = "vedlegg/iPhone_6.jpg".fromResources().readBytes()

            // LASTER OPP VEDLEGG
            val vedleggId = handleRequestUploadImage(
                cookie = cookie,
                vedlegg = jpegUrl
            ).substringAfterLast("/")

            requestAndAssert(
                httpMethod = HttpMethod.Post,
                path = "/soknad",
                expectedResponse = null,
                expectedCode = HttpStatusCode.Accepted,
                cookie = cookie,
                requestEntity = defaultSøknad.copy(
                    vedlegg =
                    listOf(URL("${wireMockServer.getK9MellomlagringUrl()}/$vedleggId"))
                ).somJson()
            )
        }
    }

    @Test
    fun `Sende soknad som raw json`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val jpegUrl = engine.jpegUrl(cookie)
        val pdfUrl = engine.pdUrl(cookie)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
            expectedResponse = null,
            expectedCode = HttpStatusCode.Accepted,
            cookie = cookie,
            requestEntity =
            //language=json
            """
                {
                  "språk": "nb",
                  "bosteder": [
                    {
                      "fraOgMed": "2019-12-12",
                      "tilOgMed": "2019-12-22",
                      "landkode": "GB",
                      "landnavn": "Great Britain",
                      "erEØSLand": true
                    }
                  ],
                  "opphold": [
                    {
                      "fraOgMed": "2019-12-12",
                      "tilOgMed": "2019-12-22",
                      "landkode": "GB",
                      "landnavn": "Great Britain",
                      "erEØSLand": true
                    }
                  ],
                  "arbeidsgivere": [
                      {
                        "navn": "Arbeidsgiver 1",
                        "organisasjonsnummer": "917755736",
                        "harHattFraværHosArbeidsgiver": true,
                        "arbeidsgiverHarUtbetaltLønn": false,
                        "utbetalingsårsak" : "ARBEIDSGIVER_KONKURS",
                        "perioder": [
                          {
                            "fraOgMed": "2020-01-01",
                            "tilOgMed": "2020-01-11",
                            "årsak": "STENGT_SKOLE_ELLER_BARNEHAGE"
                          }
                        ]
                      },
                      {
                        "navn": "Arbeidsgiver 2",
                        "organisasjonsnummer": "917755736",
                        "harHattFraværHosArbeidsgiver": true,
                        "arbeidsgiverHarUtbetaltLønn": false,
                        "utbetalingsårsak" : "ARBEIDSGIVER_KONKURS",
                        "perioder": [
                          {
                            "fraOgMed": "2020-01-21",
                            "tilOgMed": "2020-01-21",
                            "årsak": "STENGT_SKOLE_ELLER_BARNEHAGE"
                          }
                        ]
                      },
                      {
                        "navn": "Arbeidsgiver 3",
                        "organisasjonsnummer": "917755736",
                        "harHattFraværHosArbeidsgiver": true,
                        "arbeidsgiverHarUtbetaltLønn": false,
                        "utbetalingsårsak" : "ARBEIDSGIVER_KONKURS",
                        "perioder": [
                          {
                            "fraOgMed": "2020-01-31",
                            "tilOgMed": "2020-02-05",
                            "årsak": "STENGT_SKOLE_ELLER_BARNEHAGE"
                          }
                        ]
                      },
                      {
                        "navn": "Navn navnesen",
                        "organisasjonsnummer": "917755736",
                        "harHattFraværHosArbeidsgiver": true,
                        "arbeidsgiverHarUtbetaltLønn": false,
                        "utbetalingsårsak" : "ARBEIDSGIVER_KONKURS",
                        "perioder": [
                          {
                            "fraOgMed": "2020-02-01",
                            "tilOgMed": "2020-02-06",
                            "årsak": "STENGT_SKOLE_ELLER_BARNEHAGE"
                          }
                        ]
                      }
                    ],
                  "bekreftelser": {
                    "harBekreftetOpplysninger": true,
                    "harForståttRettigheterOgPlikter": true
                  },
                  "vedlegg": [
                    "$jpegUrl",
                    "$pdfUrl"
                  ]
                }
            """.trimIndent()
        )
    }

    @Test
    fun `Sende søknad ikke innlogget`() {
        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
            expectedCode = HttpStatusCode.Unauthorized,
            expectedResponse = null,
            requestEntity = defaultSøknad.somJson(),
            leggTilCookie = false
        )
    }

    @Test
    fun `Sende soknad ikke myndig`() {
        val cookie = getAuthCookie(ikkeMyndigFnr)
        val jpegUrl = engine.jpegUrl(cookie)
        val pdfUrl = engine.pdUrl(cookie)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
            expectedResponse = """
                {
                    "type": "/problem-details/unauthorized",
                    "title": "unauthorized",
                    "status": 403,
                    "detail": "Søkeren er ikke myndig og kan ikke sende inn søknaden.",
                    "instance": "about:blank"
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.Forbidden,
            cookie = cookie,
            requestEntity = defaultSøknad.copy(
                vedlegg = listOf(
                    URL(jpegUrl), URL(pdfUrl)
                )
            )
                .somJson()
        )
    }

    @Test
    fun `Sende soknad med ugylidge parametre gir feil`() {

        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val jpegUrl = engine.jpegUrl(cookie)
        val pdfUrl = engine.pdUrl(cookie)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
            expectedCode = HttpStatusCode.BadRequest,
            requestEntity = defaultSøknad.copy(
                arbeidsgivere = listOf(
                    defaultSøknad.arbeidsgivere[0].copy(
                        perioder = listOf(),
                        navn = " ",
                        organisasjonsnummer = " "
                    )
                ),
                bekreftelser = Bekreftelser(
                    harForståttRettigheterOgPlikter = JaNei.Nei,
                    harBekreftetOpplysninger = JaNei.Nei
                ),
                vedlegg = listOf(
                    URL(jpegUrl), URL(pdfUrl)
                )
            ).somJson(),
            expectedResponse = //language=json
            """
            {
              "type": "/problem-details/invalid-request-parameters",
              "title": "invalid-request-parameters",
              "status": 400,
              "detail": "Requesten inneholder ugyldige paramtere.",
              "instance": "about:blank",
              "invalid_parameters": [
                {
                  "type": "entity",
                  "name": "utbetalingsperioder",
                  "reason": "Må settes minst en utbetalingsperiode.",
                  "invalid_value": []
                },
                {
                  "type": "entity",
                  "name": "navn",
                  "reason": "ArbeidsgiverDetaljer må ha navn satt.",
                  "invalid_value": " "
                },
                {
                  "type": "entity",
                  "name": "organisasjonsnummer",
                  "reason": "organisasjonsnummer må være satt.",
                  "invalid_value": " "
                },
                {
                  "type": "entity",
                  "name": "bekreftlser.harBekreftetOpplysninger",
                  "reason": "Må besvars Ja.",
                  "invalid_value": false
                },
                {
                  "type": "entity",
                  "name": "bekreftelser.harForståttRettigheterOgPlikter",
                  "reason": "Må besvars Ja.",
                  "invalid_value": false
                },
                {
                  "type": "entity",
                  "name": "fraværsperioder",
                  "reason": "Minst 1 fraværsperiode må oppgis",
                  "invalid_value": "k9-format feilkode: påkrevd"
                }
              ]
            }
            """.trimIndent()
        )
    }

    @Test
    fun `Sende ugyldig søknad hvor antallTimerPlanlagt ikke er satt mens antallTimerBorte er satt`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
            expectedResponse = """
                {
                  "type": "/problem-details/invalid-request-parameters",
                  "title": "invalid-request-parameters",
                  "status": 400,
                  "detail": "Requesten inneholder ugyldige paramtere.",
                  "instance": "about:blank",
                  "invalid_parameters": [
                    {
                      "type": "entity",
                      "name": "utbetalingsperioder[Utbetalingsperiode(fraOgMed=2020-01-01, tilOgMed=2020-01-10, antallTimerBorte=null, antallTimerPlanlagt=PT5H, årsak=ORDINÆRT_FRAVÆR)]",
                      "reason": "Dersom antallTimerPlanlagt er satt så kan ikke antallTimerBorte være tom",
                      "invalid_value": "antallTimerBorte = null, antallTimerPlanlagt=PT5H"
                    }
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = defaultSøknad.copy(
                arbeidsgivere = listOf(
                    defaultSøknad.arbeidsgivere[0].copy(
                        perioder = listOf(
                            Utbetalingsperiode(
                                fraOgMed = LocalDate.parse("2020-01-01"),
                                tilOgMed = LocalDate.parse("2020-01-10"),
                                antallTimerPlanlagt = Duration.ofHours(5),
                                årsak = FraværÅrsak.ORDINÆRT_FRAVÆR
                            )
                        )
                    )
                ),
                vedlegg = listOf()
            ).somJson()
        )
    }

    @Test
    fun `Sende ugyldig søknad hvor antallTimerBorte er større enn antallTimerPlanlagt`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
            expectedResponse = """
                {
                  "type": "/problem-details/invalid-request-parameters",
                  "title": "invalid-request-parameters",
                  "status": 400,
                  "detail": "Requesten inneholder ugyldige paramtere.",
                  "instance": "about:blank",
                  "invalid_parameters": [
                    {
                      "type": "entity",
                      "name": "utbetalingsperioder[Utbetalingsperiode(fraOgMed=2020-01-01, tilOgMed=2020-01-10, antallTimerBorte=PT6H, antallTimerPlanlagt=PT5H, årsak=ORDINÆRT_FRAVÆR)]",
                      "reason": "Antall timer borte kan ikke være større enn antall timer planlagt jobbe",
                      "invalid_value": "antallTimerBorte = PT6H, antallTimerPlanlagt=PT5H"
                    }
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = defaultSøknad.copy(
                arbeidsgivere = listOf(
                    defaultSøknad.arbeidsgivere[0].copy(
                        perioder = listOf(
                            Utbetalingsperiode(
                                fraOgMed = LocalDate.parse("2020-01-01"),
                                tilOgMed = LocalDate.parse("2020-01-10"),
                                antallTimerPlanlagt = Duration.ofHours(5),
                                antallTimerBorte = Duration.ofHours(6),
                                årsak = FraværÅrsak.ORDINÆRT_FRAVÆR
                            )
                        )
                    )
                ),
                vedlegg = listOf()
            ).somJson()
        )
    }

    private fun requestAndAssert(
        httpMethod: HttpMethod,
        path: String,
        requestEntity: String? = null,
        expectedResponse: String?,
        expectedCode: HttpStatusCode,
        leggTilCookie: Boolean = true,
        cookie: Cookie = getAuthCookie(fnr)
    ): String? {
        val respons: String?
        with(engine) {
            handleRequest(httpMethod, path) {
                if (leggTilCookie) addHeader(HttpHeaders.Cookie, cookie.toString())
                logger.info("Request Entity = $requestEntity")
                addHeader(HttpHeaders.Accept, "application/json")
                if (requestEntity != null) addHeader(HttpHeaders.ContentType, "application/json")
                if (requestEntity != null) setBody(requestEntity)
            }.apply {
                logger.info("Response Entity = ${response.content}")
                logger.info("Expected Entity = $expectedResponse")
                respons = response.content
                assertEquals(expectedCode, response.status())
                if (expectedResponse != null) {
                    JSONAssert.assertEquals(expectedResponse, response.content!!, true)
                } else {
                    assertEquals(expectedResponse, response.content)
                }
            }
        }
        return respons
    }
}
