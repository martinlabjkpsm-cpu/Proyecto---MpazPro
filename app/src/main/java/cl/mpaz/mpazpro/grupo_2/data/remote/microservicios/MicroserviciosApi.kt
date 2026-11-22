package cl.mpaz.mpazpro.grupo_2.data.remote.microservicios

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Contratos de los microservicios propios para Retrofit (API interna).
// Retrofit genera la implementación: cada método anotado es una petición HTTP.
// Si el servidor responde un error (400, 404, 409, 500) Retrofit lanza HttpException;
// si no hay conexión, IOException. El repositorio captura ambas.

/** ms-contenidos, puerto 8080. URL base: http://10.0.2.2:8080/api/v1/ */
interface ContenidosApi {

    @GET("contenidos")
    suspend fun listar(): List<ContenidoDto>

    @POST("contenidos")
    suspend fun crear(@Body contenido: ContenidoDto): ContenidoDto

    @PUT("contenidos/{id}")
    suspend fun actualizar(@Path("id") id: Int, @Body contenido: ContenidoDto): ContenidoDto

    @DELETE("contenidos/{id}")
    suspend fun eliminar(@Path("id") id: Int)

    @GET("contenidos/{id}/preguntas")
    suspend fun preguntas(@Path("id") contenidoId: Int): List<PreguntaDto>

    @POST("contenidos/{id}/preguntas")
    suspend fun agregarPregunta(@Path("id") contenidoId: Int, @Body pregunta: PreguntaDto): PreguntaDto

    @DELETE("preguntas/{id}")
    suspend fun eliminarPregunta(@Path("id") id: Int)
}

/** ms-resultados, puerto 8081. URL base: http://10.0.2.2:8081/api/v1/ */
interface ResultadosApi {

    @GET("intentos")
    suspend fun listar(): List<IntentoDto>

    @POST("intentos")
    suspend fun registrar(@Body intento: IntentoRequest): IntentoDto
}
