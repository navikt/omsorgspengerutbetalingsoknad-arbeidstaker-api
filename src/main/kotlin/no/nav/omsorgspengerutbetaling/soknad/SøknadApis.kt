package no.nav.omsorgspengerutbetaling.soknad

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.omsorgspengerutbetaling.general.auth.IdTokenProvider
import no.nav.omsorgspengerutbetaling.general.getCallId
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("nav.soknadApis")

@KtorExperimentalLocationsAPI
internal fun Route.arbeidstakerutbetalingsøknadApis(
    søknadService: SøknadService,
    idTokenProvider: IdTokenProvider
) {

    @Location("/soknad")
    class sendSoknadUtbetalingArbeidstaker

    post { _ : sendSoknadUtbetalingArbeidstaker ->
        logger.trace("Mottatt ny søknad om arbeidstakerutbetaling. Mapper søknad.")
        val søknad = call.receive<Søknad>()
        søknad.valider()
        logger.trace("Validering OK. Registrerer søknad.")

        søknadService.registrer(
            søknad = søknad,
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
        logger.info("Validerer søknad...")
        søknad.valider()
        logger.trace("Validering OK.")
        call.respond(HttpStatusCode.Accepted)
    }
}
