package no.nav.omsorgspengerutbetaling.felles

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.helse.dusseldorf.ktor.core.ParameterType
import no.nav.helse.dusseldorf.ktor.core.Violation
import java.time.Duration
import java.time.LocalDate

private object Verktøy{

    internal const val JsonPath = "utbetalingsperioder"
}

internal fun Utbetalingsperiode.somPeriode() =
    Periode(
        fraOgMed = fraOgMed,
        tilOgMed = tilOgMed
    )

data class Utbetalingsperiode(
    @JsonFormat(pattern = "yyyy-MM-dd") val fraOgMed: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") val tilOgMed: LocalDate,
    val antallTimerBorte: Duration? = null,
    val antallTimerPlanlagt: Duration? = null,
    val årsak: FraværÅrsak? = null //Fjernes null og optional når feltet er prodsatt
)

enum class FraværÅrsak {
    STENGT_SKOLE_ELLER_BARNEHAGE,
    SMITTEVERNHENSYN,
    ORDINÆRT_FRAVÆR
}

internal fun List<Utbetalingsperiode>.valider() : Set<Violation> {
    val violations = mutableSetOf<Violation>()

    if (isEmpty()) {
        violations.add(
            Violation(
                parameterName = Verktøy.JsonPath,
                parameterType = ParameterType.ENTITY,
                reason = "Må settes minst en utbetalingsperiode.",
                invalidValue = this
            )
        )
    }
    forEach {
        if(it.antallTimerPlanlagt != null && it.antallTimerBorte == null){
            violations.add(
                Violation(
                    parameterName = "${Verktøy.JsonPath}[$it]",
                    parameterType = ParameterType.ENTITY,
                    reason = "Dersom antallTimerPlanlagt er satt så kan ikke antallTimerBorte være tom",
                    invalidValue = "antallTimerBorte = ${it.antallTimerBorte}, antallTimerPlanlagt=${it.antallTimerPlanlagt}"
                )
            )
        }

        if(it.antallTimerBorte != null && it.antallTimerPlanlagt == null){
            violations.add(
                Violation(
                    parameterName = "${Verktøy.JsonPath}[$it]",
                    parameterType = ParameterType.ENTITY,
                    reason = "Dersom antallTimerBorte er satt så kan ikke antallTimerPlanlagt være tom",
                    invalidValue = "antallTimerBorte = ${it.antallTimerBorte}, antallTimerPlanlagt=${it.antallTimerPlanlagt}"
                )
            )
        }

        if(it.antallTimerBorte != null && it.antallTimerPlanlagt != null){
            if(it.antallTimerBorte > it.antallTimerPlanlagt){
                violations.add(
                    Violation(
                        parameterName = "${Verktøy.JsonPath}[$it]",
                        parameterType = ParameterType.ENTITY,
                        reason = "Antall timer borte kan ikke være større enn antall timer planlagt jobbe",
                        invalidValue = "antallTimerBorte = ${it.antallTimerBorte}, antallTimerPlanlagt=${it.antallTimerPlanlagt}"
                    )
                )
            }
        }
    }

    val perioder = map { it.somPeriode() }
    violations.addAll(perioder.valider(Verktøy.JsonPath))

    return violations
}