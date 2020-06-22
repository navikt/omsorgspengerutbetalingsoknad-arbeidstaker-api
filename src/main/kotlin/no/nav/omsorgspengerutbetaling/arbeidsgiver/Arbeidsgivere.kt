package no.nav.omsorgspengerutbetaling.arbeidsgiver

import no.nav.helse.dusseldorf.ktor.core.Violation
import no.nav.omsorgspengerutbetaling.felles.Utbetalingsperiode
import no.nav.omsorgspengerutbetaling.felles.validerUtenVedlegg
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
    val ansettelseslengde: Ansettelseslengde,
    val perioder: List<Utbetalingsperiode>
)

fun List<ArbeidsgiverDetaljer>.valider(vedlegg: List<URL>): List<Violation> =
    mapIndexed { index, arbeidsgiverDetaljer ->
        val violations = mutableSetOf<Violation>()
        violations.addAll(arbeidsgiverDetaljer.ansettelseslengde.valider(vedlegg, "arbeidsgivere[$index].ansettelseslengde"))
        violations.addAll(arbeidsgiverDetaljer.perioder.validerUtenVedlegg())
        violations
    }.flatMap { it }
