package no.nav.omsorgspengerutbetaling.arbeidsgiver

import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.omsorgspengerutbetaling.felles.Utbetalingsperiode
import no.nav.omsorgspengerutbetaling.felles.valider
import no.nav.omsorgspengerutbetaling.soknad.Ansettelseslengde
import no.nav.omsorgspengerutbetaling.soknad.valider
import java.net.URL

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

fun List<ArbeidsgiverDetaljer>.valider(vedlegg: List<URL>): List<Violation> =
    mapIndexed { index, arbeidsgiverDetaljer ->
        val violations = mutableSetOf<Violation>()
        arbeidsgiverDetaljer.ansettelseslengde?.let { violations.addAll(it.valider(vedlegg, "arbeidsgivere[$index].ansettelseslengde")) }
        violations.addAll(arbeidsgiverDetaljer.perioder.valider())
        violations

        // TODO: 31/08/2021 Skal det være validering dersom man velger konflikt? Feks at forklaring må være gitt
    }.flatMap { it }
