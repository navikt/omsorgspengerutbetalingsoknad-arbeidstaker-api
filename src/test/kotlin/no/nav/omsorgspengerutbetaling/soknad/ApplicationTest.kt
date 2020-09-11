package no.nav.omsorgspengerutbetaling.soknad

import com.github.fppt.jedismock.RedisServer
import com.github.tomakehurst.wiremock.http.Cookie
import com.typesafe.config.ConfigFactory
import io.ktor.config.ApplicationConfig
import io.ktor.config.HoconApplicationConfig
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.createTestEnvironment
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.util.KtorExperimentalAPI
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder
import no.nav.omsorgspengerutbetaling.TestConfiguration
import no.nav.omsorgspengerutbetaling.soknad.ArbeidstakerutbetalingSøknadUtils.defaultSøknad
import no.nav.omsorgspengerutbetaling.felles.Bekreftelser
import no.nav.omsorgspengerutbetaling.felles.FosterBarn
import no.nav.omsorgspengerutbetaling.felles.JaNei
import no.nav.omsorgspengerutbetaling.felles.Utbetalingsperiode
import no.nav.omsorgspengerutbetaling.getAuthCookie
import no.nav.omsorgspengerutbetaling.jpegUrl
import no.nav.omsorgspengerutbetaling.mellomlagring.started
import no.nav.omsorgspengerutbetaling.pdUrl
import no.nav.omsorgspengerutbetaling.wiremock.*
import org.junit.AfterClass
import org.junit.BeforeClass
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

@KtorExperimentalAPI
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
            .stubK9Dokument()

        val redisServer: RedisServer = RedisServer
            .newRedisServer(6379)
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


        @BeforeClass
        @JvmStatic
        fun buildUp() {
            engine.start(wait = true)
        }

        @AfterClass
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
        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val jpegUrl = engine.jpegUrl(cookie)
        val pdfUrl = engine.pdUrl(cookie)

        requestAndAssert(
            httpMethod = HttpMethod.Post,
            path = "/soknad",
            expectedResponse = null,
            expectedCode = HttpStatusCode.Accepted,
            cookie = cookie,
            requestEntity = defaultSøknad.copy(
                vedlegg = listOf(
                    URL(pdfUrl), URL(jpegUrl)
                )
            ).somJson()
        )
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
                        "ansettelseslengde": {
                          "merEnn4Uker": true
                        },
                        "perioder": [
                          {
                            "fraOgMed": "2020-01-01",
                            "tilOgMed": "2020-01-11",
                            "lengde": null
                          }
                        ]
                      },
                      {
                        "navn": "Arbeidsgiver 2",
                        "organisasjonsnummer": "917755736",
                        "harHattFraværHosArbeidsgiver": true,
                        "arbeidsgiverHarUtbetaltLønn": false,
                        "ansettelseslengde": {
                          "merEnn4Uker": false,
                          "begrunnelse": "ANNET_ARBEIDSFORHOLD"
                        },
                        "perioder": [
                          {
                            "fraOgMed": "2020-01-21",
                            "tilOgMed": "2020-01-21",
                            "lengde": "PT5H30M"
                          }
                        ]
                      },
                      {
                        "navn": "Arbeidsgiver 3",
                        "organisasjonsnummer": "917755736",
                        "harHattFraværHosArbeidsgiver": true,
                        "arbeidsgiverHarUtbetaltLønn": false,
                        "ansettelseslengde": {
                          "merEnn4Uker": false,
                          "begrunnelse": "MILITÆRTJENESTE"
                        },
                        "perioder": [
                          {
                            "fraOgMed": "2020-01-31",
                            "tilOgMed": "2020-02-05",
                            "lengde": null
                          }
                        ]
                      },
                      {
                        "navn": null,
                        "organisasjonsnummer": "917755736",
                        "harHattFraværHosArbeidsgiver": true,
                        "arbeidsgiverHarUtbetaltLønn": false,
                        "ansettelseslengde": {
                          "merEnn4Uker": false,
                          "begrunnelse": "ANDRE_YTELSER"
                        },
                        "perioder": [
                          {
                            "fraOgMed": "2020-02-01",
                            "tilOgMed": "2020-02-06",
                            "lengde": null
                          }
                        ]
                      },
                      {
                        "navn": "Ikke registrert arbeidsgiver",
                        "harHattFraværHosArbeidsgiver": true,
                        "arbeidsgiverHarUtbetaltLønn": false,
                        "ansettelseslengde": {
                          "merEnn4Uker": false,
                          "begrunnelse": "ANDRE_YTELSER"
                        },
                        "perioder": [
                          {
                            "fraOgMed": "2020-02-01",
                            "tilOgMed": "2020-02-06",
                            "lengde": null
                          }
                        ]
                      }
                    ],
                  "bekreftelser": {
                    "harBekreftetOpplysninger": true,
                    "harForståttRettigheterOgPlikter": true
                  },
                  "utbetalingsperioder": [
                    {
                      "fraOgMed": "2020-01-01",
                      "tilOgMed": "2020-01-11",
                      "lengde": null
                    },
                    {
                      "fraOgMed": "2020-01-21",
                      "tilOgMed": "2020-01-21",
                      "lengde": "PT5H30M"
                    },
                    {
                      "fraOgMed": "2020-01-31",
                      "tilOgMed": "2020-02-05",
                      "lengde": null
                    }
                  ],
                  "andreUtbetalinger": [
                    "dagpenger",
                    "sykepenger"
                  ],
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
                        perioder = listOf()
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
            expectedResponse = """
            {
                "type": "/problem-details/invalid-request-parameters",
                "title": "invalid-request-parameters",
                "status": 400,
                "detail": "Requesten inneholder ugyldige paramtere.",
                "instance": "about:blank",
                "invalid_parameters": [{
                    "type": "entity",
                    "name": "utbetalingsperioder",
                    "reason": "Må settes minst en utbetalingsperiode.",
                    "invalid_value": []
                }, {
                    "type": "entity",
                    "name": "bekreftlser.harBekreftetOpplysninger",
                    "reason": "Må besvars Ja.",
                    "invalid_value": false
                }, {
                    "type": "entity",
                    "name": "bekreftelser.harForståttRettigheterOgPlikter",
                    "reason": "Må besvars Ja.",
                    "invalid_value": false
                }]
            }
            """.trimIndent()
        )
    }

    @Test
    fun `Sende søknad ugyldig fødselsnummer på fosterbarn, gir feilmelding`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val jpegUrl = engine.jpegUrl(cookie)
        val pdfUrl = engine.pdUrl(cookie)

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
                      "name": "fosterbarn[1].fødselsnummer",
                      "reason": "Ikke gyldig fødselsnummer.",
                      "invalid_value": "ugyldig fødselsnummer"
                    }
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = defaultSøknad.copy(
                fosterbarn = listOf(
                    FosterBarn(
                        fødselsnummer = "02119970078"
                    ),
                    FosterBarn(
                        fødselsnummer = "ugyldig fødselsnummer"
                    )
                ),
                vedlegg = listOf(
                    URL(jpegUrl), URL(pdfUrl)
                )
            ).somJson()
        )
    }

    @Test
    fun `Sende søknad med ugyldig andreUtbetalinger`() {
        val cookie = getAuthCookie(gyldigFodselsnummerA)
        val jpegUrl = engine.jpegUrl(cookie)
        val pdfUrl = engine.pdUrl(cookie)

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
                      "name": "andreUtbetalinger[1]",
                      "reason": "Ugyldig verdig for andre utbetaling. Kun 'dagpenger' og 'sykepenger' er tillatt.",
                      "invalid_value": "koronapenger"
                    }
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = """
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
                      "ansettelseslengde": {
                        "merEnn4Uker": true,
                        "begrunnelse": null
                      },
                      "perioder": [
                        {
                          "fraOgMed": "2020-01-01",
                          "tilOgMed": "2020-01-11",
                          "lengde": null
                        }
                      ]
                    }
                  ],
                  "bekreftelser": {
                    "harBekreftetOpplysninger": true,
                    "harForståttRettigheterOgPlikter": true
                  },
                  "andreUtbetalinger": [
                    "dagpenger",
                    "koronapenger"
                  ],
                  "vedlegg": [
                    "$jpegUrl",
                    "$pdfUrl"
                  ]
                }
                """.trimIndent()
        )
    }

    @Test
    fun `Sende ugyldig søknad, der begrunnelse på ansettelseslengde ikke er satt når det har vart mer enn 4 uker`() {
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
                      "name": "arbeidsgivere[0].ansettelseslengde.begrunnelse",
                      "reason": "Begrunnelse kan ikke være null, dersom merEnn4Uker er satt til false.",
                      "invalid_value": null
                    }
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = """
                 {
                    "språk": "nb",
                    "bosteder": [{
                        "fraOgMed": "2019-12-12",
                        "tilOgMed": "2019-12-22",
                        "landkode": "GB",
                        "landnavn": "Great Britain",
                        "erEØSLand": true
                    }],
                    "opphold": [{
                        "fraOgMed": "2019-12-12",
                        "tilOgMed": "2019-12-22",
                        "landkode": "GB",
                        "landnavn": "Great Britain",
                        "erEØSLand": true
                    }],
                    "arbeidsgivere": [
                      {
                        "navn": "Arbeidsgiver 1",
                        "organisasjonsnummer": "917755736",
                        "harHattFraværHosArbeidsgiver": true,
                        "arbeidsgiverHarUtbetaltLønn": false,
                        "ansettelseslengde": {
                          "merEnn4Uker": false
                        },
                        "perioder": [
                          {
                            "fraOgMed": "2020-01-01",
                            "tilOgMed": "2020-01-11",
                            "lengde": null
                          }
                        ]
                      }
                    ],
                    "bekreftelser": {
                        "harBekreftetOpplysninger": true,
                        "harForståttRettigheterOgPlikter": true
                    },
                    "andreUtbetalinger": ["dagpenger", "sykepenger"],
                    "vedlegg": []
                }
                """.trimIndent()
        )
    }

    @Test
    fun `Sende ugyldig søknad, der ansettelseslengde er begrunnelse er INGEN_AV_SITUASJONENE, men mangler forklaring`() {
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
                      "name": "arbeidsgivere[0].ansettelseslengde.ingenAvSituasjoneneForklaring",
                      "reason": "Forklaring for ingen av situasjonene kan ikke være null/tom, dersom begrunnelsen er INGEN_AV_SITUASJONENE",
                      "invalid_value": null
                    }
                  ]
                }
            """.trimIndent(),
            expectedCode = HttpStatusCode.BadRequest,
            cookie = cookie,
            requestEntity = defaultSøknad.copy(
                arbeidsgivere = listOf(
                    defaultSøknad.arbeidsgivere[0].copy(
                        ansettelseslengde = Ansettelseslengde(
                            merEnn4Uker = false,
                            begrunnelse = Ansettelseslengde.Begrunnelse.INGEN_AV_SITUASJONENE,
                            ingenAvSituasjoneneForklaring = null
                        )
                    )
                ),
                vedlegg = listOf()
            ).somJson()
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
                      "name": "utbetalingsperioder[Utbetalingsperiode(fraOgMed=2020-01-01, tilOgMed=2020-01-10, antallTimerBorte=null, antallTimerPlanlagt=PT5H, lengde=null)]",
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
                                antallTimerPlanlagt = Duration.ofHours(5)
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
                      "name": "utbetalingsperioder[Utbetalingsperiode(fraOgMed=2020-01-01, tilOgMed=2020-01-10, antallTimerBorte=PT6H, antallTimerPlanlagt=PT5H, lengde=null)]",
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
                                antallTimerBorte = Duration.ofHours(6)
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
    ) {
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
                assertEquals(expectedCode, response.status())
                if (expectedResponse != null) {
                    JSONAssert.assertEquals(expectedResponse, response.content!!, true)
                } else {
                    assertEquals(expectedResponse, response.content)
                }
            }
        }
    }
}
