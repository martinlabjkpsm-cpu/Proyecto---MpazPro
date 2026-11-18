package cl.mpaz.mpazpro.grupo_2.domain.model

// Qué compañeros Pokémon tiene el estudiante: todos parten con el primero y cada medalla suma uno más.
object Companeros {

    val ORDEN = listOf(25, 133, 1, 4, 7, 39, 52, 54, 143, 175)   // Pikachu, Eevee, Bulbasaur, ...

    fun desbloqueados(medallas: Int): List<Int> = ORDEN.take((1 + medallas).coerceIn(1, ORDEN.size))

    fun faltan(medallas: Int): Int = ORDEN.size - desbloqueados(medallas).size
}
