package no.nav.omsorgspengerutbetaling.soknad

import no.nav.helse.dusseldorf.ktor.core.*
import no.nav.k9.søknad.ytelse.omsorgspenger.v1.OmsorgspengerUtbetaling
import no.nav.k9.søknad.ytelse.omsorgspenger.v1.OmsorgspengerUtbetalingValidator
import no.nav.omsorgspengerutbetaling.arbeidsgiver.valider
import no.nav.omsorgspengerutbetaling.felles.valider
import no.nav.omsorgspengerutbetaling.vedlegg.Vedlegg
import java.net.URL
import no.nav.k9.søknad.Søknad as K9Søknad

internal fun Søknad.valider(k9Format: K9Søknad) {
    val violations = mutableSetOf<Violation>().apply {
        addAll(arbeidsgivere.valider())
        addAll(opphold.valider("opphold"))
        addAll(bosteder.valider("bosteder"))
        addAll(bekreftelser.valider())
        addAll(k9Format.valider())
    }

    if (violations.isNotEmpty()) {
        throw Throwblem(ValidationProblemDetails(violations))
    }
}

private fun K9Søknad.valider() = OmsorgspengerUtbetalingValidator().valider(getYtelse<OmsorgspengerUtbetaling>()).map {
    Violation(
        parameterName = it.felt,
        parameterType = ParameterType.ENTITY,
        reason = it.feilmelding,
        invalidValue = "k9-format feilkode: ${it.feilkode}"
    )
}

internal fun List<Vedlegg>.validerVedlegg(vedleggUrler: List<URL>) {
    if (size != vedleggUrler.size) {
        throw Throwblem(
            ValidationProblemDetails(
                violations = setOf(
                    Violation(
                        parameterName = "vedlegg",
                        parameterType = ParameterType.ENTITY,
                        reason = "Mottok referanse til ${vedleggUrler.size} vedlegg, men fant kun $size vedlegg.",
                        invalidValue = vedleggUrler
                    )
                )
            )
        )
    }
    validerTotalStorresle()
}

private fun List<Vedlegg>.validerTotalStorresle() {
    val totalSize = sumBy { it.content.size }
    if (totalSize > MAX_VEDLEGG_SIZE) {
        throw Throwblem(vedleggTooLargeProblemDetails)
    }
}

private const val MAX_VEDLEGG_SIZE = 24 * 1024 * 1024 // 3 vedlegg på 8 MB
private val vedleggTooLargeProblemDetails = DefaultProblemDetails(
    title = "attachments-too-large",
    status = 413,
    detail = "Totale størreslsen på alle vedlegg overstiger maks på 24 MB."
)