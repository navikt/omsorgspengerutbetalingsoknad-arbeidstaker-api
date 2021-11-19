package no.nav.omsorgspengerutbetaling.soknad

import io.ktor.http.*
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.k9.søknad.Søknad
import no.nav.omsorgspengerutbetaling.arbeidsgiver.ArbeidsgiverDetaljer
import no.nav.omsorgspengerutbetaling.felles.Bekreftelser
import no.nav.omsorgspengerutbetaling.felles.Bosted
import no.nav.omsorgspengerutbetaling.felles.Opphold
import no.nav.omsorgspengerutbetaling.felles.Språk
import no.nav.omsorgspengerutbetaling.soker.Søker
import java.net.URI
import java.net.URL
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
    fun tilKomplettSøknad(
        søker: Søker,
        k9Format: Søknad,
        mottatt: ZonedDateTime,
        k9MellomlagringIngress: URI
    ) = KomplettSøknad(
        søknadId = søknadId,
        språk = språk,
        mottatt = mottatt,
        søker = søker,
        bosteder = bosteder,
        opphold = opphold,
        arbeidsgivere = arbeidsgivere,
        bekreftelser = bekreftelser,
        vedleggUrls = vedlegg.tilK9MellomLagringUrl(k9MellomlagringIngress),
        hjemmePgaSmittevernhensyn = hjemmePgaSmittevernhensyn,
        hjemmePgaStengtBhgSkole = hjemmePgaStengtBhgSkole,
        k9Format = k9Format
    )

    fun harVedlegg() = vedlegg.isNotEmpty()
}

fun List<URL>.tilK9MellomLagringUrl(baseUrl: URI): List<URL> = map {
    val idFraUrl = it.path.substringAfterLast("/")
    Url.buildURL(
        baseUrl = baseUrl,
        pathParts = listOf(idFraUrl)
    ).toURL()
}