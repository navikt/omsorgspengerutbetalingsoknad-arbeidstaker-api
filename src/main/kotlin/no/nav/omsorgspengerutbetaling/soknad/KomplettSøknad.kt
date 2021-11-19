package no.nav.omsorgspengerutbetaling.soknad

import no.nav.k9.søknad.Søknad
import no.nav.omsorgspengerutbetaling.arbeidsgiver.ArbeidsgiverDetaljer
import no.nav.omsorgspengerutbetaling.felles.Bekreftelser
import no.nav.omsorgspengerutbetaling.felles.Bosted
import no.nav.omsorgspengerutbetaling.felles.Opphold
import no.nav.omsorgspengerutbetaling.felles.Språk
import no.nav.omsorgspengerutbetaling.soker.Søker
import java.net.URL
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
    val vedleggUrls: List<URL>,
    val hjemmePgaSmittevernhensyn: Boolean,
    val hjemmePgaStengtBhgSkole: Boolean? = null,
    val k9Format: Søknad
)
