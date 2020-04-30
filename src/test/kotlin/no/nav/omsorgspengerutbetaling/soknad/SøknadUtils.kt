package no.nav.omsorgspengerutbetaling.soknad

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.arbeidsgiver.ArbeidsgiverDetaljer
import no.nav.omsorgspengerutbetaling.felles.*
import no.nav.omsorgspengerutbetaling.omsorgspengerKonfiguert
import no.nav.omsorgspengerutbetaling.soker.Søker
import no.nav.omsorgspengerutbetaling.soknad.Ansettelseslengde.Begrunnelse.*
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime

internal object ArbeidstakerutbetalingSøknadUtils {
    internal val objectMapper = jacksonObjectMapper().omsorgspengerKonfiguert()
    private val start = LocalDate.parse("2020-01-01")
    private const val GYLDIG_ORGNR = "917755736"

    internal val defaultSøknad = Søknad(
        språk = Språk.BOKMÅL,
        arbeidsgivere = listOf(
            ArbeidsgiverDetaljer(
                navn = "Arbeidsgiver 1",
                organisasjonsnummer = GYLDIG_ORGNR,
                harHattFraværHosArbeidsgiver = true,
                arbeidsgiverHarUtbetaltLønn = false,
                ansettelseslengde = Ansettelseslengde(
                    merEnn4Uker = true
                ),
                perioder = listOf(
                    Utbetalingsperiode(
                        fraOgMed = start,
                        tilOgMed = start.plusDays(10)
                    )
                )
            ),
            ArbeidsgiverDetaljer(
                navn = "Arbeidsgiver 2",
                organisasjonsnummer = GYLDIG_ORGNR,
                harHattFraværHosArbeidsgiver = true,
                arbeidsgiverHarUtbetaltLønn = false,
                ansettelseslengde = Ansettelseslengde(
                    merEnn4Uker = false,
                    begrunnelse = ANNET_ARBEIDSFORHOLD
                ),
                perioder = listOf(
                    Utbetalingsperiode(
                        fraOgMed = start.plusDays(20),
                        tilOgMed = start.plusDays(20),
                        lengde = Duration.ofHours(5).plusMinutes(30)
                    )
                )
            ),
            ArbeidsgiverDetaljer(
                navn = "Arbeidsgiver 3",
                organisasjonsnummer = GYLDIG_ORGNR,
                harHattFraværHosArbeidsgiver = true,
                arbeidsgiverHarUtbetaltLønn = false,
                ansettelseslengde = Ansettelseslengde(
                    merEnn4Uker = false,
                    begrunnelse = MILITÆRTJENESTE
                ),
                perioder = listOf(
                    Utbetalingsperiode(
                        fraOgMed = start.plusDays(30),
                        tilOgMed = start.plusDays(35)
                    )
                )
            ),
            ArbeidsgiverDetaljer(
                organisasjonsnummer = GYLDIG_ORGNR,
                harHattFraværHosArbeidsgiver = true,
                arbeidsgiverHarUtbetaltLønn = false,
                ansettelseslengde = Ansettelseslengde(
                    merEnn4Uker = false,
                    begrunnelse = ANDRE_YTELSER
                ),
                perioder = listOf(
                    Utbetalingsperiode(
                        fraOgMed = start.plusMonths(1),
                        tilOgMed = start.plusMonths(1).plusDays(5)
                    )
                )
            ),
            ArbeidsgiverDetaljer(
                navn = "Ikke registrert arbeidsgiver",
                harHattFraværHosArbeidsgiver = true,
                arbeidsgiverHarUtbetaltLønn = false,
                ansettelseslengde = Ansettelseslengde(
                    merEnn4Uker = false,
                    begrunnelse = ANDRE_YTELSER
                ),
                perioder = listOf(
                    Utbetalingsperiode(
                        fraOgMed = start.plusMonths(1),
                        tilOgMed = start.plusMonths(1).plusDays(5)
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
        spørsmål = listOf(
            SpørsmålOgSvar(
                spørsmål = "Et spørsmål",
                svar = JaNei.Nei
            )
        ),
        bekreftelser = Bekreftelser(
            harForståttRettigheterOgPlikter = JaNei.Ja,
            harBekreftetOpplysninger = JaNei.Ja
        ),
        andreUtbetalinger = listOf(DAGPENGER, SYKEPENGER),
        fosterbarn = listOf(
            FosterBarn(
                fødselsnummer = "02119970078"
            )
        ),
        vedlegg = emptyList()
    )

    internal val defaultKomplettSøknad = KomplettSøknad(
        språk = Språk.BOKMÅL,
        mottatt = ZonedDateTime.now(),
        søker = Søker(
            aktørId = "123456",
            fødselsnummer = "02119970078",
            fødselsdato = LocalDate.parse("1999-11-02"),
            etternavn = "Nordmann",
            mellomnavn = null,
            fornavn = "Ola",
            myndig = true
        ),
        arbeidsgivere = listOf(
            ArbeidsgiverDetaljer(
                navn = "Arbeidsgiver 1",
                organisasjonsnummer = GYLDIG_ORGNR,
                harHattFraværHosArbeidsgiver = true,
                arbeidsgiverHarUtbetaltLønn = false,
                ansettelseslengde = Ansettelseslengde(
                    merEnn4Uker = true
                ),
                perioder = listOf(
                    Utbetalingsperiode(
                        fraOgMed = start,
                        tilOgMed = start.plusDays(10)
                    )
                )
            ),
            ArbeidsgiverDetaljer(
                navn = "Arbeidsgiver 2",
                organisasjonsnummer = GYLDIG_ORGNR,
                harHattFraværHosArbeidsgiver = true,
                arbeidsgiverHarUtbetaltLønn = false,
                ansettelseslengde = Ansettelseslengde(
                    merEnn4Uker = false,
                    begrunnelse = ANNET_ARBEIDSFORHOLD
                ),
                perioder = listOf(
                    Utbetalingsperiode(
                        fraOgMed = start.plusDays(20),
                        tilOgMed = start.plusDays(20),
                        lengde = Duration.ofHours(5).plusMinutes(30)
                    )
                )
            ),
            ArbeidsgiverDetaljer(
                navn = "Arbeidsgiver 3",
                organisasjonsnummer = GYLDIG_ORGNR,
                harHattFraværHosArbeidsgiver = true,
                arbeidsgiverHarUtbetaltLønn = false,
                ansettelseslengde = Ansettelseslengde(
                    merEnn4Uker = false,
                    begrunnelse = MILITÆRTJENESTE
                ),
                perioder = listOf(
                    Utbetalingsperiode(
                        fraOgMed = start.plusDays(30),
                        tilOgMed = start.plusDays(35)
                    )
                )
            ),
            ArbeidsgiverDetaljer(
                organisasjonsnummer = GYLDIG_ORGNR,
                harHattFraværHosArbeidsgiver = true,
                arbeidsgiverHarUtbetaltLønn = false,
                ansettelseslengde = Ansettelseslengde(
                    merEnn4Uker = false,
                    begrunnelse = ANDRE_YTELSER
                ),
                perioder = listOf(
                    Utbetalingsperiode(
                        fraOgMed = start.plusMonths(1),
                        tilOgMed = start.plusMonths(1).plusDays(5)
                    )
                )
            ),
            ArbeidsgiverDetaljer(
                navn = "Ikke registrert arbeidsgiver",
                harHattFraværHosArbeidsgiver = true,
                arbeidsgiverHarUtbetaltLønn = false,
                ansettelseslengde = Ansettelseslengde(
                    merEnn4Uker = false,
                    begrunnelse = ANDRE_YTELSER
                ),
                perioder = listOf(
                    Utbetalingsperiode(
                        fraOgMed = start.plusMonths(1),
                        tilOgMed = start.plusMonths(1).plusDays(5)
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
        spørsmål = listOf(
            SpørsmålOgSvar(
                spørsmål = "Et spørsmål",
                svar = JaNei.Nei
            )
        ),
        andreUtbetalinger = listOf(DAGPENGER, SYKEPENGER),
        fosterbarn = listOf(
            FosterBarn(
                fødselsnummer = "02119970078"
            )
        ),
        bekreftelser = Bekreftelser(
            harForståttRettigheterOgPlikter = JaNei.Ja,
            harBekreftetOpplysninger = JaNei.Ja
        ),
        vedlegg = emptyList()
    )
}

internal fun Søknad.somJson() = ArbeidstakerutbetalingSøknadUtils.objectMapper.writeValueAsString(this)
internal fun KomplettSøknad.somJson() = ArbeidstakerutbetalingSøknadUtils.objectMapper.writeValueAsString(this)
