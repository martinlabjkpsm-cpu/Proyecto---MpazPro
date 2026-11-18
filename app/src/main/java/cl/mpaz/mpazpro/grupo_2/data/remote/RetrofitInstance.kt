package cl.mpaz.mpazpro.grupo_2.data.remote

import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

/**
 * Punto único de configuración de Retrofit. `MPazApp` llama a `createApi` una sola vez.
 * Necesita la carpeta de caché de la app, que solo se conoce con un `Context`.
 */
object RetrofitInstance {
    // La URL base debe terminar en "/" para que Retrofit concatene bien las rutas.
    private const val BASE_URL = "https://pokeapi.co/api/v2/"

    // Caché HTTP en disco: 10 MB alcanzan para los compañeros Pokémon de la app.
    private const val CACHE_SIZE_BYTES = 10L * 1024 * 1024

    /** Crea el `ApiService` con caché HTTP en `cacheDir/http`. */
    fun createApi(cacheDir: File): ApiService {
        // OkHttp es el cliente HTTP que usa Retrofit por debajo: se le agrega la caché
        // y el interceptor que la usa cuando no hay conexión.
        val client = OkHttpClient.Builder()
            .cache(Cache(File(cacheDir, "http"), CACHE_SIZE_BYTES))
            .addInterceptor(OfflineCacheInterceptor())
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            // Gson convierte el JSON de la respuesta en las clases de `model/`.
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
