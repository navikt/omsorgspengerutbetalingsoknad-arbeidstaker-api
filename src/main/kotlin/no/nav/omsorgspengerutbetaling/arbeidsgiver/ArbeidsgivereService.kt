package no.nav.omsorgspengerutbetaling.arbeidsgiver

import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.omsorgspengerutbetaling.general.CallId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate

class ArbeidsgivereService (
    private val arbeidsgivereGateway: ArbeidsgivereGateway
) {
    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(ArbeidsgivereService::class.java)
    }

    suspend fun getArbeidsgivere(
        idToken: IdToken,
        callId: CallId,
        fraOgMed: LocalDate,
        tilOgMed: LocalDate
    ) : Arbeidsgivere {
        return try {
            arbeidsgivereGateway.hentArbeidsgivere(idToken, callId, fraOgMed, tilOgMed)
        } catch (cause: Throwable) {
            logger.error("Feil ved henting av arbeidsgivere, returnerer en tom liste", cause)
            Arbeidsgivere(emptyList())
        }
    }
}
