package no.nav.omsorgspengerutbetaling.soknad

import no.nav.omsorgspengerutbetaling.felles.formaterStatuslogging
import no.nav.omsorgspengerutbetaling.general.CallId
import no.nav.omsorgspengerutbetaling.general.auth.IdToken
import no.nav.omsorgspengerutbetaling.k9format.tilK9Format
import no.nav.omsorgspengerutbetaling.kafka.KafkaProducer
import no.nav.omsorgspengerutbetaling.kafka.Metadata
import no.nav.omsorgspengerutbetaling.soker.Søker
import no.nav.omsorgspengerutbetaling.soker.SøkerService
import no.nav.omsorgspengerutbetaling.soker.validate
import no.nav.omsorgspengerutbetaling.vedlegg.DokumentEier
import no.nav.omsorgspengerutbetaling.vedlegg.VedleggService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.ZoneOffset
import java.time.ZonedDateTime

internal class SøknadService(
    private val søkerService: SøkerService,
    private val vedleggService: VedleggService,
    private val k9MellomLagringIngress: URI,
    private val kafkaProducer: KafkaProducer
) {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(SøknadService::class.java)
    }

    internal suspend fun registrer(
        søknad: Søknad,
        idToken: IdToken,
        callId: CallId,
        metadata: Metadata
    ) {
        logger.info(formaterStatuslogging(søknad.søknadId, "registreres"))

        val søker = søkerService.getSoker(idToken, callId)
        søker.validate()

        logger.info("Mapper om til K9Format")
        val mottatt = ZonedDateTime.now(ZoneOffset.UTC)
        val k9Format = søknad.tilK9Format(mottatt, søker)

        søknad.valider(k9Format)

        if(søknad.harVedlegg()) validerOgPersisterVedlegg(søknad, callId, idToken, søker)

        val komplettSøknad = søknad.tilKomplettSøknad(søker, k9Format, mottatt, k9MellomLagringIngress)

        try {
            kafkaProducer.produserKafkaMelding(komplettSøknad, metadata)
        } catch (exception: Exception) {
            logger.info("Feilet ved å legge melding på Kafka.")
            if(søknad.harVedlegg()){
                logger.info("Fjerner hold på persisterte vedlegg")
                vedleggService.fjernHoldPåPersistertVedlegg(søknad.vedlegg, callId, DokumentEier(søker.fødselsnummer))
            }
            throw MeldingRegistreringFeiletException("Feilet ved å legge melding på Kafka")
        }
    }

    private suspend fun validerOgPersisterVedlegg(søknad: Søknad, callId: CallId, idToken: IdToken, søker: Søker){
        logger.info("Validerer ${søknad.vedlegg.size} vedlegg.")
        val vedleggHentet = vedleggService.hentVedlegg(
            idToken = idToken,
            vedleggUrls = søknad.vedlegg,
            callId = callId,
            eier = DokumentEier(søker.fødselsnummer)
        )
        vedleggHentet.validerVedlegg(søknad.vedlegg)

        logger.info("Persisterer vedlegg")
        vedleggService.persisterVedlegg(søknad.vedlegg, callId, DokumentEier(søker.fødselsnummer))
    }
}

class MeldingRegistreringFeiletException(s: String) : Throwable(s)