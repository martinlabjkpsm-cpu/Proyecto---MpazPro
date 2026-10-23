package cl.mpaz.mpazpro.utils

//validaciones separadas de la pantalla

object Validaciones {


    fun usuario(valor: String): String? = when {
        valor.isBlank() -> "Escribe tu usuario"
        valor.length < 4 -> "El usuario tiene al menos 4 caracteres"
        !valor.all { it.isLowerCase() || it.isDigit() } -> "solo minúsculas y números"
        else -> null
    }


    fun pin(valor: String): String? = when {
        valor.isBlank() -> "Escribe tu PIN"
        !valor.all { it.isDigit() } -> "El PIN solo tiene números"
        valor.length != 4 -> "El PIN tiene 4 números"
        else -> null
    }


    fun intentos(valor: String): String? {
        val numero = valor.toIntOrNull()
        return when {
            valor.isBlank() -> "Indica el máximo de intentos"
            numero == null -> "Ingresa solo números"
            numero !in 1..5 -> "Debe ser entre 1 y 5 intentos"
            else -> null
        }
    }


    fun observacion(valor: String, esRechazo: Boolean): String? = when {
        valor.length > 200 -> "Máximo 200 caracteres"
        esRechazo && valor.trim().length < 10 -> "Para rechazar, explica el motivo (mínimo 10 caracteres)"
        else -> null
    }

    fun seleccion(valor: Int?): String? =
        if (valor == null) "Elige una alternativa antes de continuar" else null


    fun comentario(valor: String): String? = when {
        valor.isBlank() -> "Escribe un comentario"
        valor.trim().length < 10 -> "Mínimo 10 caracteres"
        else -> null
    }


    fun aceptacion(marcado: Boolean): String? =
        if (!marcado) "Debes confirmar que revisaste el informe" else null
}