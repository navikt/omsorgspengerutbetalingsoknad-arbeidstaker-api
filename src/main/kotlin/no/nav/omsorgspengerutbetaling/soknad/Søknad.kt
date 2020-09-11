package no.nav.omsorgspengerutbetaling.soknad

import no.nav.omsorgspengerutbetaling.arbeidsgiver.ArbeidsgiverDetaljer
import no.nav.omsorgspengerutbetaling.felles.*
import java.net.URL

data class Søknad(
    val språk: Språk,
    val bosteder: List<Bosted>,
    val opphold: List<Opphold>,
    val arbeidsgivere: List<ArbeidsgiverDetaljer>,
    val bekreftelser: Bekreftelser,
    val andreUtbetalinger: List<String>,
    val erSelvstendig: JaNei = JaNei.Nei,
    val erFrilanser: JaNei = JaNei.Nei,
    val fosterbarn: List<FosterBarn>? = listOf(),
    val vedlegg: List<URL>,
    val hjemmePgaSmittevernhensyn: Boolean,
    val hjemmePgaStengtBhgSkole: Boolean? = null // TODO låses til JaNei etter lansering.
)
