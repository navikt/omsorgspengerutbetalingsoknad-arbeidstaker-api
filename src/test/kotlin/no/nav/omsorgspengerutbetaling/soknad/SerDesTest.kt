package no.nav.omsorgspengerutbetaling.soknad

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.omsorgspengerutbetaling.TestUtils
import no.nav.omsorgspengerutbetaling.TestUtils.Companion.søker
import no.nav.omsorgspengerutbetaling.felles.objectMapper
import no.nav.omsorgspengerutbetaling.felles.somJson
import no.nav.omsorgspengerutbetaling.k9format.tilK9Format
import org.skyscreamer.jsonassert.JSONAssert
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SerDesTest {

    @Test
    internal fun `Test reserialisering av request`() {
        val søknad = søknad

        JSONAssert.assertEquals(søknadJson, søknad.somJson(), true)
        assertEquals(søknad, objectMapper.readValue(søknadJson))
    }

    @Test
    fun `Test serialisering av request til mottak`() {
        val komplettSøknad = komplettSøknad()

        JSONAssert.assertEquals(komplettSøknadJson, komplettSøknad.somJson(), true)
        //assertEquals(komplettSøknad, ArbeidstakerutbetalingSøknadUtils.objectMapper.readValue(komplettSøknadJson(søknadId))) //TODO 09.03.2021 - Problemer med å deserialsiere k9format objektet.
    }

    private companion object {
        val mottatt = ZonedDateTime.of(2018, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC"))
        val søknadId = UUID.randomUUID().toString()
        val søknad = TestUtils.hentGyldigSøknad().copy(søknadId = søknadId)
        val k9Format = søknad.tilK9Format(mottatt, søker)
        fun komplettSøknad() = søknad.tilKomplettSøknad(søker, k9Format, mottatt, listOf()).copy(søknadId = søknadId)

        //language=json
        val søknadJson = """
        {
            "søknadId" : "$søknadId",
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
                    "konfliktForklaring": "Forklarer konflikten...",
                    "utbetalingsårsak": "KONFLIKT_MED_ARBEIDSGIVER",
                    "årsakNyoppstartet": null,
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
                    "utbetalingsårsak": "ARBEIDSGIVER_KONKURS",
                    "årsakNyoppstartet": null,
                    "konfliktForklaring": null,
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
                  "navn": "Navn navnesen",
                  "organisasjonsnummer": "917755736",
                    "harHattFraværHosArbeidsgiver": true,
                    "arbeidsgiverHarUtbetaltLønn": false,
                    "utbetalingsårsak": "NYOPPSTARTET_HOS_ARBEIDSGIVER",
                    "årsakNyoppstartet": "ARBEID_I_UTLANDET",
                    "konfliktForklaring": null,
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
            "vedlegg": [
              "http://localhost:8080/vedlegg/1234"
            ],
            "hjemmePgaSmittevernhensyn": true,
            "hjemmePgaStengtBhgSkole": true
        }
        """.trimIndent()

        //language=json
        val komplettSøknadJson =
            """
            {
              "søknadId": "$søknadId",
              "språk": "nb",
              "mottatt": "2018-01-02T03:04:05.000000006Z",
              "søker": {
                "aktørId": "12345",
                "fødselsdato": "2000-01-01",
                "fødselsnummer": "02119970078",
                "fornavn": "Ole",
                "mellomnavn": "Dole",
                "etternavn": "Doffen",
                "myndig": true
              },
              "titler" : [],
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
                  "konfliktForklaring": "Forklarer konflikten...",
                  "utbetalingsårsak": "KONFLIKT_MED_ARBEIDSGIVER",
                  "årsakNyoppstartet": null,
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
                  "konfliktForklaring": null,
                  "utbetalingsårsak": "ARBEIDSGIVER_KONKURS",
                  "årsakNyoppstartet": null,
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
                  "navn": "Navn navnesen",
                  "organisasjonsnummer": "917755736",
                  "harHattFraværHosArbeidsgiver": true,
                  "arbeidsgiverHarUtbetaltLønn": false,
                  "konfliktForklaring": null,
                  "utbetalingsårsak": "NYOPPSTARTET_HOS_ARBEIDSGIVER",
                  "årsakNyoppstartet": "ARBEID_I_UTLANDET",
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
              "bekreftelser": {
                "harBekreftetOpplysninger": true,
                "harForståttRettigheterOgPlikter": true
              },
              "vedleggId": [
                "1234"
              ],
              "hjemmePgaSmittevernhensyn": true,
              "hjemmePgaStengtBhgSkole": true,
              "k9Format": {
                "søknadId": "$søknadId",
                "versjon": "1.0.0",
                "mottattDato": "2018-01-02T03:04:05.000Z",
                "søker": {
                  "norskIdentitetsnummer": "02119970078"
                },
                "journalposter" : [],
                "ytelse": {
                  "type": "OMP_UT",
                  "fosterbarn": null,
                  "aktivitet": {
                    
                  },
                  "fraværsperioder": [
                    {
                      "periode": "2020-01-01/2020-01-11",
                      "duration": null,
                      "årsak": "ORDINÆRT_FRAVÆR",
                      "søknadÅrsak": "KONFLIKT_MED_ARBEIDSGIVER",
                      "aktivitetFravær": [
                        "ARBEIDSTAKER"
                      ],
                      "arbeidsgiverOrgNr": "917755736",
                      "arbeidsforholdId": null
                    },
                    {
                      "periode": "2020-01-21/2020-01-21",
                      "duration": "PT5H",
                      "årsak": "ORDINÆRT_FRAVÆR",
                      "søknadÅrsak": "ARBEIDSGIVER_KONKURS",
                      "aktivitetFravær": [
                        "ARBEIDSTAKER"
                      ],
                      "arbeidsgiverOrgNr": "917755736",
                      "arbeidsforholdId": null
                    },
                    {
                      "periode": "2020-02-01/2020-02-06",
                      "duration": null,
                      "årsak": "ORDINÆRT_FRAVÆR",
                      "søknadÅrsak": "NYOPPSTARTET_HOS_ARBEIDSGIVER",
                      "aktivitetFravær": [
                        "ARBEIDSTAKER"
                      ],
                      "arbeidsgiverOrgNr": "917755736",
                      "arbeidsforholdId": null
                    }
                  ],
                  "fraværsperioderKorrigeringIm": null,
                  "bosteder": {
                    "perioder": {
                      "2019-12-12/2019-12-22": {
                        "land": "GB"
                      }
                    },
                    "perioderSomSkalSlettes": { }
                  },
                  "utenlandsopphold": {
                    "perioder": {
                      "2019-12-12/2019-12-22": {
                        "land": "GB",
                        "årsak": null
                      }
                    },
                    "perioderSomSkalSlettes": {
                      
                    }
                  }
                },
                "språk": "nb",
                "begrunnelseForInnsending": {
                  "tekst": null
                }
              }
            }
            """.trimIndent()
    }
}
