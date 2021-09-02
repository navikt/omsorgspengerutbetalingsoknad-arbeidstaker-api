package no.nav.omsorgspengerutbetaling.soknad

import no.nav.k9.søknad.Søknad
import no.nav.omsorgspengerutbetaling.arbeidsgiver.ArbeidsgiverDetaljer
import no.nav.omsorgspengerutbetaling.felles.Bekreftelser
import no.nav.omsorgspengerutbetaling.felles.Bosted
import no.nav.omsorgspengerutbetaling.felles.Opphold
import no.nav.omsorgspengerutbetaling.felles.Språk
import no.nav.omsorgspengerutbetaling.soker.Søker
import no.nav.omsorgspengerutbetaling.vedlegg.Vedlegg
import java.net.URL
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

data class Søknad(
    val søknadId: String = UUID.randomUUID().toString(),
    val språk: Språk,
    val bosteder: List<Bosted>,
    val opphold: List<Opphold>,
    val arbeidsgivere: List<ArbeidsgiverDetaljer>,
    val bekreftelser: Bekreftelser,
    val vedlegg: List<URL>,
    val hjemmePgaSmittevernhensyn: Boolean,
    val hjemmePgaStengtBhgSkole: Boolean? = null
) {
    fun tilKomplettSøknad(søker: Søker, k9Format: Søknad, vedlegg: List<Vedlegg>) = KomplettSøknad(
        søknadId = søknadId,
        språk = språk,
        mottatt = ZonedDateTime.now(ZoneOffset.UTC),
        søker = søker,
        bosteder = bosteder,
        opphold = opphold,
        arbeidsgivere = arbeidsgivere,
        bekreftelser = bekreftelser,
        vedlegg = vedlegg,
        hjemmePgaSmittevernhensyn = hjemmePgaSmittevernhensyn,
        hjemmePgaStengtBhgSkole = hjemmePgaStengtBhgSkole,
        k9Format = k9Format
    )
}