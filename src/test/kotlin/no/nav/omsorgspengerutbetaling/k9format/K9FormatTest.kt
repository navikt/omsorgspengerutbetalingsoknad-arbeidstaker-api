package no.nav.omsorgspengerutbetaling.k9format

import no.nav.k9.søknad.JsonUtils
import no.nav.omsorgspengerutbetaling.soknad.SøknadUtils
import org.skyscreamer.jsonassert.JSONAssert
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.Test

class K9FormatTest {

    @Test
    fun `Gyldig søknad blir til forventet k9-format`() {
        val mottatt = ZonedDateTime.of(2020, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC"))
        val søknadId = UUID.randomUUID().toString()

        val søknad = SøknadUtils.defaultSøknad.copy(søknadId = søknadId)
        val k9Format = søknad.tilK9Format(mottatt, SøknadUtils.søker)

        val forventetK9FormatJson =
            //language=json
            """
            {
              "søknadId": "$søknadId",
              "versjon": "1.0.0",
              "mottattDato": "2020-01-02T03:04:05.000Z",
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
                    "periode": "2020-01-01/2020-01-11",
                    "duration": null,
                    "årsak": "ORDINÆRT_FRAVÆR",
                    "søknadÅrsak": "KONFLIKT_MED_ARBEIDSGIVER",
                    "aktivitetFravær": [
                      "ARBEIDSTAKER"
                    ],
                    "arbeidsgiverOrgNr": "917755736"
                  },
                  {
                    "periode": "2020-01-21/2020-01-21",
                    "duration": "PT5H",
                    "årsak": "ORDINÆRT_FRAVÆR",
                    "søknadÅrsak": "ARBEIDSGIVER_KONKURS",
                    "aktivitetFravær": [
                      "ARBEIDSTAKER"
                    ],
                    "arbeidsgiverOrgNr": "917755736"
                  },
                  {
                    "periode": "2020-01-31/2020-02-05",
                    "duration": "PT5H",
                    "årsak": "ORDINÆRT_FRAVÆR",
                    "søknadÅrsak": "ARBEIDSGIVER_KONKURS",
                    "aktivitetFravær": [
                      "ARBEIDSTAKER"
                    ],
                    "arbeidsgiverOrgNr": "917755736"
                  },
                  {
                    "periode": "2020-01-31/2020-02-05",
                    "duration": null,
                    "årsak": "ORDINÆRT_FRAVÆR",
                    "søknadÅrsak": "NYOPPSTARTET_HOS_ARBEIDSGIVER",
                    "aktivitetFravær": [
                      "ARBEIDSTAKER"
                    ],
                    "arbeidsgiverOrgNr": "917755736"
                  },
                  {
                    "periode": "2020-02-01/2020-02-06",
                    "duration": null,
                    "årsak": "ORDINÆRT_FRAVÆR",
                    "søknadÅrsak": "NYOPPSTARTET_HOS_ARBEIDSGIVER",
                    "aktivitetFravær": [
                      "ARBEIDSTAKER"
                    ],
                    "arbeidsgiverOrgNr": "917755736"
                  }
                ],
                "bosteder": {
                  "perioder": {
                    "2019-12-12/2019-12-22": {
                      "land": "GB"
                    }
                  },
                  "perioderSomSkalSlettes": {
                    
                  }
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
              "journalposter": [
                
              ]
            }
        """.trimIndent()

        JSONAssert.assertEquals(forventetK9FormatJson, JsonUtils.toString(k9Format), true)
    }

}