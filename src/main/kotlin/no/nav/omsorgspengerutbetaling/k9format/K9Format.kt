package no.nav.omsorgspengerutbetaling.k9format

import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.fravær.AktivitetFravær
import no.nav.k9.søknad.felles.fravær.FraværPeriode
import no.nav.k9.søknad.felles.opptjening.OpptjeningAktivitet
import no.nav.k9.søknad.felles.personopplysninger.Barn
import no.nav.k9.søknad.felles.personopplysninger.Bosteder
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold
import no.nav.k9.søknad.felles.type.Landkode
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.omsorgspenger.v1.OmsorgspengerUtbetaling
import no.nav.omsorgspengerutbetaling.arbeidsgiver.ArbeidsgiverDetaljer
import no.nav.omsorgspengerutbetaling.felles.Bosted
import no.nav.omsorgspengerutbetaling.felles.FosterBarn
import no.nav.omsorgspengerutbetaling.felles.FraværÅrsak
import no.nav.omsorgspengerutbetaling.felles.FraværÅrsak.*
import no.nav.omsorgspengerutbetaling.felles.Opphold
import no.nav.omsorgspengerutbetaling.soker.Søker
import no.nav.omsorgspengerutbetaling.soknad.Søknad
import java.time.ZonedDateTime
import no.nav.k9.søknad.Søknad as K9Søknad
import no.nav.k9.søknad.felles.fravær.FraværÅrsak as K9FraværÅrsak
import no.nav.k9.søknad.felles.personopplysninger.Søker as K9Søker

private val k9FormatVersjon = Versjon.of("1.0.0")

fun Søknad.tilK9Format(mottatt: ZonedDateTime, søker: Søker): K9Søknad {
    return K9Søknad(
        SøknadId.of(søknadId),
        k9FormatVersjon,
        mottatt,
        søker.tilK9Søker(),
        OmsorgspengerUtbetaling(
            fosterbarn?.tilK9Barn(),
            OpptjeningAktivitet(null, null, null), //Trenger ikke OpptjeningAktivitet for denne ytelsen.
            arbeidsgivere.byggK9Fraværsperiode(),
            bosteder.tilK9Bosteder(),
            opphold.tilK9Utenlandsopphold()
        )
    )
}

fun Søker.tilK9Søker(): K9Søker = K9Søker(NorskIdentitetsnummer.of(fødselsnummer))

fun List<FosterBarn>.tilK9Barn(): List<Barn> = map {
    Barn(NorskIdentitetsnummer.of(it.identitetsnummer), null)
}

fun List<Bosted>.tilK9Bosteder(): Bosteder {
    val perioder = mutableMapOf<Periode, Bosteder.BostedPeriodeInfo>()

    forEach {
        val periode = Periode(it.fraOgMed, it.tilOgMed)
        perioder[periode] = Bosteder.BostedPeriodeInfo(Landkode.of(it.landkode))
    }

    return Bosteder(perioder)
}

fun List<Opphold>.tilK9Utenlandsopphold(): Utenlandsopphold {
    val perioder = mutableMapOf<Periode, Utenlandsopphold.UtenlandsoppholdPeriodeInfo>()

    forEach {
        val periode = Periode(it.fraOgMed, it.tilOgMed)
        perioder[periode] = Utenlandsopphold.UtenlandsoppholdPeriodeInfo.builder()
            .land(Landkode.of(it.landkode))
            .build()
    }

    return Utenlandsopphold(perioder)
}

fun List<ArbeidsgiverDetaljer>.byggK9Fraværsperiode(): List<FraværPeriode> {
    val fraværsperioder = mutableListOf<FraværPeriode>()

    forEach { arbeidsgiver ->
        arbeidsgiver.perioder.forEach { utbetalingsperiode ->
            fraværsperioder.add(
                FraværPeriode(
                    Periode(utbetalingsperiode.fraOgMed, utbetalingsperiode.tilOgMed),
                    utbetalingsperiode.antallTimerBorte,
                    utbetalingsperiode.årsak?.tilK9Årsak() ?: K9FraværÅrsak.ORDINÆRT_FRAVÆR,
                    listOf(AktivitetFravær.ARBEIDSTAKER)
                )
            )
        }
    }

    return fraværsperioder
}

private fun FraværÅrsak.tilK9Årsak(): K9FraværÅrsak {
    return when(this) {
        ORDINÆRT_FRAVÆR -> K9FraværÅrsak.ORDINÆRT_FRAVÆR
        SMITTEVERNHENSYN -> K9FraværÅrsak.SMITTEVERNHENSYN
        STENGT_SKOLE_ELLER_BARNEHAGE -> K9FraværÅrsak.STENGT_SKOLE_ELLER_BARNEHAGE
    }
}
