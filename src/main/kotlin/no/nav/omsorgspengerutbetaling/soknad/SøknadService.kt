package no.nav.omsorgspengerutbetaling.soknad

import no.nav.omsorgspengerutbetaling.general.CallId
import no.nav.omsorgspengerutbetaling.general.auth.IdToken
import no.nav.omsorgspengerutbetaling.mottak.OmsorgpengesøknadMottakGateway
import no.nav.omsorgspengerutbetaling.soker.Søker
import no.nav.omsorgspengerutbetaling.soker.SøkerService
import no.nav.omsorgspengerutbetaling.vedlegg.DokumentEier
import no.nav.omsorgspengerutbetaling.vedlegg.Vedlegg
import no.nav.omsorgspengerutbetaling.vedlegg.VedleggService

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZoneOffset
import java.time.ZonedDateTime
import no.nav.k9.søknad.Søknad as K9Søknad

internal class SøknadService(
    private val omsorgpengesøknadMottakGateway: OmsorgpengesøknadMottakGateway,
    private val søkerService: SøkerService,
    private val vedleggService: VedleggService
) {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(SøknadService::class.java)
    }

    internal suspend fun registrer(
        søknad: Søknad,
        søker: Søker,
        k9Format: K9Søknad,
        idToken: IdToken,
        callId: CallId
    ) {
        logger.trace("Registrerer søknad.")

        logger.trace("Henter ${søknad.vedlegg.size} vedlegg.")
        val vedlegg: List<Vedlegg> = vedleggService.hentVedlegg(
            idToken = idToken,
            vedleggUrls = søknad.vedlegg,
            callId = callId,
            eier = DokumentEier(søker.fødselsnummer)
        )

        logger.trace("Vedlegg hentet. Validerer vedlegg.")
        vedlegg.validerVedlegg(søknad.vedlegg)
        logger.info("Vedlegg validert")

        logger.info("Legger søknad til prosessering")

        val komplettSøknad = KomplettSøknad(
            søknadId = søknad.søknadId,
            språk = søknad.språk,
            mottatt = ZonedDateTime.now(ZoneOffset.UTC),
            søker = søker,
            bosteder = søknad.bosteder,
            opphold = søknad.opphold,
            arbeidsgivere = søknad.arbeidsgivere,
            andreUtbetalinger = søknad.andreUtbetalinger,
            erSelvstendig = søknad.erSelvstendig,
            erFrilanser = søknad.erFrilanser,
            fosterbarn = søknad.fosterbarn,
            bekreftelser = søknad.bekreftelser,
            vedlegg = vedlegg,
            hjemmePgaSmittevernhensyn = søknad.hjemmePgaSmittevernhensyn,
            hjemmePgaStengtBhgSkole = søknad.hjemmePgaStengtBhgSkole,
            k9Format = k9Format
        )

        omsorgpengesøknadMottakGateway.leggTilProsessering(
            søknad = komplettSøknad,
            callId = callId
        )

        logger.trace("Søknad lagt til mottak.")
    }
}