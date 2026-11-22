package cl.mpaz.mpazpro.grupo_2.microservicios

import cl.mpaz.mpazpro.grupo_2.data.local.ContenidoEntity
import cl.mpaz.mpazpro.grupo_2.data.local.Preferencias
import cl.mpaz.mpazpro.grupo_2.data.local.PreguntaEntity
import cl.mpaz.mpazpro.grupo_2.data.remote.microservicios.ContenidoDto
import cl.mpaz.mpazpro.grupo_2.data.remote.microservicios.ContenidosApi
import cl.mpaz.mpazpro.grupo_2.data.remote.microservicios.IntentoDto
import cl.mpaz.mpazpro.grupo_2.data.remote.microservicios.IntentoRequest
import cl.mpaz.mpazpro.grupo_2.data.remote.microservicios.PreguntaDto
import cl.mpaz.mpazpro.grupo_2.data.remote.microservicios.RespuestaDto
import cl.mpaz.mpazpro.grupo_2.data.remote.microservicios.ResultadosApi
import cl.mpaz.mpazpro.grupo_2.data.remote.microservicios.aDto
import cl.mpaz.mpazpro.grupo_2.data.remote.microservicios.aEntidad
import cl.mpaz.mpazpro.grupo_2.data.repository.MPazRepository
import cl.mpaz.mpazpro.grupo_2.domain.model.Estado
import cl.mpaz.mpazpro.grupo_2.domain.model.TipoActividad
import cl.mpaz.mpazpro.grupo_2.soporte.FakeDao
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import java.io.IOException

// Integración con los microservicios Spring Boot (API interna), sin red:
// - Room: FakeDao en memoria.
// - ms-contenidos y ms-resultados: interfaces de Retrofit simuladas con MockK.
// Herramientas: Kotest + MockK (coEvery / coVerify) + kotlinx-coroutines-test (runTest).

class MicroserviciosRepositoryTest : StringSpec({

    val sinRed = IOException("Sin conexión")

    fun contenidoDto(id: Int, asignatura: String, estado: Estado = Estado.APROBADO) = ContenidoDto(
        id = id, asignatura = asignatura, unidad = "Unidad 1", codigoOa = "OA $id",
        objetivo = "Objetivo $id", explicacion = "Explicación $id", estado = estado, asignado = true
    )

    fun preguntaDto(id: Int, contenidoId: Int, tipo: TipoActividad) = PreguntaDto(
        id = id, contenidoId = contenidoId, tipo = tipo, enunciado = "P$id",
        opcionA = "a", opcionB = "b", opcionC = "c", correcta = 1, dificultad = 1, ayuda = "ayuda"
    )

    fun pregunta(id: Int, correcta: Int) =
        PreguntaEntity(id, 1, TipoActividad.PRUEBA, "P$id", "a", "b", "c", correcta, 1, "ayuda")

    fun nuevoContenido() = ContenidoEntity(
        asignatura = "Matemática", unidad = "Unidad 1", codigoOa = "MA03 OA 02",
        objetivo = "Leer números", explicacion = "Centenas, decenas y unidades"
    )

    class Entorno {
        val dao = FakeDao()
        val contenidosApi = mockk<ContenidosApi>()
        val resultadosApi = mockk<ResultadosApi>()
        val repo = MPazRepository(dao, mockk<Preferencias>(relaxed = true), contenidosApi, resultadosApi)
    }

    "al iniciar sesión se descargan contenidos, preguntas e intentos del servidor a Room" {
        runTest {
            val e = Entorno()
            coEvery { e.contenidosApi.listar() } returns listOf(contenidoDto(10, "Matemática"), contenidoDto(11, "Lenguaje"))
            coEvery { e.contenidosApi.preguntas(10) } returns listOf(preguntaDto(100, 10, TipoActividad.DESAFIO), preguntaDto(101, 10, TipoActividad.PRUEBA))
            coEvery { e.contenidosApi.preguntas(11) } returns listOf(preguntaDto(110, 11, TipoActividad.PRUEBA))
            coEvery { e.resultadosApi.listar() } returns listOf(
                IntentoDto(5, 3, 10, TipoActividad.PRUEBA, 1, 2, 1000L,
                    listOf(RespuestaDto(50, 5, 101, true, 1), RespuestaDto(51, 5, 100, false, 0)))
            )

            e.repo.iniciarSesion("est01", "1111")

            // Los datos ficticios locales se reemplazan por los del servidor, con sus ids
            e.dao.todosLosContenidos().map { it.id } shouldContainExactly listOf(10, 11)
            e.dao.todasLasPreguntasGuardadas().map { it.id }.sorted() shouldContainExactly listOf(100, 101, 110)
            e.dao.todosLosIntentos().single().id shouldBe 5
            e.dao.todasLasRespuestas().map { it.intentoId }.distinct() shouldContainExactly listOf(5)
            e.repo.enLinea.value shouldBe true
        }
    }

    "si el servidor no responde, Room se mantiene y la app sigue sin conexión" {
        runTest {
            val e = Entorno()
            coEvery { e.contenidosApi.listar() } throws sinRed

            e.repo.iniciarSesion("est01", "1111")?.usuario shouldBe "est01"

            e.dao.todosLosContenidos() shouldHaveSize 4          // los 4 contenidos ficticios siguen ahí
            e.repo.enLinea.value shouldBe false
            e.repo.sincronizar() shouldBe false
        }
    }

    "sin microservicios configurados, sincronizar no hace nada" {
        runTest {
            val repo = MPazRepository(FakeDao(), mockk(relaxed = true))
            repo.sincronizar() shouldBe false
        }
    }

    "crear un contenido lo envía por POST y lo guarda en Room con el id del servidor" {
        runTest {
            val e = Entorno()
            val enviado = slot<ContenidoDto>()
            coEvery { e.contenidosApi.crear(capture(enviado)) } answers { enviado.captured.copy(id = 42) }

            val id = e.repo.guardarContenido(nuevoContenido())

            id shouldBe 42
            enviado.captured.id shouldBe null                    // el id lo asigna el servidor
            enviado.captured.estado shouldBe Estado.PENDIENTE
            e.dao.buscarContenido(42)?.codigoOa shouldBe "MA03 OA 02"
        }
    }

    "editar un contenido lo envía por PUT" {
        runTest {
            val e = Entorno()
            val id = e.dao.insertarContenido(nuevoContenido()).toInt()
            val editado = e.dao.buscarContenido(id)!!.copy(objetivo = "Nuevo objetivo")
            coEvery { e.contenidosApi.actualizar(id, any()) } answers { secondArg() }

            e.repo.guardarContenido(editado) shouldBe id

            coVerify { e.contenidosApi.actualizar(id, match { it.objetivo == "Nuevo objetivo" }) }
            e.dao.buscarContenido(id)?.objetivo shouldBe "Nuevo objetivo"
        }
    }

    "sin conexión, el contenido nuevo se guarda solo en Room" {
        runTest {
            val e = Entorno()
            coEvery { e.contenidosApi.crear(any()) } throws sinRed

            val id = e.repo.guardarContenido(nuevoContenido())

            e.dao.buscarContenido(id)?.asignatura shouldBe "Matemática"
            e.repo.enLinea.value shouldBe false
        }
    }

    "la UTP aprueba: el cambio llega al servidor y a Room" {
        runTest {
            val e = Entorno()
            val id = e.dao.insertarContenido(nuevoContenido()).toInt()
            coEvery { e.contenidosApi.actualizar(any(), any()) } answers { secondArg() }
            val aprobado = e.dao.buscarContenido(id)!!.copy(estado = Estado.APROBADO)

            e.repo.actualizar(aprobado)

            coVerify { e.contenidosApi.actualizar(id, match { it.estado == Estado.APROBADO }) }
            e.dao.buscarContenido(id)?.estado shouldBe Estado.APROBADO
        }
    }

    "volver a revisión deja el contenido PENDIENTE y sin asignar también en el servidor" {
        runTest {
            val e = Entorno()
            val id = e.dao.insertarContenido(nuevoContenido().copy(estado = Estado.APROBADO, asignado = true)).toInt()
            coEvery { e.contenidosApi.actualizar(any(), any()) } answers { secondArg() }

            e.repo.volverARevision(id)

            coVerify { e.contenidosApi.actualizar(id, match { it.estado == Estado.PENDIENTE && !it.asignado }) }
        }
    }

    "eliminar un contenido llama a DELETE y lo borra de Room con sus preguntas" {
        runTest {
            val e = Entorno()
            val id = e.dao.insertarContenido(nuevoContenido()).toInt()
            e.dao.insertarPregunta(pregunta(0, 1).copy(contenidoId = id))
            coEvery { e.contenidosApi.eliminar(id) } returns Unit

            e.repo.eliminar(e.dao.buscarContenido(id)!!)

            coVerify { e.contenidosApi.eliminar(id) }
            e.dao.buscarContenido(id) shouldBe null
            e.dao.todasLasPreguntas(id) shouldHaveSize 0
        }
    }

    "agregar y eliminar preguntas usa el microservicio" {
        runTest {
            val e = Entorno()
            coEvery { e.contenidosApi.agregarPregunta(1, any()) } answers { secondArg<PreguntaDto>().copy(id = 77, contenidoId = 1) }
            coEvery { e.contenidosApi.eliminarPregunta(77) } returns Unit

            e.repo.agregarPregunta(pregunta(0, 2)) shouldBe 77L
            e.dao.todasLasPreguntas(1).single().id shouldBe 77

            e.repo.eliminarPregunta(e.dao.todasLasPreguntas(1).single())
            coVerify { e.contenidosApi.eliminarPregunta(77) }
            e.dao.todasLasPreguntas(1) shouldHaveSize 0
        }
    }

    "sin conexión, la pregunta se guarda solo en Room" {
        runTest {
            val e = Entorno()
            coEvery { e.contenidosApi.agregarPregunta(any(), any()) } throws sinRed

            e.repo.agregarPregunta(pregunta(0, 2))

            e.dao.todasLasPreguntas(1) shouldHaveSize 1
        }
    }

    "el intento del estudiante se envía a ms-resultados con cada respuesta" {
        runTest {
            val e = Entorno()
            coEvery { e.contenidosApi.listar() } throws sinRed      // el login no sincroniza
            e.repo.iniciarSesion("est01", "1111")
            val enviado = slot<IntentoRequest>()
            coEvery { e.resultadosApi.registrar(capture(enviado)) } answers {
                val r = enviado.captured
                IntentoDto(900, r.estudianteId, r.contenidoId, r.tipo, r.respuestas.count { it.esCorrecta },
                    r.respuestas.size, r.fecha, r.respuestas.mapIndexed { i, x -> x.copy(id = 9000 + i, intentoId = 900) })
            }

            e.repo.guardarIntento(1, TipoActividad.PRUEBA, listOf(pregunta(10, 1), pregunta(11, 0)), listOf(1, 2))

            enviado.captured.respuestas.map { it.esCorrecta } shouldContainExactly listOf(true, false)
            enviado.captured.respuestas.map { it.opcionElegida } shouldContainExactly listOf(1, 2)
            val guardado = e.dao.todosLosIntentos().single()
            guardado.id shouldBe 900
            guardado.correctas shouldBe 1
            e.dao.todasLasRespuestas().map { it.intentoId }.distinct() shouldContainExactly listOf(900)
        }
    }

    "sin conexión, el intento se guarda solo en Room" {
        runTest {
            val e = Entorno()
            coEvery { e.contenidosApi.listar() } throws sinRed
            coEvery { e.resultadosApi.registrar(any()) } throws sinRed
            e.repo.iniciarSesion("est01", "1111")

            e.repo.guardarIntento(1, TipoActividad.PRUEBA, listOf(pregunta(10, 1)), listOf(1))

            e.dao.todosLosIntentos().single().correctas shouldBe 1
            e.dao.todasLasRespuestas() shouldHaveSize 1
        }
    }

    "las conversiones entre Room y el JSON conservan todos los campos" {
        val c = nuevoContenido().copy(id = 3, estado = Estado.RECHAZADO, observacionUtp = "Falta ejemplo", fechaValidacion = 5L)
        c.aDto().aEntidad() shouldBe c
        nuevoContenido().aDto().id shouldBe null

        val p = pregunta(8, 2)
        p.aDto().aEntidad() shouldBe p
        PreguntaDto(null, null, TipoActividad.DESAFIO, "x", "a", "b", "c", 0, 1, "y").aEntidad(4).contenidoId shouldBe 4

        RespuestaDto(preguntaId = 1, esCorrecta = true, opcionElegida = 0).aEntidad(12).intentoId shouldBe 12
    }
})
