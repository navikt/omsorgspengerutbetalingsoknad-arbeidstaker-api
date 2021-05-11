package no.nav.omsorgspengerutbetaling.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.matching.AnythingPattern
import io.ktor.http.*
import no.nav.helse.dusseldorf.testsupport.wiremock.WireMockBuilder

internal const val k9OppslagPath = "/k9-selvbetjening-oppslag-mock"
private const val omsorgpengerutbetalingsoknadMottakPath = "/omsorgspengerutbetalingsoknad-arbeidstaker-mottak-mock"
private const val k9MellomlagringPath = "/k9-mellomlagring-mock/v1/dokument"

internal fun WireMockBuilder.omsorgspengesoknadApiConfig() = wireMockConfiguration {
    it
        .extensions(SokerResponseTransformer())
        .extensions(K9DokumentResponseTransformer())
}


internal fun WireMockServer.stubK9OppslagSoker() : WireMockServer {
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching("$k9OppslagPath/.*"))
            .withHeader(HttpHeaders.Authorization, AnythingPattern())
            .withQueryParam("a", equalTo("aktør_id"))
            .withQueryParam("a", equalTo("fornavn"))
            .withQueryParam("a", equalTo("mellomnavn"))
            .withQueryParam("a", equalTo("etternavn"))
            .withQueryParam("a", equalTo("fødselsdato"))
            .willReturn(
                WireMock.aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withTransformers("k9-oppslag-soker")
            )
    )
    return this
}

private fun WireMockServer.stubHealthEndpoint(
    path : String
) : WireMockServer{
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathMatching(".*$path")).willReturn(
            WireMock.aResponse()
                .withStatus(200)
        )
    )
    return this
}

internal fun WireMockServer.stubK9DokumentHealth() = stubHealthEndpoint("$k9MellomlagringPath/health")
internal fun WireMockServer.stubOmsorgspengerutbetalingsoknadMottakHealth() = stubHealthEndpoint("$omsorgpengerutbetalingsoknadMottakPath/health")
internal fun WireMockServer.stubOppslagHealth() = stubHealthEndpoint("$k9OppslagPath/health")

internal fun WireMockServer.stubLeggSoknadTilProsessering(path: String): WireMockServer{
    WireMock.stubFor(
        WireMock.post(WireMock.urlMatching(".*$omsorgpengerutbetalingsoknadMottakPath$path"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(202)
            )
    )
    return this
}

internal fun WireMockServer.stubK9Mellomlagring() : WireMockServer{
    WireMock.stubFor(
        WireMock.any(WireMock.urlMatching(".*$k9MellomlagringPath.*"))
            .willReturn(
                WireMock.aResponse()
                    .withTransformers("K9DokumentResponseTransformer")
            )
    )
    return this
}

internal fun WireMockServer.getK9OppslagUrl() = baseUrl() + k9OppslagPath
internal fun WireMockServer.getOmsorgpengerutbetalingsoknadMottakUrl() = baseUrl() + omsorgpengerutbetalingsoknadMottakPath
internal fun WireMockServer.getK9MellomlagringUrl() = baseUrl() + k9MellomlagringPath
