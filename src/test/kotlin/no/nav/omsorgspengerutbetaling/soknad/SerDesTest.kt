package no.nav.omsorgspengerutbetaling.soknad

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.omsorgspengerutbetaling.k9format.tilK9Format
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.assertEquals

internal class SerDesTest {

    @Test
    internal fun `Test reserialisering av request`() {
        val søknadId = UUID.randomUUID().toString()
        val søknad = søknad.copy(søknadId = søknadId)

        JSONAssert.assertEquals(søknadJson(søknadId), søknad.somJson(), true)
        assertEquals(søknad, SøknadUtils.objectMapper.readValue(søknadJson(søknadId)))
    }

    @Test
    fun `Test serialisering av request til mottak`() {
        val søknadId = UUID.randomUUID().toString()
        val komplettSøknad = komplettSøknad(søknadId)

        JSONAssert.assertEquals(komplettSøknadJson(søknadId), komplettSøknad.somJson(), true)
        //assertEquals(komplettSøknad, ArbeidstakerutbetalingSøknadUtils.objectMapper.readValue(komplettSøknadJson(søknadId))) //TODO 09.03.2021 - Problemer med å deserialsiere k9format objektet.
    }

    private companion object {
        val now = ZonedDateTime.of(2018, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC"))
        val søknad = SøknadUtils.defaultSøknad
        val k9Format = søknad.tilK9Format(now, SøknadUtils.søker)
        fun komplettSøknad(søknadId: String = UUID.randomUUID().toString()) = søknad.tilKomplettSøknad(SøknadUtils.søker, k9Format, listOf()).copy(søknadId = søknadId)

        //language=json
        fun søknadJson(søknadId: String = UUID.randomUUID().toString()) = """
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
            "vedlegg": [],
            "hjemmePgaSmittevernhensyn": true,
            "hjemmePgaStengtBhgSkole": true
        }
        """.trimIndent()

        //language=json
        fun komplettSøknadJson(søknadId: String = UUID.randomUUID().toString()) =
            """
            {
              "søknadId": "$søknadId",
              "språk": "nb",
              "mottatt": "2018-01-02T03:04:05.000000006Z",
              "søker": {
                "aktørId": "123456",
                "fødselsdato": "1999-11-02",
                "fødselsnummer": "02119970078",
                "fornavn": "Ola",
                "mellomnavn": null,
                "etternavn": "Nordmann",
                "myndig": true
              },
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
                  "perioder": [
                    {
                      "fraOgMed": "2020-01-01",
                      "tilOgMed": "2020-01-11",
                      "antallTimerBorte": null,
                      "antallTimerPlanlagt": null,
                    "årsak": "ORDINÆRT_FRAVÆR"}
                  ]
                },
                {
                  "navn": "Arbeidsgiver 2",
                  "organisasjonsnummer": "917755736",
                  "harHattFraværHosArbeidsgiver": true,
                  "arbeidsgiverHarUtbetaltLønn": false,
                  "perioder": [
                    {
                      "fraOgMed": "2020-01-21",
                      "tilOgMed": "2020-01-21",
                      "antallTimerBorte": "PT5H",
                      "antallTimerPlanlagt": "PT8H",
                    "årsak": "ORDINÆRT_FRAVÆR"}
                  ]
                },
                {
                  "navn": "Arbeidsgiver 3",
                  "organisasjonsnummer": "917755736",
                  "harHattFraværHosArbeidsgiver": true,
                  "arbeidsgiverHarUtbetaltLønn": false,
                  "perioder": [
                    {
                      "fraOgMed": "2020-01-31",
                      "tilOgMed": "2020-02-05",
                      "antallTimerBorte": null,
                      "antallTimerPlanlagt": null,
                    "årsak": "ORDINÆRT_FRAVÆR"}
                  ]
                },
                {
                  "navn": "Arbeidsgiver 4",
                  "organisasjonsnummer": "917755736",
                  "harHattFraværHosArbeidsgiver": true,
                  "arbeidsgiverHarUtbetaltLønn": false,
                  "perioder": [
                    {
                      "fraOgMed": "2020-01-31",
                      "tilOgMed": "2020-02-05",
                      "antallTimerBorte": null,
                      "antallTimerPlanlagt": null,
                    "årsak": "ORDINÆRT_FRAVÆR"}
                  ]
                },
                {
                  "navn": null,
                  "organisasjonsnummer": "917755736",
                  "harHattFraværHosArbeidsgiver": true,
                  "arbeidsgiverHarUtbetaltLønn": false,
                  "perioder": [
                    {
                      "fraOgMed": "2020-02-01",
                      "tilOgMed": "2020-02-06",
                      "antallTimerBorte": null,
                      "antallTimerPlanlagt": null,
                    "årsak": "ORDINÆRT_FRAVÆR"}
                  ]
                },
                {
                  "navn": "Ikke registrert arbeidsgiver",
                  "organisasjonsnummer": null,
                  "harHattFraværHosArbeidsgiver": true,
                  "arbeidsgiverHarUtbetaltLønn": false,
                  "perioder": [
                    {
                      "fraOgMed": "2020-02-01",
                      "tilOgMed": "2020-02-06",
                      "antallTimerBorte": null,
                      "antallTimerPlanlagt": null,
                    "årsak": "ORDINÆRT_FRAVÆR"}
                  ]
                }
              ],
              "bekreftelser": {
                "harBekreftetOpplysninger": true,
                "harForståttRettigheterOgPlikter": true
              },
              "vedlegg": [
                
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
                "ytelse": {
                  "type": "OMP_UT",
                  "fosterbarn": null,
                  "aktivitet": {
                    
                  },
                  "fraværsperioder": [
                    {
                      "periode": "2020-01-01/2020-01-10",
                      "duration": "PT7H30M",
                      "årsak": "STENGT_SKOLE_ELLER_BARNEHAGE",
                      "aktivitetFravær": ["ARBEIDSTAKER"]
                    }
                  ],
                  "bosteder": null,
                  "utenlandsopphold": {
                    "perioder": {
                      "2020-01-01/2020-01-10": {
                        "land": "ESP",
                        "årsak": "barnetInnlagtIHelseinstitusjonDekketEtterAvtaleMedEtAnnetLandOmTrygd"
                      }
                    }
                  }
                },
                "språk": "nb"
              }
            }
            """.trimIndent()
    }
}
