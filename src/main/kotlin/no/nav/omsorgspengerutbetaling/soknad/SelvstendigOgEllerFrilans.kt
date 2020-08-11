package no.nav.omsorgspengerutbetaling.soknad

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation

const val SELVSTENDIG = "selvstendig"
const val FRILANS = "frilans"

typealias SelvstendigAndOrFrilans = List<String>

fun SelvstendigAndOrFrilans.validerSelvstendigAndOrFrilans(): Set<Violation> {
    val violations = mutableSetOf<Violation>()

    mapIndexed { index, selvstendigOgEllerFrilansValue: String ->
        if (selvstendigOgEllerFrilansValue != SELVSTENDIG && selvstendigOgEllerFrilansValue != FRILANS) {
            violations.add(
                Violation(
                    parameterName = "selvstendigOgEllerFrilans[$index]",
                    parameterType = ParameterType.ENTITY,
                    reason = "Ugyldig verdi for andre selvstendigOgEllerFrilans. Kun 'selvstendig' og 'frilans' er tillatt.",
                    invalidValue = selvstendigOgEllerFrilansValue
                )
            )
        }
    }

    return violations
}
