package no.nav.omsorgspengerutbetaling.felles

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation

data class Bekreftelser(
    val harBekreftetOpplysninger: JaNei,
    val harForståttRettigheterOgPlikter: JaNei
)

/**
 * Unngå `Boolean` default-verdi null -> false
 */
enum class JaNei (@get:JsonValue val boolean: Boolean) {
    Ja(true),
    Nei(false);

    companion object {
        @JsonCreator
        @JvmStatic
        fun fraBoolean(boolean: Boolean?) = when(boolean) {
            true -> Ja
            false -> Nei
            else -> throw IllegalStateException("Kan ikke være null")
        }
    }
}

internal fun Bekreftelser.valider() : Set<Violation> {
    val violations = mutableSetOf<Violation>()

    if (harBekreftetOpplysninger != JaNei.Ja) {
        violations.add(
            Violation(
                parameterName = "bekreftlser.harBekreftetOpplysninger",
                parameterType = ParameterType.ENTITY,
                reason = "Må besvars Ja.",
                invalidValue = harBekreftetOpplysninger
            )
        )
    }

    if (harForståttRettigheterOgPlikter != JaNei.Ja) {
        violations.add(
            Violation(
                parameterName = "bekreftelser.harForståttRettigheterOgPlikter",
                parameterType = ParameterType.ENTITY,
                reason = "Må besvars Ja.",
                invalidValue = harForståttRettigheterOgPlikter
            )
        )
    }

    return violations
}
