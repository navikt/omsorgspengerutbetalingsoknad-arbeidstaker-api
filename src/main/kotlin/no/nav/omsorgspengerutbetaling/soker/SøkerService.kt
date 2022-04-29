package no.nav.omsorgspengerutbetaling.soker

import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.omsorgspengerutbetaling.general.CallId

class SøkerService (
    private val søkerGateway: SøkerGateway
) {
    suspend fun getSoker(
        idToken: IdToken,
        callId: CallId
    ): Søker {
        return søkerGateway.hentSoker(idToken, callId)
    }

}