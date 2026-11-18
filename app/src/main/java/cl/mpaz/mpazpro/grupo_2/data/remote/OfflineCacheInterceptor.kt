package cl.mpaz.mpazpro.grupo_2.data.remote

import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.HttpURLConnection.HTTP_GATEWAY_TIMEOUT
import java.util.concurrent.TimeUnit

/**
 * Tomado del ejemplo my-pokemon del docente.
 * Si falla la red, intenta responder con la copia guardada en la caché de OkHttp.
 *
 * Con conexión, la caché funciona sola: PokéAPI envía `Cache-Control: max-age=86400`,
 * así que una respuesta se reutiliza durante un día sin volver a descargarla. Este
 * interceptor cubre el caso sin conexión: acepta copias caducadas de hasta
 * `MAX_STALE_DAYS` días, para que un compañero ya visto se pueda mostrar sin red.
 */
class OfflineCacheInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return try {
            chain.proceed(request)
        } catch (e: IOException) {
            // Sin red: pide la misma URL, pero solo a la caché.
            val cacheOnly = CacheControl.Builder()
                .onlyIfCached()
                .maxStale(MAX_STALE_DAYS, TimeUnit.DAYS)
                .build()
            val cached = chain.proceed(request.newBuilder().cacheControl(cacheOnly).build())

            // OkHttp responde 504 si no hay copia guardada. En ese caso se relanza el
            // error de red original, para que el usuario vea "Sin conexión" y no un
            // error del servidor.
            if (cached.code == HTTP_GATEWAY_TIMEOUT) {
                cached.close()
                throw e
            }
            cached
        }
    }

    private companion object {
        const val MAX_STALE_DAYS = 30
    }
}
