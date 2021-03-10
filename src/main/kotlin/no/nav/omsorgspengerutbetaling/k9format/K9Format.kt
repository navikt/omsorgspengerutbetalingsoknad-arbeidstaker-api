package no.nav.omsorgspengerutbetaling.k9format

import no.nav.k9.søknad.felles.Versjon
import no.nav.k9.søknad.felles.fravær.FraværPeriode
import no.nav.k9.søknad.felles.personopplysninger.Barn
import no.nav.k9.søknad.felles.personopplysninger.Bosteder
import no.nav.k9.søknad.felles.personopplysninger.Utenlandsopphold
import no.nav.k9.søknad.felles.type.Landkode
import no.nav.k9.søknad.felles.type.NorskIdentitetsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.felles.type.SøknadId
import no.nav.k9.søknad.ytelse.omsorgspenger.v1.OmsorgspengerUtbetaling
import no.nav.omsorgspengerutbetaling.felles.Bosted
import no.nav.omsorgspengerutbetaling.felles.FosterBarn
import no.nav.omsorgspengerutbetaling.felles.Opphold
import no.nav.omsorgspengerutbetaling.soker.Søker
import no.nav.omsorgspengerutbetaling.soknad.Søknad
import java.time.Duration
import java.time.ZonedDateTime
import no.nav.k9.søknad.Søknad as K9Søknad
import no.nav.k9.søknad.felles.personopplysninger.Søker as K9Søker

private val k9FormatVersjon = Versjon.of("1.0.0")
internal val fullArbeidsdag: Duration = Duration.ofHours(7).plusMinutes(30)

fun Søknad.tilK9Format(mottatt: ZonedDateTime, søker: Søker): K9Søknad {
    return K9Søknad(
        SøknadId.of(søknadId),
        k9FormatVersjon,
        mottatt,
        søker.tilK9Søker(),
        OmsorgspengerUtbetaling(
            fosterbarn?.tilK9Barn(),
            byggK9ArbeidAktivitet(),
            byggK9Fraværsperiode(),
            bosteder.tilK9Bosteder(),
            opphold.tilK9Utenlandsopphold()
        )
    )
}

fun Søker.tilK9Søker(): K9Søker = K9Søker(NorskIdentitetsnummer.of(fødselsnummer))

fun List<FosterBarn>.tilK9Barn(): List<Barn> = map {
    Barn(NorskIdentitetsnummer.of(it.fødselsnummer), null)
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

fun Søknad.byggK9Fraværsperiode(): List<FraværPeriode> {
    val fraværsperioder = mutableListOf<FraværPeriode>()

    arbeidsgivere.forEach { arbeidsgiver ->
        arbeidsgiver.perioder.forEach { utbetalingsperiode ->
            val periode = Periode(utbetalingsperiode.fraOgMed, utbetalingsperiode.tilOgMed)
            val lengde = utbetalingsperiode.antallTimerBorte ?: fullArbeidsdag
            fraværsperioder.add(FraværPeriode(periode, lengde))
        }
    }

    return fraværsperioder
}