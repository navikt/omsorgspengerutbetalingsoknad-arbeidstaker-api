package no.nav.omsorgspengerutbetaling.arbeidsgiver

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.omsorgspengerutbetaling.felles.Utbetalingsperiode
import no.nav.omsorgspengerutbetaling.felles.valider
import no.nav.omsorgspengerutbetaling.soknad.Ansettelseslengde
import no.nav.omsorgspengerutbetaling.soknad.valider

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
    val ansettelseslengde: Ansettelseslengde? = null, // TODO: 31/08/2021 Fjerne hele feltet når frontend er prodsatt
    val perioder: List<Utbetalingsperiode>,
    val utbetalingsårsak: Utbetalingsårsak? = null, // TODO: 31/08/2021 Fjerne nullable når frontend er prodsatt
    val konfliktForklaring: String? = null
)

enum class Utbetalingsårsak(){
    ARBEIDSGIVER_KONKURS,
    NYOPPSTARTET_HOS_ARBEIDSGIVER,
    KONFLIKT_MED_ARBEIDSGIVER
}

fun List<ArbeidsgiverDetaljer>.valider(): List<Violation> {
    val violations = mutableListOf<Violation>()

    forEachIndexed { index, arbeidsgiver -> violations.addAll(arbeidsgiver.valider(index)) }

    return violations
}

fun ArbeidsgiverDetaljer.valider(index: Int): List<Violation> {
    val violations = mutableListOf<Violation>()

    if(utbetalingsårsak != null){ // TODO: 02/09/2021 Sjekke fjernes etter at feltet er påbudt
        if(utbetalingsårsak == Utbetalingsårsak.KONFLIKT_MED_ARBEIDSGIVER && konfliktForklaring.isNullOrBlank()){
            violations.add(
                Violation(
                    parameterName = "konfliktForklaring",
                    parameterType = ParameterType.ENTITY,
                    reason = "Dersom utbetalingsårsak er KONFLIKT_MED_ARBEIDSGIVER må konfliktForklaring inneholde noe.",
                    invalidValue = konfliktForklaring
                )
            )
        }
    }
    ansettelseslengde?.let { violations.addAll(it.valider("arbeidsgivere[$index].ansettelseslengde")) } // TODO: 02/09/2021 Fjernes når ansettelselengde slettes
    violations.addAll(perioder.valider())

    return violations
}