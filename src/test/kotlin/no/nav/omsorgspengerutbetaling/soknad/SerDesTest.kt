package no.nav.omsorgspengerutbetaling.soknad

import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.assertEquals

internal class SerDesTest {

    @Test
    internal fun `Test reserialisering av request`() {
        JSONAssert.assertEquals(SøknadJson, søknad.somJson(), true)
        assertEquals(
            søknad, ArbeidstakerutbetalingSøknadUtils.objectMapper.readValue(
                SøknadJson
            )
        )
    }

    @Test
    fun `Test serialisering av request til mottak`() {
        JSONAssert.assertEquals(KomplettSøknadJson, komplettSøknad.somJson(), true)
        assertEquals(
            komplettSøknad, ArbeidstakerutbetalingSøknadUtils.objectMapper.readValue(
                KomplettSøknadJson
            )
        )
    }

    private companion object {
        val now = ZonedDateTime.of(2018, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC"))
        internal val start = LocalDate.parse("2020-01-01")

        internal val søknad = ArbeidstakerutbetalingSøknadUtils.defaultSøknad
        internal val komplettSøknad = ArbeidstakerutbetalingSøknadUtils.defaultKomplettSøknad.copy(
            mottatt = now
        )

        //language=json
        internal val SøknadJson = """
        {
            "språk": "nb",
            "bosteder": [{
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
                        "merEnn4Uker": true,
                        "begrunnelse": null,
                        "ingenAvSituasjoneneForklaring": null
                      },
                    "perioder": [
                      {
                        "fraOgMed": "2020-01-01",
                        "tilOgMed": "2020-01-11",
                        "antallTimerBorte": null,
                        "antallTimerPlanlagt": null,
                        "årsak": "ORDINÆRT_FRAVÆR"
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
                        "begrunnelse": "ANNET_ARBEIDSFORHOLD",
                        "ingenAvSituasjoneneForklaring": null
                      },
                    "perioder": [
                      {
                        "fraOgMed": "2020-01-21",
                        "tilOgMed": "2020-01-21",
                        "antallTimerBorte": "PT5H",
                        "antallTimerPlanlagt": "PT8H",
                        "årsak": "ORDINÆRT_FRAVÆR"
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
                        "begrunnelse": "MILITÆRTJENESTE",
                        "ingenAvSituasjoneneForklaring": null
                      },
                    "perioder": [
                      {
                        "fraOgMed": "2020-01-31",
                        "tilOgMed": "2020-02-05",
                        "antallTimerBorte": "PT5H",
                        "antallTimerPlanlagt": "PT8H",
                        "årsak": "ORDINÆRT_FRAVÆR"
                      }
                    ]
                },
                {
                  "navn": "Arbeidsgiver 4",
                  "organisasjonsnummer": "917755736",
                    "harHattFraværHosArbeidsgiver": true,
                    "arbeidsgiverHarUtbetaltLønn": false,
                     "ansettelseslengde": {
                        "merEnn4Uker": false,
                        "begrunnelse": "INGEN_AV_SITUASJONENE",
                        "ingenAvSituasjoneneForklaring": "Forklarer hvorfor ingen av situasjonene passer."
                      },
                    "perioder": [
                      {
                        "fraOgMed": "2020-01-31",
                        "tilOgMed": "2020-02-05",
                        "antallTimerBorte": null,
                        "antallTimerPlanlagt": null,
                        "årsak": "ORDINÆRT_FRAVÆR"
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
                        "begrunnelse": "ANDRE_YTELSER",
                        "ingenAvSituasjoneneForklaring": null
                      },
                    "perioder": [
                      {
                        "fraOgMed": "2020-02-01",
                        "tilOgMed": "2020-02-06",
                        "antallTimerBorte": null,
                        "antallTimerPlanlagt": null,
                        "årsak": "ORDINÆRT_FRAVÆR"
                      }
                    ]
                },
                {
                  "navn": "Ikke registrert arbeidsgiver",
                  "organisasjonsnummer": null,
                    "harHattFraværHosArbeidsgiver": true,
                    "arbeidsgiverHarUtbetaltLønn": false,
                     "ansettelseslengde": {
                        "merEnn4Uker": false,
                        "begrunnelse": "ANDRE_YTELSER",
                        "ingenAvSituasjoneneForklaring": null
                      },
                    "perioder": [
                      {
                        "fraOgMed": "2020-02-01",
                        "tilOgMed": "2020-02-06",
                        "antallTimerBorte": null,
                        "antallTimerPlanlagt": null,
                        "årsak": "ORDINÆRT_FRAVÆR"
                      }
                    ]
                }
              ],
            "opphold": [{
                "fraOgMed": "2019-12-12",
                "tilOgMed": "2019-12-22",
                "landkode": "GB",
                "landnavn": "Great Britain",
                "erEØSLand": true
            }],
            "bekreftelser": {
                "harBekreftetOpplysninger": true,
                "harForståttRettigheterOgPlikter": true
            },
            "erSelvstendig": false,
            "erFrilanser": false,
            "andreUtbetalinger": ["dagpenger", "sykepenger"],
            "fosterbarn": [{
                "fødselsnummer": "02119970078"
            }],
            "vedlegg": [],
            "hjemmePgaSmittevernhensyn": true,
            "hjemmePgaStengtBhgSkole": true,
            "barn": [
              {
                "identitetsnummer": "26104500284",
                "aktørId": null,
                "navn": "Ole Dole",
                "aleneOmOmsorgen": true
              }
            ]
        }
        """.trimIndent()

        //language=json
        internal val KomplettSøknadJson = """
        {
            "mottatt": "2018-01-02T03:04:05.000000006Z",
            "språk": "nb",
            "søker": {
                "aktørId": "123456",
                "fødselsdato": "1999-11-02",
                "fødselsnummer": "02119970078",
                "fornavn": "Ola",
                "mellomnavn": null,
                "etternavn": "Nordmann",
                "myndig": true
            },
            "arbeidsgivere": [
                {
                    "navn": "Arbeidsgiver 1",
                    "organisasjonsnummer": "917755736",
                    "harHattFraværHosArbeidsgiver": true,
                    "arbeidsgiverHarUtbetaltLønn": false,
                     "ansettelseslengde": {
                        "merEnn4Uker": true,
                        "begrunnelse": null,
                        "ingenAvSituasjoneneForklaring": null
                      },
                    "perioder": [
                      {
                        "fraOgMed": "2020-01-01",
                        "tilOgMed": "2020-01-11",
                        "antallTimerBorte": null,
                        "antallTimerPlanlagt": null,
                        "årsak": "ORDINÆRT_FRAVÆR"
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
                        "begrunnelse": "ANNET_ARBEIDSFORHOLD",
                        "ingenAvSituasjoneneForklaring": null
                      },
                    "perioder": [
                      {
                        "fraOgMed": "2020-01-21",
                        "tilOgMed": "2020-01-21",
                        "antallTimerBorte": "PT5H",
                        "antallTimerPlanlagt": "PT8H",
                        "årsak": "ORDINÆRT_FRAVÆR"
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
                        "begrunnelse": "MILITÆRTJENESTE",
                        "ingenAvSituasjoneneForklaring": null
                      },
                    "perioder": [
                      {
                        "fraOgMed": "2020-01-31",
                        "tilOgMed": "2020-02-05",
                        "antallTimerBorte": null,
                        "antallTimerPlanlagt": null,
                        "årsak": "ORDINÆRT_FRAVÆR"
                      }
                    ]
                },
                {
                  "navn": "Arbeidsgiver 4",
                  "organisasjonsnummer": "917755736",
                    "harHattFraværHosArbeidsgiver": true,
                    "arbeidsgiverHarUtbetaltLønn": false,
                     "ansettelseslengde": {
                        "merEnn4Uker": false,
                        "begrunnelse": "INGEN_AV_SITUASJONENE",
                        "ingenAvSituasjoneneForklaring": "Forklarer hvorfor ingen av situasjonene passer."
                      },
                    "perioder": [
                      {
                        "fraOgMed": "2020-01-31",
                        "tilOgMed": "2020-02-05",
                        "antallTimerBorte": null,
                        "antallTimerPlanlagt": null,
                        "årsak": "ORDINÆRT_FRAVÆR"
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
                        "begrunnelse": "ANDRE_YTELSER",
                        "ingenAvSituasjoneneForklaring": null
                      },
                    "perioder": [
                      {
                        "fraOgMed": "2020-02-01",
                        "tilOgMed": "2020-02-06",
                        "antallTimerBorte": null,
                        "antallTimerPlanlagt": null,
                        "årsak": "ORDINÆRT_FRAVÆR"
                      }
                    ]
                },
                {
                  "navn": "Ikke registrert arbeidsgiver",
                  "organisasjonsnummer": null,
                    "harHattFraværHosArbeidsgiver": true,
                    "arbeidsgiverHarUtbetaltLønn": false,
                     "ansettelseslengde": {
                        "merEnn4Uker": false,
                        "begrunnelse": "ANDRE_YTELSER",
                        "ingenAvSituasjoneneForklaring": null
                      },
                    "perioder": [
                      {
                        "fraOgMed": "2020-02-01",
                        "tilOgMed": "2020-02-06",
                        "antallTimerBorte": null,
                        "antallTimerPlanlagt": null,
                        "årsak": "ORDINÆRT_FRAVÆR"
                      }
                    ]
                }
              ],
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
            "bekreftelser": {
                "harBekreftetOpplysninger": true,
                "harForståttRettigheterOgPlikter": true
            },
            "andreUtbetalinger": ["dagpenger", "sykepenger"],
            "erSelvstendig": false,
            "erFrilanser": false,
            "fosterbarn": [{
                "fødselsnummer": "02119970078"
            }],
            "vedlegg": [],
            "hjemmePgaSmittevernhensyn": true,
            "hjemmePgaStengtBhgSkole": true,
            "barn": [
              {
                "identitetsnummer": "26104500284",
                "aktørId": null,
                "navn": "Ole Dole",
                "aleneOmOmsorgen": true
              }
            ]
        }
        """.trimIndent()
    }
}
