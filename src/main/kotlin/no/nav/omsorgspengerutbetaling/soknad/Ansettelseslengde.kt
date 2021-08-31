package no.nav.omsorgspengerutbetaling.soknad

import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.omsorgspengerutbetaling.soknad.Ansettelseslengde.Begrunnelse.INGEN_AV_SITUASJONENE

data class Ansettelseslengde(
    val merEnn4Uker: Boolean,
    val begrunnelse: Begrunnelse? = null,
    val ingenAvSituasjoneneForklaring: String? = null
) {
    enum class Begrunnelse {
        ANNET_ARBEIDSFORHOLD,
        ANDRE_YTELSER,
        LOVBESTEMT_FERIE_ELLER_ULØNNET_PERMISJON,
        MILITÆRTJENESTE,
        INGEN_AV_SITUASJONENE
    }
}

fun Ansettelseslengde.valider(felt: String) = mutableSetOf<Violation>().apply {
    if (merEnn4Uker == false && begrunnelse == null) {
        add(
            Violation(
                parameterType = ParameterType.ENTITY,
                parameterName = "${felt}.begrunnelse",
                reason = "Begrunnelse kan ikke være null, dersom merEnn4Uker er satt til false.",
                invalidValue = begrunnelse
            )
        )
    }

    if (begrunnelse == INGEN_AV_SITUASJONENE && ingenAvSituasjoneneForklaring.isNullOrBlank()) {
        add(
            Violation(
                parameterType = ParameterType.ENTITY,
                parameterName = "${felt}.ingenAvSituasjoneneForklaring",
                reason = "Forklaring for ingen av situasjonene kan ikke være null/tom, dersom begrunnelsen er INGEN_AV_SITUASJONENE",
                invalidValue = ingenAvSituasjoneneForklaring
            )
        )
    }
}
