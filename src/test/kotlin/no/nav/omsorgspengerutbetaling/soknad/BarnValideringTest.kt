package no.nav.omsorgspengerutbetaling.soknad

import no.nav.helse.dusseldorf.ktor.core.Violation
import org.junit.Test
import kotlin.test.assertEquals

class BarnValideringTest {

    val gyldigBarn = Barn(
        identitetsnummer = "26104500284",
        navn = "Ole Dole",
        aleneOmOmsorgen = true,
        aktørId = null
    )

    val gyldigBarn2 = Barn(
        identitetsnummer = "26104500284",
        navn = "Bjarne",
        aleneOmOmsorgen = true,
        aktørId = null
    )

    @Test
    fun `Gyldig liste med barn gir null mangler`() {
        listOf(gyldigBarn, gyldigBarn2).valider().assertAntallMangler(0)
    }

    @Test
    fun `Barn hvor aleneOmOmsorg er null gir en feil`() {
        listOf(gyldigBarn.copy(aleneOmOmsorgen = null)).valider().assertAntallMangler(1)
    }

    @Test
    fun `Barn som har identitetsnummer som null gir en feil`() {
        listOf(gyldigBarn.copy(identitetsnummer = null)).valider().assertAntallMangler(1)
    }

    @Test
    fun `Barn som har blankt navn gir en feil`() {
        listOf(gyldigBarn.copy(navn = "  ")).valider().assertAntallMangler(1)
    }

    @Test
    fun `Liste med to barn hvor begge mangler identitetsnummer gir 2 feil`() {
        listOf(gyldigBarn.copy(identitetsnummer = null), gyldigBarn2.copy(identitetsnummer = null))
            .valider()
            .assertAntallMangler(2)
    }

}

private fun MutableSet<Violation>.assertAntallMangler(forventetAntallFeil: Int) {
    assertEquals(forventetAntallFeil, this.size)
}
