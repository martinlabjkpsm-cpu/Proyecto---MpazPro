package cl.mpaz.mpazpro.data.local

import cl.mpaz.mpazpro.domain.model.Estado
import cl.mpaz.mpazpro.domain.model.Rol
import cl.mpaz.mpazpro.domain.model.TipoActividad.DESAFIO
import cl.mpaz.mpazpro.domain.model.TipoActividad.PRUEBA

// Datos ficticios que se cargan la primera vez que se abre la app.
// Una unidad por asignatura, como exige el caso. Contenidos de elaboración propia.

object DatosIniciales {

    suspend fun cargarSiEstaVacia(dao: AppDao) {
        if (dao.contarUsuarios() > 0) return

        dao.insertarUsuarios(
            listOf(
                UsuarioEntity(usuario = "utp01", pin = "1234", nombre = "Jefatura UTP", rol = Rol.UTP),
                UsuarioEntity(usuario = "docente01", pin = "1234", nombre = "Docente 3.º A", rol = Rol.DOCENTE),
                UsuarioEntity(usuario = "est01", pin = "1111", nombre = "Estudiante 01", rol = Rol.ESTUDIANTE),
                UsuarioEntity(usuario = "est02", pin = "1111", nombre = "Estudiante 02", rol = Rol.ESTUDIANTE)
            )
        )


        val ma = dao.insertarContenido(
            ContenidoEntity(
                asignatura = "Matemática", codigoOa = "MA03 OA 01",
                objetivo = "Contar números del 0 al 1 000 de 5 en 5, de 10 en 10 y de 100 en 100.",
                explicacion = "Contar de 10 en 10 es como subir una escalera donde cada peldaño suma 10: " +
                        "120, 130, 140. De 100 en 100 cambian las centenas: 300, 400, 500.",
                estado = Estado.APROBADO, asignado = true, maxIntentos = 3
            )
        ).toInt()
        dao.insertarPreguntas(
            listOf(
                PreguntaEntity(0, ma, DESAFIO, "¿Qué sigue? 130, 140, 150, ___", "151", "160", "250", 1, 1, "De 10 en 10: 150 + 10 = 160."),
                PreguntaEntity(0, ma, DESAFIO, "Cuenta de 5 en 5: 315, 320, 325, ___", "330", "326", "335", 0, 2, "Sumamos 5: 325 + 5 = 330."),
                PreguntaEntity(0, ma, DESAFIO, "Hacia atrás de 100 en 100: 870, 770, ___", "760", "670", "570", 1, 3, "Restamos 100: 770 - 100 = 670."),
                PreguntaEntity(0, ma, PRUEBA, "¿Qué sigue? 400, 500, 600, ___", "610", "700", "601", 1, 1, "De 100 en 100."),
                PreguntaEntity(0, ma, PRUEBA, "De 10 en 10: 482, 492, 502, ___", "512", "503", "602", 0, 2, "502 + 10 = 512."),
                PreguntaEntity(0, ma, PRUEBA, "¿Qué número falta? 255, 260, ___, 270", "261", "275", "265", 2, 3, "260 + 5 = 265.")
            )
        )


        val le = dao.insertarContenido(
            ContenidoEntity(
                asignatura = "Lenguaje", codigoOa = "LE03 OA 04",
                objetivo = "Comprender narraciones: personajes, ambiente y secuencia de acciones.",
                explicacion = "Cuento: Tomás vivía junto al mar. Una noche de niebla la luz del faro no se veía, " +
                        "así que tocó la campana y los pescadores llegaron a salvo.",
                estado = Estado.APROBADO
            )
        ).toInt()
        dao.insertarPreguntas(
            listOf(
                PreguntaEntity(0, le, DESAFIO, "¿Quién es el personaje principal?", "Un pescador", "Tomás", "El faro", 1, 1, "La historia trata de Tomás."),
                PreguntaEntity(0, le, DESAFIO, "¿Dónde ocurre la historia?", "Junto al mar", "En un bosque", "En la ciudad", 0, 2, "Vivía junto al mar."),
                PreguntaEntity(0, le, PRUEBA, "¿Qué problema hubo?", "Llovió", "Mucha niebla", "Hacía calor", 1, 1, "La niebla tapaba la luz."),
                PreguntaEntity(0, le, PRUEBA, "¿Cómo lo resolvió Tomás?", "Gritó", "Se fue", "Tocó la campana", 2, 2, "El sonido guió a los botes.")
            )
        )


        val cn = dao.insertarContenido(
            ContenidoEntity(
                asignatura = "Ciencias Naturales", codigoOa = "CN03 OA 08",
                objetivo = "Distinguir fuentes naturales y artificiales de luz.",
                explicacion = "Las fuentes naturales no las fabrican las personas (Sol, estrellas). " +
                        "Las artificiales sí (ampolleta, linterna).",
                estado = Estado.APROBADO
            )
        ).toInt()
        dao.insertarPreguntas(
            listOf(
                PreguntaEntity(0, cn, DESAFIO, "¿Cuál es una fuente natural?", "El Sol", "Una linterna", "Un celular", 0, 1, "Nadie fabricó el Sol."),
                PreguntaEntity(0, cn, PRUEBA, "¿Cuál es una fuente artificial?", "Una estrella", "Una ampolleta", "Un rayo", 1, 1, "La fabrican las personas.")
            )
        )


        val hi = dao.insertarContenido(
            ContenidoEntity(
                asignatura = "Historia", codigoOa = "HI03 OA 06",
                objetivo = "Ubicar lugares en una cuadrícula usando puntos cardinales.",
                explicacion = "El Sol sale por el Este y se esconde por el Oeste. En una cuadrícula, " +
                        "la letra indica la columna y el número la fila."
            )
        ).toInt()
        dao.insertarPreguntas(
            listOf(
                PreguntaEntity(0, hi, DESAFIO, "¿Por dónde sale el Sol?", "Este", "Oeste", "Sur", 0, 1, "Sale por el Este."),
                PreguntaEntity(0, hi, PRUEBA, "¿Qué es contrario al Norte?", "Este", "Sur", "Oeste", 1, 1, "Norte y Sur son opuestos.")
            )
        )
    }
}