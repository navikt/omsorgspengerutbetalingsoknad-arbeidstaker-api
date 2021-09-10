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
    val navn: String,
    val organisasjonsnummer: String,
    val harHattFraværHosArbeidsgiver: Boolean,
    val arbeidsgiverHarUtbetaltLønn: Boolean,
    val perioder: List<Utbetalingsperiode>,
    val utbetalingsårsak: Utbetalingsårsak,
    val konfliktForklaring: String? = null,
    val årsakNyoppstartet: ÅrsakNyoppstartet? = null
)

enum class ÅrsakNyoppstartet{
    JOBBET_HOS_ANNEN_ARBEIDSGIVER,
    VAR_FRILANSER,
    VAR_SELVSTENDIGE,
    SØKTE_ANDRE_UTBETALINGER,
    ARBEID_I_UTLANDET,
    UTØVDE_MILITÆR,
    ANNET
}

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

    // TODO: 10/09/2021 Validering mot utbetalingsårsak. Konfliktforklaring og årsakNyoppstartet
    if(navn.isNullOrBlank()){
        violations.add(
            Violation(
                parameterName = "navn",
                parameterType = ParameterType.ENTITY,
                reason = "ArbeidsgiverDetaljer må ha navn satt.",
                invalidValue = navn
            )
        )
    }

    if(organisasjonsnummer.isNullOrBlank()){
        violations.add(
            Violation(
                parameterName = "organisasjonsnummer",
                parameterType = ParameterType.ENTITY,
                reason = "organisasjonsnummer må være satt.",
                invalidValue = organisasjonsnummer
            )
        )
    }

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