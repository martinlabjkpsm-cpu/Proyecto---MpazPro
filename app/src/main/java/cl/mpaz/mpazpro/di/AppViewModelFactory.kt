package cl.mpaz.mpazpro.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.mpaz.mpazpro.MPazApp
import cl.mpaz.mpazpro.ui.viewmodels.DocenteViewModel
import cl.mpaz.mpazpro.ui.viewmodels.EstudianteViewModel
import cl.mpaz.mpazpro.ui.viewmodels.LoginViewModel
import cl.mpaz.mpazpro.ui.viewmodels.UtpViewModel


object AppViewModelFactory {
    val Factory = viewModelFactory {
        initializer { LoginViewModel(app().repositorio) }
        initializer { EstudianteViewModel(app().repositorio, app().vibracion, app().notificaciones) }
        initializer { DocenteViewModel(app().repositorio) }
        initializer { UtpViewModel(app().repositorio) }
    }
}

private fun CreationExtras.app(): MPazApp =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MPazApp