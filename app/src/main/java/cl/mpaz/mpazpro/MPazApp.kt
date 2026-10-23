package cl.mpaz.mpazpro

import android.app.Application
import cl.mpaz.mpazpro.data.local.AppDatabase
import cl.mpaz.mpazpro.data.local.DatosIniciales
import cl.mpaz.mpazpro.data.local.PreferenciasDataStore
import cl.mpaz.mpazpro.data.repository.MPazRepository
import cl.mpaz.mpazpro.nativo.FotoPerfil
import cl.mpaz.mpazpro.nativo.LectorVoz
import cl.mpaz.mpazpro.nativo.Notificaciones
import cl.mpaz.mpazpro.nativo.Vibracion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Se ejecuta 1 vez al abrir la app, antes que cualquier pantalla, ejecuta todas las bases de datos.


class MPazApp : Application() {

    lateinit var repositorio: MPazRepository
    lateinit var vibracion: Vibracion
    lateinit var notificaciones: Notificaciones
    lateinit var lectorVoz: LectorVoz
    lateinit var fotoPerfil: FotoPerfil

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.obtener(this)
        repositorio = MPazRepository(db.dao(), PreferenciasDataStore(this))
        vibracion = Vibracion(this)
        notificaciones = Notificaciones(this)
        notificaciones.crearCanal()
        lectorVoz = LectorVoz(this)
        fotoPerfil = FotoPerfil(this)

        //carga datos ficticios
        CoroutineScope(Dispatchers.IO).launch { DatosIniciales.cargarSiEstaVacia(db.dao()) }
    }
}