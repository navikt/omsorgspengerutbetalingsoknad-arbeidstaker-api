package no.nav.omsorgspengerutbetaling.soker

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import io.ktor.http.*
import no.nav.helse.dusseldorf.ktor.auth.IdToken
import no.nav.helse.dusseldorf.ktor.client.buildURL
import no.nav.helse.dusseldorf.ktor.core.Retry
import no.nav.helse.dusseldorf.ktor.metrics.Operation
import no.nav.omsorgspengerutbetaling.general.CallId
import no.nav.omsorgspengerutbetaling.general.oppslag.K9OppslagGateway
import no.nav.omsorgspengerutbetaling.general.oppslag.throwable
import no.nav.omsorgspengerutbetaling.k9SelvbetjeningOppslagKonfigurert
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.Duration
import java.time.LocalDate

class SøkerGateway (
    baseUrl: URI
) : K9OppslagGateway(baseUrl) {

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger("nav.SokerGateway")
        private const val HENTE_SOKER_OPERATION = "hente-soker"
        private val objectMapper = k9SelvbetjeningOppslagKonfigurert()

        private val attributter = Pair("a", listOf("aktør_id", "fornavn", "mellomnavn", "etternavn", "fødselsdato"))
    }

    suspend fun hentSoker(
        idToken: IdToken,
        callId : CallId
    ) : Søker {
        val sokerUrl = Url.buildURL(
            baseUrl = baseUrl,
            pathParts = listOf("meg"),
            queryParameters = mapOf(
                attributter
            )
        ).toString()
        val httpRequest = generateHttpRequest(idToken, sokerUrl, callId)

        val oppslagRespons = Retry.retry(
            operation = HENTE_SOKER_OPERATION,
            initialDelay = Duration.ofMillis(200),
            factor = 2.0,
            logger = logger
        ) {
            val (request, _, result) = Operation.monitored(
                app = "omsorgspengesoknad-utbetaling-arbeidstaker-api",
                operation = HENTE_SOKER_OPERATION,
                resultResolver = { 200 == it.second.statusCode }
            ) { httpRequest.awaitStringResponseResult() }

            result.fold(
                { success -> objectMapper.readValue<SokerOppslagRespons>(success)},
                { error -> throw error.throwable(request, logger, "Feil ved henting av søkers personinformasjon")}
            )
        }
        return oppslagRespons.tilSøker(idToken.getNorskIdentifikasjonsnummer())
    }

    data class SokerOppslagRespons(
        val aktør_id: String,
        val fornavn: String,
        val mellomnavn: String?,
        val etternavn: String,
        val fødselsdato: LocalDate
    ) {
        fun tilSøker(fødselsnummer: String) = Søker(
            aktørId = aktør_id,
            fødselsnummer = fødselsnummer,
            fødselsdato = fødselsdato,
            fornavn = fornavn,
            mellomnavn = mellomnavn,
            etternavn = etternavn
        )
    }
}
