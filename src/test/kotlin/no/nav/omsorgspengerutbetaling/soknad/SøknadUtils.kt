package no.nav.omsorgspengerutbetaling.soknad

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.fravær.AktivitetFravær
import no.nav.k9.søknad.felles.fravær.FraværPeriode
import no.nav.k9.søknad.felles.fravær.SøknadÅrsak
import no.nav.k9.søknad.felles.opptjening.OpptjeningAktivitet
import no.nav.k9.søknad.felles.personopplysninger.Barn
import no.nav.k9.søknad.felles.personopplysninger.Bosteder
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold
import no.nav.k9.søknad.felles.type.*
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.omsorgspenger.v1.OmsorgspengerUtbetaling
import no.nav.omsorgspengerutbetaling.arbeidsgiver.ArbeidsgiverDetaljer
import no.nav.omsorgspengerutbetaling.arbeidsgiver.Utbetalingsårsak
import no.nav.omsorgspengerutbetaling.felles.*
import no.nav.omsorgspengerutbetaling.felles.Språk
import no.nav.omsorgspengerutbetaling.omsorgspengerKonfiguert
import no.nav.omsorgspengerutbetaling.soker.Søker
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*
import no.nav.k9.søknad.Søknad as K9Søknad
import no.nav.k9.søknad.felles.personopplysninger.Søker as K9Søker

internal object SøknadUtils {
    internal val objectMapper = jacksonObjectMapper().omsorgspengerKonfiguert()
    private val start = LocalDate.parse("2020-01-01")
    private const val GYLDIG_ORGNR = "917755736"

    val søker = Søker(
        aktørId = "12345",
        fødselsdato = LocalDate.parse("2000-01-01"),
        fødselsnummer = "02119970078",
        fornavn = "Ole",
        mellomnavn = "Dole",
        etternavn = "Doffen"
    )

    internal val defaultSøknad = Søknad(
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
                navn = "Arbeidsgiver 3",
                organisasjonsnummer = GYLDIG_ORGNR,
                harHattFraværHosArbeidsgiver = true,
                arbeidsgiverHarUtbetaltLønn = false,
                utbetalingsårsak = Utbetalingsårsak.ARBEIDSGIVER_KONKURS,
                perioder = listOf(
                    Utbetalingsperiode(
                        fraOgMed = start.plusDays(30),
                        tilOgMed = start.plusDays(35),
                        antallTimerBorte = Duration.ofHours(5),
                        antallTimerPlanlagt = Duration.ofHours(8),
                        årsak = FraværÅrsak.ORDINÆRT_FRAVÆR
                    )
                )
            ),
            ArbeidsgiverDetaljer(
                navn = "Arbeidsgiver 4",
                organisasjonsnummer = GYLDIG_ORGNR,
                harHattFraværHosArbeidsgiver = true,
                arbeidsgiverHarUtbetaltLønn = false,
                utbetalingsårsak = Utbetalingsårsak.NYOPPSTARTET_HOS_ARBEIDSGIVER,
                perioder = listOf(
                    Utbetalingsperiode(
                        fraOgMed = start.plusDays(30),
                        tilOgMed = start.plusDays(35),
                        årsak = FraværÅrsak.ORDINÆRT_FRAVÆR
                    )
                )
            ),
            ArbeidsgiverDetaljer(
                organisasjonsnummer = GYLDIG_ORGNR,
                harHattFraværHosArbeidsgiver = true,
                arbeidsgiverHarUtbetaltLønn = false,
                utbetalingsårsak = Utbetalingsårsak.NYOPPSTARTET_HOS_ARBEIDSGIVER,
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
        erSelvstendig = JaNei.Nei,
        erFrilanser = JaNei.Nei,
        vedlegg = emptyList(),
        hjemmePgaSmittevernhensyn = true,
        hjemmePgaStengtBhgSkole = true
    )

    fun defaultK9Format(søknadId: String = UUID.randomUUID().toString(), mottatt: ZonedDateTime = ZonedDateTime.now()) =
        K9Søknad(
            SøknadId(søknadId),
            Versjon.of("1.0.0"),
            mottatt,
            K9Søker(NorskIdentitetsnummer.of("02119970078")),
            OmsorgspengerUtbetaling(
                listOf(
                    Barn(NorskIdentitetsnummer.of("26128027024"), null)
                ),
                OpptjeningAktivitet(),
                listOf(
                    FraværPeriode(
                        Periode(LocalDate.parse("2020-01-01"), LocalDate.parse("2020-01-10")),
                        Duration.ofHours(7).plusMinutes(30),
                        no.nav.k9.søknad.felles.fravær.FraværÅrsak.STENGT_SKOLE_ELLER_BARNEHAGE,
                        SøknadÅrsak.ARBEIDSGIVER_KONKURS,
                        listOf(AktivitetFravær.ARBEIDSTAKER),
                        Organisasjonsnummer.of("917755736")
                    )
                ),
                Bosteder().medPerioder(
                    mapOf(
                        Periode(LocalDate.parse("2020-01-01"), LocalDate.parse("2020-01-10")) to
                                Bosteder.BostedPeriodeInfo().medLand(Landkode.NORGE)
                    )
                ),
                Utenlandsopphold().medPerioder(
                    mapOf(
                        Periode(LocalDate.parse("2020-01-01"), LocalDate.parse("2020-01-10")) to
                                Utenlandsopphold.UtenlandsoppholdPeriodeInfo()
                                    .medLand(Landkode.SPANIA)
                                    .medÅrsak(Utenlandsopphold.UtenlandsoppholdÅrsak.BARNET_INNLAGT_I_HELSEINSTITUSJON_DEKKET_ETTER_AVTALE_MED_ET_ANNET_LAND_OM_TRYGD)
                    )
                )
            )
        )
}

internal fun Søknad.somJson() = SøknadUtils.objectMapper.writeValueAsString(this)
internal fun KomplettSøknad.somJson() = SøknadUtils.objectMapper.writeValueAsString(this)
