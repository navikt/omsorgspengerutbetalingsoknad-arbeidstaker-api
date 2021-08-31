package no.nav.omsorgspengerutbetaling.soknad

import no.nav.omsorgspengerutbetaling.arbeidsgiver.ArbeidsgiverDetaljer
import no.nav.omsorgspengerutbetaling.felles.*
import java.net.URL
import java.util.*

data class Søknad(
    val søknadId: String = UUID.randomUUID().toString(),
    val språk: Språk,
    val bosteder: List<Bosted>,
    val opphold: List<Opphold>,
    val arbeidsgivere: List<ArbeidsgiverDetaljer>,
    val bekreftelser: Bekreftelser,
    val andreUtbetalinger: List<String>? = null, // TODO: 31/08/2021 Hele feltet skal fjernes når frontend er prodsatt
    val erSelvstendig: JaNei = JaNei.Nei,
    val erFrilanser: JaNei = JaNei.Nei,
    val fosterbarn: List<FosterBarn>? = listOf(), // TODO: 31/08/2021 Hele feltet skal fjernes når frontend er prodsatt
    val vedlegg: List<URL>,
    val hjemmePgaSmittevernhensyn: Boolean? = null, //TODO 15.03.2021 - Fjernes når frontend er prodsatt
    val hjemmePgaStengtBhgSkole: Boolean? = null //TODO 15.03.2021 - Fjernes når frontend er prodsatt
)