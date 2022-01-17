package no.nav.omsorgspengerutbetaling

import com.auth0.jwt.JWT
import com.github.tomakehurst.wiremock.http.Cookie
import com.github.tomakehurst.wiremock.http.Request
import io.ktor.http.*
import no.nav.helse.dusseldorf.testsupport.jws.LoginService
import no.nav.omsorgspengerutbetaling.arbeidsgiver.ArbeidsgiverDetaljer
import no.nav.omsorgspengerutbetaling.arbeidsgiver.Utbetalingsårsak
import no.nav.omsorgspengerutbetaling.arbeidsgiver.ÅrsakNyoppstartet
import no.nav.omsorgspengerutbetaling.felles.*
import no.nav.omsorgspengerutbetaling.soker.Søker
import no.nav.omsorgspengerutbetaling.soknad.Søknad
import java.net.URL
import java.time.Duration
import java.time.LocalDate

class TestUtils {
    companion object {
        private const val GYLDIG_ORGNR = "917755736"
        private val start = LocalDate.parse("2020-01-01")

        fun getIdentFromIdToken(request: Request?): String {
            val idToken: String = request!!.getHeader(HttpHeaders.Authorization).substringAfter("Bearer ")
            return JWT.decode(idToken).subject ?: throw IllegalStateException("Token mangler 'sub' claim.")
        }

        fun getAuthCookie(
            fnr: String,
            level: Int = 4,
            cookieName: String = "localhost-idtoken",
            expiry: Long? = null) : Cookie {

            val overridingClaims : Map<String, Any> = if (expiry == null) emptyMap() else mapOf(
                "exp" to expiry
            )

            val jwt = LoginService.V1_0.generateJwt(fnr = fnr, level = level, overridingClaims = overridingClaims)
            return Cookie(listOf(String.format("%s=%s", cookieName, jwt), "Path=/", "Domain=localhost"))
        }

        val søker = Søker(
            aktørId = "12345",
            fødselsdato = LocalDate.parse("2000-01-01"),
            fødselsnummer = "02119970078",
            fornavn = "Ole",
            mellomnavn = "Dole",
            etternavn = "Doffen"
        )

        fun hentGyldigSøknad() = Søknad(
            språk = Språk.BOKMÅL,
            arbeidsgivere = listOf(
                ArbeidsgiverDetaljer(
                    navn = "Arbeidsgiver 1",
                    organisasjonsnummer = GYLDIG_ORGNR,
                    harHattFraværHosArbeidsgiver = true,
                    arbeidsgiverHarUtbetaltLønn = false,
                    utbetalingsårsak = Utbetalingsårsak.KONFLIKT_MED_ARBEIDSGIVER,
                    konfliktForklaring = "Forklarer konflikten...",
                    perioder = listOf(
                        Utbetalingsperiode(
                            fraOgMed = start,
                            tilOgMed = start.plusDays(10),
                            årsak = FraværÅrsak.ORDINÆRT_FRAVÆR
                        )
                    )
                ),
                ArbeidsgiverDetaljer(
                    navn = "Arbeidsgiver 2",
                    organisasjonsnummer = GYLDIG_ORGNR,
                    harHattFraværHosArbeidsgiver = true,
                    arbeidsgiverHarUtbetaltLønn = false,
                    utbetalingsårsak = Utbetalingsårsak.ARBEIDSGIVER_KONKURS,
                    perioder = listOf(
                        Utbetalingsperiode(
                            fraOgMed = start.plusDays(20),
                            tilOgMed = start.plusDays(20),
                            antallTimerBorte = Duration.ofHours(5),
                            antallTimerPlanlagt = Duration.ofHours(8),
                            årsak = FraværÅrsak.ORDINÆRT_FRAVÆR
                        )
                    )
                ),
                ArbeidsgiverDetaljer(
                    organisasjonsnummer = GYLDIG_ORGNR,
                    navn = "Navn navnesen",
                    harHattFraværHosArbeidsgiver = true,
                    arbeidsgiverHarUtbetaltLønn = false,
                    utbetalingsårsak = Utbetalingsårsak.NYOPPSTARTET_HOS_ARBEIDSGIVER,
                    årsakNyoppstartet = ÅrsakNyoppstartet.ARBEID_I_UTLANDET,
                    perioder = listOf(
                        Utbetalingsperiode(
                            fraOgMed = start.plusMonths(1),
                            tilOgMed = start.plusMonths(1).plusDays(5),
                            årsak = FraværÅrsak.ORDINÆRT_FRAVÆR
                        )
                    )
                )
            ),
            bosteder = listOf(
                Bosted(
                    fraOgMed = start.minusDays(20),
                    tilOgMed = start.minusDays(10),
                    landkode = "GB",
                    landnavn = "Great Britain",
                    erEØSLand = JaNei.Ja
                )
            ),
            opphold = listOf(
                Opphold(
                    fraOgMed = start.minusDays(20),
                    tilOgMed = start.minusDays(10),
                    landkode = "GB",
                    landnavn = "Great Britain",
                    erEØSLand = JaNei.Ja
                )
            ),
            bekreftelser = Bekreftelser(
                harForståttRettigheterOgPlikter = JaNei.Ja,
                harBekreftetOpplysninger = JaNei.Ja
            ),
            vedlegg = listOf(
                URL("http://localhost:8080/vedlegg/1234")
            ),
            hjemmePgaSmittevernhensyn = true,
            hjemmePgaStengtBhgSkole = true
        )
    }
}