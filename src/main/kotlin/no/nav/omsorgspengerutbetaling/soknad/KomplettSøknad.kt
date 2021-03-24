package no.nav.omsorgspengerutbetaling.soknad

import no.nav.k9.søknad.Søknad
import no.nav.omsorgspengerutbetaling.arbeidsgiver.ArbeidsgiverDetaljer
import no.nav.omsorgspengerutbetaling.felles.*
import no.nav.omsorgspengerutbetaling.soker.Søker
import no.nav.omsorgspengerutbetaling.vedlegg.Vedlegg
import java.time.ZonedDateTime

data class KomplettSøknad(
    val søknadId: String,
    val språk: Språk,
    val mottatt: ZonedDateTime,
    val søker: Søker,
    val bosteder: List<Bosted>,
    val opphold: List<Opphold>,
    val arbeidsgivere: List<ArbeidsgiverDetaljer>,
    val bekreftelser: Bekreftelser,
    val andreUtbetalinger: List<String>,
    val erSelvstendig: JaNei,
    val erFrilanser: JaNei,
    val fosterbarn: List<FosterBarn>? = listOf(),
    val vedlegg: List<Vedlegg>,
    val hjemmePgaSmittevernhensyn: Boolean? = null, //TODO 15.03.2021 - Fjernes når frontend er prodsatt
    val hjemmePgaStengtBhgSkole: Boolean? = null, //TODO 15.03.2021 - Fjernes når frontend er prodsatt
    val k9Format: Søknad
)
