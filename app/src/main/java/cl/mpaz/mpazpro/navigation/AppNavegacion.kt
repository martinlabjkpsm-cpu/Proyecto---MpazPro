package cl.mpaz.mpazpro.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.mpaz.mpazpro.MPazApp
import cl.mpaz.mpazpro.di.AppViewModelFactory
import cl.mpaz.mpazpro.domain.model.Rol
import cl.mpaz.mpazpro.domain.model.TipoActividad
import cl.mpaz.mpazpro.ui.screens.ActividadScreen
import cl.mpaz.mpazpro.ui.screens.AsignarScreen
import cl.mpaz.mpazpro.ui.screens.ContenidoFormScreen
import cl.mpaz.mpazpro.ui.screens.DocenteScreen
import cl.mpaz.mpazpro.ui.screens.EstudianteScreen
import cl.mpaz.mpazpro.ui.screens.InformeScreen
import cl.mpaz.mpazpro.ui.screens.LoginScreen
import cl.mpaz.mpazpro.ui.screens.UtpScreen
import cl.mpaz.mpazpro.ui.viewmodels.DocenteViewModel
import cl.mpaz.mpazpro.ui.viewmodels.EstudianteViewModel


@Composable
fun AppNavegacion(esTablet: Boolean) {
    val navController = rememberNavController()


    val cerrarSesion: () -> Unit = {
        navController.navigate(Rutas.LOGIN) { popUpTo(navController.graph.id) { inclusive = true } }
    }

    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN,
        enterTransition = { slideInHorizontally { it / 3 } + fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        composable(Rutas.LOGIN) {
            LoginScreen(onIngreso = { rol ->

                navController.navigate(Rutas.inicio(rol)) { popUpTo(Rutas.LOGIN) { inclusive = true } }
            })
        }


        // ---------- Estudiante ----------
        composable(Rutas.ESTUDIANTE) {
            RequiereRol(Rol.ESTUDIANTE, cerrarSesion) {
                EstudianteScreen(
                    esTablet = esTablet,
                    onAbrirActividad = { id, tipo -> navController.navigate(Rutas.actividad(id, tipo.name)) },
                    onSalir = cerrarSesion
                )
            }
        }
        composable(
            Rutas.ACTIVIDAD,
            arguments = listOf(
                navArgument("contenidoId") { type = NavType.IntType },
                navArgument("tipo") { type = NavType.StringType }
            )
        ) { entrada ->
            RequiereRol(Rol.ESTUDIANTE, cerrarSesion) {
                val padre = remember(entrada) { navController.getBackStackEntry(Rutas.ESTUDIANTE) }
                val vm: EstudianteViewModel = viewModel(padre, factory = AppViewModelFactory.Factory)
                ActividadScreen(
                    viewModel = vm,
                    contenidoId = entrada.arguments?.getInt("contenidoId") ?: 0,
                    tipo = TipoActividad.valueOf(entrada.arguments?.getString("tipo") ?: "DESAFIO"),
                    onVolver = { navController.popBackStack() }
                )
            }
        }


        // ---------- Docente ----------
        composable(Rutas.DOCENTE) {
            RequiereRol(Rol.DOCENTE, cerrarSesion) {
                DocenteScreen(
                    esTablet = esTablet,
                    onAsignar = { id -> navController.navigate(Rutas.asignar(id)) },
                    onInforme = { id -> navController.navigate(Rutas.informe(id)) },
                    onEditar = { id -> navController.navigate(Rutas.contenidoForm(id)) },
                    onNuevo = { navController.navigate(Rutas.contenidoForm(0)) },
                    onSalir = cerrarSesion
                )
            }
        }
        composable(Rutas.CONTENIDO_FORM, arguments = listOf(navArgument("contenidoId") { type = NavType.IntType })) { entrada ->
            RequiereRol(Rol.DOCENTE, cerrarSesion) {
                ContenidoFormScreen(
                    contenidoId = entrada.arguments?.getInt("contenidoId") ?: 0,
                    onVolver = { navController.popBackStack() }
                )
            }
        }
        composable(Rutas.ASIGNAR, arguments = listOf(navArgument("contenidoId") { type = NavType.IntType })) { entrada ->
            RequiereRol(Rol.DOCENTE, cerrarSesion) {
                val padre = remember(entrada) { navController.getBackStackEntry(Rutas.DOCENTE) }
                val vm: DocenteViewModel = viewModel(padre, factory = AppViewModelFactory.Factory)
                AsignarScreen(vm, entrada.arguments?.getInt("contenidoId") ?: 0, onVolver = { navController.popBackStack() })
            }
        }
        composable(Rutas.INFORME, arguments = listOf(navArgument("contenidoId") { type = NavType.IntType })) { entrada ->
            // El informe es reservado: solo el rol DOCENTE puede verlo (caso 3.1 y 5.1)
            RequiereRol(Rol.DOCENTE, cerrarSesion) {
                val padre = remember(entrada) { navController.getBackStackEntry(Rutas.DOCENTE) }
                val vm: DocenteViewModel = viewModel(padre, factory = AppViewModelFactory.Factory)
                InformeScreen(vm, entrada.arguments?.getInt("contenidoId") ?: 0, onVolver = { navController.popBackStack() })
            }
        }


        // ---------- UTP ----------
        composable(Rutas.UTP) {
            RequiereRol(Rol.UTP, cerrarSesion) {
                UtpScreen(esTablet = esTablet, onSalir = cerrarSesion)
            }
        }
    }
}


// Muestra la pantalla solo si hay una sesión activa con el rol correcto.
// Si Android cerró la app (se pierde la sesión) o el rol no corresponde, vuelve al login.
@Composable
private fun RequiereRol(rol: Rol, onSinSesion: () -> Unit, contenido: @Composable () -> Unit) {
    val app = LocalContext.current.applicationContext as MPazApp
    val usuario = app.repositorio.usuarioActual
    if (usuario == null || usuario.rol != rol) {
        LaunchedEffect(Unit) { onSinSesion() }
    } else {
        contenido()
    }
}