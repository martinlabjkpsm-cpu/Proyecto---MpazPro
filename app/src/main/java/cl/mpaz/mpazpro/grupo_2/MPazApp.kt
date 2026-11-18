package cl.mpaz.mpazpro.grupo_2

import android.app.Application
import cl.mpaz.mpazpro.grupo_2.data.local.AppDatabase
import cl.mpaz.mpazpro.grupo_2.data.local.DatosIniciales
import cl.mpaz.mpazpro.grupo_2.data.local.PreferenciasDataStore
import cl.mpaz.mpazpro.grupo_2.data.repository.MPazRepository
import cl.mpaz.mpazpro.grupo_2.nativo.ExportadorCsv
import cl.mpaz.mpazpro.grupo_2.nativo.FotoPerfil
import cl.mpaz.mpazpro.grupo_2.nativo.LectorVoz
import cl.mpaz.mpazpro.grupo_2.nativo.Notificaciones
import cl.mpaz.mpazpro.grupo_2.nativo.Vibracion
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
    lateinit var exportador: ExportadorCsv

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.obtener(this)
        repositorio = MPazRepository(db.dao(), PreferenciasDataStore(this))
        vibracion = Vibracion(this)
        notificaciones = Notificaciones(this)
        notificaciones.crearCanal()
        lectorVoz = LectorVoz(this)
        fotoPerfil = FotoPerfil(this)
        exportador = ExportadorCsv(this)

        //carga datos ficticios
        CoroutineScope(Dispatchers.IO).launch { DatosIniciales.cargarSiEstaVacia(db.dao()) }
    }
}