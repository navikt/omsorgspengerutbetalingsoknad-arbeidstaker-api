package no.nav.omsorgspengerutbetaling.soknad

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.omsorgspengerutbetaling.felles.SØKNAD_URL
import no.nav.omsorgspengerutbetaling.felles.VALIDER_SØKNAD_URL
import no.nav.omsorgspengerutbetaling.felles.formaterStatuslogging
import no.nav.omsorgspengerutbetaling.general.CallId
import no.nav.omsorgspengerutbetaling.general.getCallId
import no.nav.omsorgspengerutbetaling.general.getMetadata
import no.nav.omsorgspengerutbetaling.k9format.tilK9Format
import no.nav.omsorgspengerutbetaling.soker.Søker
import no.nav.omsorgspengerutbetaling.soker.SøkerService
import no.nav.omsorgspengerutbetaling.soker.validate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZoneOffset
import java.time.ZonedDateTime

private val logger: Logger = LoggerFactory.getLogger("nav.soknadApis")

internal fun Route.arbeidstakerutbetalingsøknadApis(
    søknadService: SøknadService,
    søkerService: SøkerService,
    idTokenProvider: IdTokenProvider
) {

    post(SØKNAD_URL) {
        val søknad = call.receive<Søknad>()
        logger.info(formaterStatuslogging(søknad.søknadId, "mottatt"))

        val(idToken, callId) = call.hentIdTokenOgCallId(idTokenProvider)

        søknadService.registrer(
            søknad = søknad,
            callId = callId,
            idToken = idToken,
            metadata = call.getMetadata()
        )

        call.respond(HttpStatusCode.Accepted)
    }

    post(VALIDER_SØKNAD_URL) {
        val søknad = call.receive<Søknad>()
        val(idToken, callId) = call.hentIdTokenOgCallId(idTokenProvider)
        val mottatt = ZonedDateTime.now(ZoneOffset.UTC)

        val søker: Søker = søkerService.getSoker(idToken = idToken, callId = callId)
        søker.validate()

        logger.info("Mapper om til K9Format")
        val k9Format = søknad.tilK9Format(mottatt, søker)

        søknad.valider(k9Format)

        call.respond(HttpStatusCode.Accepted)
    }
}

private fun ApplicationCall.hentIdTokenOgCallId(idTokenProvider: IdTokenProvider): Pair<IdToken, CallId> =
    Pair(idTokenProvider.getIdToken(this), getCallId())