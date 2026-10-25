package cl.mpaz.mpazpro.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import cl.mpaz.mpazpro.domain.model.Estado
import cl.mpaz.mpazpro.domain.model.Rol
import cl.mpaz.mpazpro.domain.model.TipoActividad

//Tablas de sqlite

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuario: String,      // ej: "est01"
    val pin: String,          // MVP académico con PIN ficticio
    val nombre: String,       // nombre ficticio: "Estudiante 01"
    val rol: Rol
)


@Entity(tableName = "contenidos")
data class ContenidoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val asignatura: String,           // "Matemática"
    val unidad: String = "",          // NUEVO: "Unidad 1: Números hasta 1 000"
    val eje: String = "",             // NUEVO: eje curricular
    val codigoOa: String,             // código oficial: "MA03 OA 01"
    val objetivo: String,             // descripción del OA
    val explicacion: String,          // material de apoyo (elaboración propia)
    val ejemplo: String = "",         // NUEVO: ejemplo resuelto
    val recursoReferencia: String = "", // NUEVO: "Currículum Nacional (Mineduc)"
    val estado: Estado = Estado.PENDIENTE,
    val observacionUtp: String = "",
    val asignado: Boolean = false,    // el docente lo dejó visible para el curso
    val maxIntentos: Int = 3,         // intentos de la prueba final (1 a 5)
    val informeValidado: Boolean = false,
    val comentarioDocente: String = "",
    val fechaValidacion: Long? = null, // NUEVO: fecha en que el docente validó el informe
    val intentosAlValidar: Int = 0     // NUEVO: intentos que existían al validar
)

@Entity(tableName = "preguntas")
data class PreguntaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val contenidoId: Int,             // a qué OA pertenece
    val tipo: TipoActividad,          // DESAFIO o PRUEBA
    val enunciado: String,
    val opcionA: String,
    val opcionB: String,
    val opcionC: String,
    val correcta: Int,                // 0 = A, 1 = B, 2 = C
    val dificultad: Int,              // 1 a 3: los desafíos se muestran de menor a mayor
    val ayuda: String                 // retroalimentación al responder
) {
    fun opciones() = listOf(opcionA, opcionB, opcionC)
}


@Entity(tableName = "intentos")
data class IntentoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val estudianteId: Int,
    val contenidoId: Int,
    val tipo: TipoActividad,
    val correctas: Int,
    val total: Int,
    val fecha: Long = System.currentTimeMillis()   // NUEVO: cuándo se rindió
)


@Entity(tableName = "respuestas")
data class RespuestaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val intentoId: Int,
    val preguntaId: Int,
    val esCorrecta: Boolean,
    val opcionElegida: Int = -1       // NUEVO: 0 = A, 1 = B, 2 = C, -1 = sin responder
)