package cl.mpaz.mpazpro.grupo_2.logica

import cl.mpaz.mpazpro.grupo_2.data.local.PreguntaEntity
import cl.mpaz.mpazpro.grupo_2.domain.model.CampoContenido
import cl.mpaz.mpazpro.grupo_2.domain.model.CampoPregunta
import cl.mpaz.mpazpro.grupo_2.domain.model.ContenidoFormEstado
import cl.mpaz.mpazpro.grupo_2.domain.model.PreguntaFormEstado
import cl.mpaz.mpazpro.grupo_2.domain.model.TipoActividad
import cl.mpaz.mpazpro.grupo_2.utils.Validaciones
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

// Reglas de validación de todos los formularios (login, asignación, revisión UTP, informe,
// contenido y preguntas). Herramienta: Kotest.

class ValidacionesTest : StringSpec({

    "usuario: obligatorio, mínimo 4 caracteres, solo minúsculas y números" {
        Validaciones.usuario("") shouldBe "Escribe tu usuario"
        Validaciones.usuario("est") shouldBe "El usuario tiene al menos 4 caracteres"
        Validaciones.usuario("Est01").shouldNotBeNull()
        Validaciones.usuario("est01").shouldBeNull()
    }

    "PIN: cuatro números" {
        Validaciones.pin("1234").shouldBeNull()
        Validaciones.pin("12a4") shouldBe "El PIN solo tiene números"
        Validaciones.pin("123") shouldBe "El PIN tiene 4 números"
    }

    "intentos de la prueba final: entre 1 y 5" {
        Validaciones.intentos("3").shouldBeNull()
        Validaciones.intentos("0").shouldNotBeNull()
        Validaciones.intentos("9").shouldNotBeNull()
        Validaciones.intentos("x") shouldBe "Ingresa solo números"
    }

    "observación UTP: obligatoria solo al rechazar" {
        Validaciones.observacion("", esRechazo = true).shouldNotBeNull()
        Validaciones.observacion("", esRechazo = false).shouldBeNull()
        Validaciones.observacion("Falta un ejemplo claro", esRechazo = true).shouldBeNull()
    }

    "el estudiante debe elegir una alternativa" {
        Validaciones.seleccion(null).shouldNotBeNull()
        Validaciones.seleccion(1).shouldBeNull()
    }

    "comentario y confirmación del informe docente" {
        Validaciones.comentario("") shouldBe "Escribe un comentario"
        Validaciones.comentario("Bien hecho, reforzar sumas").shouldBeNull()
        Validaciones.aceptacion(false).shouldNotBeNull()
        Validaciones.aceptacion(true).shouldBeNull()
    }

    val valido = ContenidoFormEstado(
        asignatura = "Matemática", unidad = "Unidad 1", eje = "Números", codigoOa = "MA03 OA 01",
        objetivo = "Contar de 10 en 10 hasta 1 000", explicacion = "Cada salto suma diez unidades al número.",
        recurso = "Currículum Nacional"
    )

    "un contenido completo no tiene errores" {
        Validaciones.contenido(valido).shouldBeEmpty()
    }

    "un objetivo muy corto marca error en ese campo" {
        Validaciones.contenido(valido.copy(objetivo = "corto")).keys shouldBe setOf(CampoContenido.OBJETIVO)
    }

    "el código OA debe tener el formato oficial y coincidir con la asignatura" {
        Validaciones.codigoOa("MA03 OA 01", "Matemática").shouldBeNull()
        Validaciones.codigoOa("LE03 OA 01", "Matemática").shouldNotBeNull()
        Validaciones.codigoOa("MA3 OA 1", "Matemática").shouldNotBeNull()
    }

    "solo una unidad por asignatura" {
        Validaciones.unidadUnica("Unidad 2", listOf("Unidad 1")).shouldNotBeNull()
        Validaciones.unidadUnica("unidad 1", listOf("Unidad 1", "")).shouldBeNull()
    }

    "una pregunta necesita correcta y alternativas distintas" {
        val errores = Validaciones.pregunta(
            PreguntaFormEstado(enunciado = "¿Qué sigue?", opcionA = "10", opcionB = "10", opcionC = "20", ayuda = "Suma diez")
        )
        errores shouldContainKey CampoPregunta.CORRECTA
        errores[CampoPregunta.OPCION_C] shouldBe "Las alternativas no pueden repetirse"
    }

    "un contenido necesita al menos un desafío y una pregunta de prueba final" {
        fun q(t: TipoActividad) = PreguntaEntity(0, 1, t, "x", "a", "b", "c", 0, 1, "y")
        Validaciones.preguntasMinimas(emptyList()) shouldBe "Agrega al menos un desafío"
        Validaciones.preguntasMinimas(listOf(q(TipoActividad.DESAFIO))).shouldNotBeNull()
        Validaciones.preguntasMinimas(listOf(q(TipoActividad.DESAFIO), q(TipoActividad.PRUEBA))).shouldBeNull()
    }
})
