package cl.mpaz.mpazpro.grupo_2.navigation

import cl.mpaz.mpazpro.grupo_2.domain.model.Rol


object Rutas {
    const val LOGIN = "login"

    const val ESTUDIANTE = "estudiante"
    const val ACTIVIDAD = "estudiante/actividad/{contenidoId}/{tipo}"
    fun actividad(contenidoId: Int, tipo: String) = "estudiante/actividad/$contenidoId/$tipo"

    const val DOCENTE = "docente"
    const val ASIGNAR = "docente/asignar/{contenidoId}"
    fun asignar(contenidoId: Int) = "docente/asignar/$contenidoId"
    const val INFORME = "docente/informe/{contenidoId}"
    fun informe(contenidoId: Int) = "docente/informe/$contenidoId"
    const val CONTENIDO_FORM = "docente/contenido/{contenidoId}"
    fun contenidoForm(contenidoId: Int = 0) = "docente/contenido/$contenidoId"   // 0 = nuevo

    const val UTP = "utp"

    fun inicio(rol: Rol) = when (rol) {
        Rol.UTP -> UTP
        Rol.DOCENTE -> DOCENTE
        Rol.ESTUDIANTE -> ESTUDIANTE
    }
}