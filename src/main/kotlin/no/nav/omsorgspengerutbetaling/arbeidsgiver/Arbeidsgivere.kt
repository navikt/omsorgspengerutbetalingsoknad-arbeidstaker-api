package no.nav.omsorgspengerutbetaling.arbeidsgiver

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.omsorgspengerutbetaling.felles.Utbetalingsperiode
import no.nav.omsorgspengerutbetaling.felles.valider

data class ArbeidsgivereOppslagRespons(
    val arbeidsgivere: Arbeidsgivere
)

data class Arbeidsgivere(
    val organisasjoner: List<Organisasjon>
)

class Organisasjon(
    val organisasjonsnummer: String,
    val navn: String?
)

data class ArbeidsgiverDetaljer(
    val navn: String? = null,
    val organisasjonsnummer: String? = null,
    val harHattFraværHosArbeidsgiver: Boolean,
    val arbeidsgiverHarUtbetaltLønn: Boolean,
    val perioder: List<Utbetalingsperiode>,
    val utbetalingsårsak: Utbetalingsårsak,
    val konfliktForklaring: String? = null
)

enum class Utbetalingsårsak() {
    ARBEIDSGIVER_KONKURS,
    NYOPPSTARTET_HOS_ARBEIDSGIVER,
    KONFLIKT_MED_ARBEIDSGIVER
}

fun List<ArbeidsgiverDetaljer>.valider(): List<Violation> {
    val violations = mutableListOf<Violation>()

    forEach { arbeidsgiver -> violations.addAll(arbeidsgiver.valider()) }

    return violations
}

fun ArbeidsgiverDetaljer.valider(): List<Violation> {
    val violations = mutableListOf<Violation>()

    violations.addAll(perioder.valider())

    if (utbetalingsårsak == Utbetalingsårsak.KONFLIKT_MED_ARBEIDSGIVER && konfliktForklaring.isNullOrBlank()) {
        violations.add(
            Violation(
                parameterName = "konfliktForklaring",
                parameterType = ParameterType.ENTITY,
                reason = "Dersom utbetalingsårsak er KONFLIKT_MED_ARBEIDSGIVER må konfliktForklaring inneholde noe.",
                invalidValue = konfliktForklaring
            )
        )
    }

    return violations
}