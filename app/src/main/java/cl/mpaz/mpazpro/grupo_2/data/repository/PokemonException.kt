package cl.mpaz.mpazpro.grupo_2.data.repository

/**
 * Errores que puede lanzar `PokemonRepository`. El ViewModel no sabe si el fallo vino de
 * Retrofit, OkHttp o Gson, solo de qué tipo es. Al ser `sealed`, el `when` debe cubrir todos.
 */
sealed class PokemonException(cause: Throwable? = null) : Exception(cause) {
    // Sin conexión, timeout o fallo de DNS.
    class Network(cause: Throwable) : PokemonException(cause)

    // No existe un Pokémon con ese número o nombre (404).
    class NotFound : PokemonException()

    // El servidor respondió con otro error (500…) o sin cuerpo. Guarda el código HTTP.
    class Server(val code: Int) : PokemonException()

    // Cualquier otro fallo, por ejemplo un JSON que Gson no puede leer.
    class Unknown(cause: Throwable) : PokemonException(cause)
}
