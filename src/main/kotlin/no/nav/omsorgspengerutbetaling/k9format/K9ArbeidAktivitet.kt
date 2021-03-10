package no.nav.omsorgspengerutbetaling.k9format

import no.nav.k9.søknad.felles.aktivitet.ArbeidAktivitet
import no.nav.k9.søknad.felles.aktivitet.Arbeidstaker
import no.nav.k9.søknad.felles.aktivitet.Organisasjonsnummer
import no.nav.k9.søknad.felles.type.Periode
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidInfo
import no.nav.k9.søknad.ytelse.psb.v1.arbeidstid.ArbeidstidPeriodeInfo
import no.nav.omsorgspengerutbetaling.arbeidsgiver.ArbeidsgiverDetaljer
import no.nav.omsorgspengerutbetaling.felles.Utbetalingsperiode
import no.nav.omsorgspengerutbetaling.soknad.Søknad
import java.time.Duration

fun Søknad.byggK9ArbeidAktivitet() = ArbeidAktivitet(
    null, //arbeidsgivere.tilK9Arbeidstaker(), //TODO 10.03.2021 - Må avklares hva som skal brukes her.
    null,
    null
)

fun List<ArbeidsgiverDetaljer>.tilK9Arbeidstaker(): List<Arbeidstaker> {
    return map { arbeidsgiverDetaljer ->
        Arbeidstaker(
            null, //K9 format vil ikke ha både fnr og org nummer
            Organisasjonsnummer.of(arbeidsgiverDetaljer.organisasjonsnummer),
            arbeidsgiverDetaljer.tilK9ArbeidstidInfo()
        )
    }
}

fun ArbeidsgiverDetaljer.tilK9ArbeidstidInfo(): ArbeidstidInfo = ArbeidstidInfo(
     fullArbeidsdag, //TODO 09.03.2021 - Må avklares hva som skal brukes her
    this.perioder.tilK9Perioder()
)


fun List<Utbetalingsperiode>.tilK9Perioder(): Map<Periode, ArbeidstidPeriodeInfo> {
    val perioder = mutableMapOf<Periode, ArbeidstidPeriodeInfo>()

    forEach {
        val faktiskArbeidTimerPerDag = (it.antallTimerPlanlagt?.minus(it.antallTimerBorte)) ?: Duration.ofHours(0)
        perioder[Periode(it.fraOgMed, it.tilOgMed)] = ArbeidstidPeriodeInfo(faktiskArbeidTimerPerDag)
    }

    return perioder
}