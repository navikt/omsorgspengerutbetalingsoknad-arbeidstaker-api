package no.nav.omsorgspengerutbetaling.soknad

import no.nav.omsorgspengerutbetaling.felles.formaterStatuslogging
import no.nav.omsorgspengerutbetaling.general.CallId
import no.nav.omsorgspengerutbetaling.general.auth.IdToken
import no.nav.omsorgspengerutbetaling.k9format.tilK9Format
import no.nav.omsorgspengerutbetaling.mottak.OmsorgpengesøknadMottakGateway
import no.nav.omsorgspengerutbetaling.soker.SøkerService
import no.nav.omsorgspengerutbetaling.soker.validate
import no.nav.omsorgspengerutbetaling.vedlegg.DokumentEier
import no.nav.omsorgspengerutbetaling.vedlegg.Vedlegg
import no.nav.omsorgspengerutbetaling.vedlegg.VedleggService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZoneOffset
import java.time.ZonedDateTime

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
        idToken: IdToken,
        callId: CallId
    ) {
        logger.info(formaterStatuslogging(søknad.søknadId, "registreres"))

        val søker = søkerService.getSoker(idToken, callId)
        søker.validate()

        logger.info("Mapper om til K9Format")
        val mottatt = ZonedDateTime.now(ZoneOffset.UTC)
        val k9Format = søknad.tilK9Format(mottatt, søker)

        søknad.valider(k9Format)

        logger.info("Henter og validerer ${søknad.vedlegg.size} vedlegg.")
        val vedlegg: List<Vedlegg> = vedleggService.hentVedlegg(
            idToken = idToken,
            vedleggUrls = søknad.vedlegg,
            callId = callId,
            eier = DokumentEier(søker.fødselsnummer)
        )
        vedlegg.validerVedlegg(søknad.vedlegg)

        omsorgpengesøknadMottakGateway.leggTilProsessering(
            søknad = søknad.tilKomplettSøknad(søker, k9Format, vedlegg, mottatt),
            callId = callId
        )
    }
}