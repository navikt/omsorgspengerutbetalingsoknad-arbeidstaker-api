package no.nav.omsorgspengerutbetaling.soknad

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.omsorgspengerutbetaling.barn.BarnService
import no.nav.omsorgspengerutbetaling.general.auth.IdTokenProvider
import no.nav.omsorgspengerutbetaling.general.getCallId
import no.nav.omsorgspengerutbetaling.k9format.tilK9Format
import no.nav.omsorgspengerutbetaling.soker.Søker
import no.nav.omsorgspengerutbetaling.soker.SøkerService
import no.nav.omsorgspengerutbetaling.soker.validate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.ZoneOffset
import java.time.ZonedDateTime

private val logger: Logger = LoggerFactory.getLogger("nav.soknadApis")

@KtorExperimentalLocationsAPI
internal fun Route.arbeidstakerutbetalingsøknadApis(
    søknadService: SøknadService,
    søkerService: SøkerService,
    barnService: BarnService,
    idTokenProvider: IdTokenProvider
) {

    @Location("/soknad")
    class sendSoknadUtbetalingArbeidstaker

    post { _ : sendSoknadUtbetalingArbeidstaker ->
        logger.info("Mottatt ny søknad om arbeidstakerutbetaling. Mapper søknad.")
        val søknad = call.receive<Søknad>()
        val idToken = idTokenProvider.getIdToken(call)
        val callId = call.getCallId()
        val mottatt = ZonedDateTime.now(ZoneOffset.UTC)

        logger.trace("Henter søker")
        val søker: Søker = søkerService.getSoker(idToken = idToken, callId = callId)
        søker.validate()

        logger.info("Mapper om til K9Format")
        val k9Format = søknad.tilK9Format(mottatt, søker)

        logger.trace("Validerer")
        søknad.valider(k9Format)
        logger.trace("Validering OK. Registrerer søknad.")

        søknadService.registrer(
            søknad = søknad,
            søker = søker,
            k9Format = k9Format,
            callId = call.getCallId(),
            idToken = idTokenProvider.getIdToken(call)
        )

        logger.trace("Søknad registrert.")
        call.respond(HttpStatusCode.Accepted)
    }

    @Location("/valider/soknad")
    class validerSoknad

    post { _ : validerSoknad ->
        val søknad = call.receive<Søknad>()
        val idToken = idTokenProvider.getIdToken(call)
        val callId = call.getCallId()
        val mottatt = ZonedDateTime.now(ZoneOffset.UTC)

        logger.trace("Henter søker")
        val søker: Søker = søkerService.getSoker(idToken = idToken, callId = callId)
        søker.validate()

        logger.info("Mapper om til K9Format")
        val k9Format = søknad.tilK9Format(mottatt, søker)

        logger.info("Validerer søknad...")
        søknad.valider(k9Format)
        logger.trace("Validering OK.")
        call.respond(HttpStatusCode.Accepted)
    }
}
