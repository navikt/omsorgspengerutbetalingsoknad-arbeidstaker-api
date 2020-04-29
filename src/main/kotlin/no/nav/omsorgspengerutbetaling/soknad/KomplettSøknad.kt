package no.nav.omsorgspengerutbetaling.soknad

import no.nav.helse.arbeidsgiver.ArbeidsgiverDetaljer
import no.nav.omsorgspengerutbetaling.felles.*
import no.nav.omsorgspengerutbetaling.soker.Søker
import no.nav.omsorgspengerutbetaling.vedlegg.Vedlegg
import java.time.ZonedDateTime

data class KomplettSøknad(
    val språk: Språk,
    val mottatt: ZonedDateTime,
    val søker: Søker,
    val bosteder: List<Bosted>,
    val opphold: List<Opphold>,
    val jobbHosNåværendeArbeidsgiver: JobbHosNåværendeArbeidsgiver,
    val arbeidsgivere: ArbeidsgiverDetaljer,
    val spørsmål: List<SpørsmålOgSvar>,
    val bekreftelser: Bekreftelser,
    val andreUtbetalinger: List<String>,
    val fosterbarn: List<FosterBarn>? = listOf(),
    val vedlegg: List<Vedlegg>
)
