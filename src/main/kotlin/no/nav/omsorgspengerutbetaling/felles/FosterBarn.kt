package no.nav.omsorgspengerutbetaling.felles

import com.fasterxml.jackson.annotation.JsonAlias

data class FosterBarn(
    @JsonAlias("fødselsnummer")
    val identitetsnummer: String
)
