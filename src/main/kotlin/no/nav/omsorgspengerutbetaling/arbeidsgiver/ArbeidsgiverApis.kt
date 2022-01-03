package no.nav.omsorgspengerutbetaling.arbeidsgiver

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.helse.dusseldorf.ktor.auth.IdTokenProvider
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Throwblem
import no.nav.helse.dusseldorf.ktor.core.ValidationProblemDetails
import no.nav.omsorgspengerutbetaling.felles.FraOgMedTilOgMedValidator
import no.nav.omsorgspengerutbetaling.general.getCallId
import java.time.LocalDate

private const val fraOgMedQueryName = "fra_og_med"
private const val tilOgMedQueryName = "til_og_med"

fun Route.arbeidsgiverApis(
    arbeidsgivereService: ArbeidsgivereService,
    idTokenProvider: IdTokenProvider
) {

    get("/arbeidsgiver") {
        val violations = FraOgMedTilOgMedValidator.validate(
            fraOgMed = call.request.queryParameters[fraOgMedQueryName],
            tilOgMed = call.request.queryParameters[tilOgMedQueryName],
            parameterType = ParameterType.QUERY
        )

        if (violations.isNotEmpty()) {
            throw Throwblem(ValidationProblemDetails(violations))
        } else {
            call.respond(
                arbeidsgivereService.getArbeidsgivere(
                    idToken = idTokenProvider.getIdToken(call),
                    callId = call.getCallId(),
                    fraOgMed = LocalDate.parse(call.request.queryParameters[fraOgMedQueryName]),
                    tilOgMed = LocalDate.parse(call.request.queryParameters[tilOgMedQueryName])
                )
            )
        }
    }
}
