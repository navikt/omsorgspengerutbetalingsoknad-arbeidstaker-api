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
    UTØVDE_VERNEPLIKT,
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

    when(utbetalingsårsak){
        Utbetalingsårsak.ARBEIDSGIVER_KONKURS -> null
        Utbetalingsårsak.KONFLIKT_MED_ARBEIDSGIVER -> {
            if(konfliktForklaring.isNullOrBlank()){
                violations.add(
                    Violation(
                        parameterName = "konfliktForklaring",
                        parameterType = ParameterType.ENTITY,
                        reason = "ArbeidsgiverDetaljer.konfliktForklaring må være satt dersom Utbetalingsårsak=KONFLIKT_MED_ARBEIDSGIVER",
                        invalidValue = konfliktForklaring
                    )
                )
            }
        }
        Utbetalingsårsak.NYOPPSTARTET_HOS_ARBEIDSGIVER -> {
            if(årsakNyoppstartet == null){
                violations.add(
                    Violation(
                        parameterName = "årsakNyoppstartet",
                        parameterType = ParameterType.ENTITY,
                        reason = "ArbeidsgiverDetaljer.årsakNyoppstartet må være satt dersom Utbetalingsårsak=NYOPPSTARTET_HOS_ARBEIDSGIVER",
                        invalidValue = årsakNyoppstartet
                    )
                )
            }
        }
    }

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

    return violations
}