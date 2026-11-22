package cl.mpaz.mpazpro.grupo_2.data.remote.microservicios

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Punto único de configuración de Retrofit para los microservicios propios.
 *
 * Los microservicios corren en la instancia EC2 `mpazpro-api` de AWS, con IP elástica (fija),
 * así la app funciona en el emulador y en un teléfono real con cualquier internet.
 * Para probarlos en el PC (respaldo): usar IP_PC_DESDE_EMULADOR ("10.0.2.2", el localhost del PC
 * visto desde el emulador) en lugar de IP_SERVIDOR.
 * Se usa http:// (sin certificado), por eso el manifiesto tiene usesCleartextTraffic="true".
 */
object RetrofitMicroservicios {

    const val IP_SERVIDOR = "184.197.243.37"          // IP elástica de la EC2 mpazpro-api
    const val IP_PC_DESDE_EMULADOR = "10.0.2.2"

    const val URL_CONTENIDOS = "http://$IP_SERVIDOR:8080/api/v1/"
    const val URL_RESULTADOS = "http://$IP_SERVIDOR:8081/api/v1/"

    // Tiempos cortos: si los microservicios están apagados, la app sigue con Room sin quedarse esperando.
    private val cliente: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build()
    }

    private fun retrofit(baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)      // debe terminar en "/"
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    fun contenidosApi(baseUrl: String = URL_CONTENIDOS): ContenidosApi =
        retrofit(baseUrl).create(ContenidosApi::class.java)

    fun resultadosApi(baseUrl: String = URL_RESULTADOS): ResultadosApi =
        retrofit(baseUrl).create(ResultadosApi::class.java)
}
