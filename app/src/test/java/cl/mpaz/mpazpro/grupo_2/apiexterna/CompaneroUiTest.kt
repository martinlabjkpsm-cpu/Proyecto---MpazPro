package cl.mpaz.mpazpro.grupo_2.apiexterna

import cl.mpaz.mpazpro.grupo_2.domain.model.Companeros
import cl.mpaz.mpazpro.grupo_2.model.Pokemon
import cl.mpaz.mpazpro.grupo_2.model.Type
import cl.mpaz.mpazpro.grupo_2.model.Types
import cl.mpaz.mpazpro.grupo_2.ui.viewmodels.formatMedidas
import cl.mpaz.mpazpro.grupo_2.ui.viewmodels.formatTitle
import cl.mpaz.mpazpro.grupo_2.ui.viewmodels.toUi
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

// Conversión del modelo de PokéAPI a lo que muestra la app, y reglas de desbloqueo. Herramientas: Kotest.
class CompaneroUiTest : StringSpec({

    "convierte a Pikachu en un compañero listo para mostrar, en español" {
        val ui = PokemonDatos.pikachu.toUi()
        ui.titulo shouldBe "#25 - Pikachu"
        ui.imageUrl shouldBe "https://img/arte/25.png"         // prefiere la ilustración oficial
        ui.tipos shouldContainExactly listOf("Eléctrico")
        ui.medidas shouldBe "Mide 40 cm y pesa 6 kg"
    }

    "ordena los tipos por slot y deja un tipo desconocido con mayúscula" {
        val p = Pokemon(id = 1, name = "bulbasaur",
            types = listOf(Types(2, Type("poison")), Types(1, Type("grass")), Types(3, Type("stellar"))))
        p.toUi().tipos shouldContainExactly listOf("Planta", "Veneno", "Stellar")
    }

    "los datos que faltan no rompen la tarjeta" {
        val ui = Pokemon(sprites = null, types = null).toUi()
        ui.titulo shouldBe "Pokémon desconocido"
        ui.imageUrl shouldBe null
        ui.medidas shouldBe "Mide — y pesa —"
        formatTitle(25, null) shouldBe "#25 - Pokémon desconocido"
        formatTitle(null, "mr-mime") shouldBe "Mr Mime"
    }

    "el peso con decimales usa coma" {
        formatMedidas(7, 69) shouldBe "Mide 70 cm y pesa 6,9 kg"
    }

    "sin medallas tiene un compañero y cada medalla suma uno, hasta el máximo" {
        Companeros.desbloqueados(0) shouldContainExactly listOf(25)
        Companeros.desbloqueados(2) shouldContainExactly listOf(25, 133, 1)
        Companeros.faltan(2) shouldBe 7
        Companeros.desbloqueados(50) shouldHaveSize Companeros.ORDEN.size
    }
})
