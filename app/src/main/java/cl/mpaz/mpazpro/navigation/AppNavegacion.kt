package cl.mpaz.mpazpro.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.mpaz.mpazpro.domain.model.TipoActividad
import cl.mpaz.mpazpro.ui.screens.ActividadScreen
import cl.mpaz.mpazpro.ui.screens.AsignarScreen
import cl.mpaz.mpazpro.ui.screens.DocenteScreen
import cl.mpaz.mpazpro.ui.screens.EstudianteScreen
import cl.mpaz.mpazpro.ui.screens.InformeScreen
import cl.mpaz.mpazpro.ui.screens.LoginScreen
import cl.mpaz.mpazpro.ui.screens.UtpScreen
import cl.mpaz.mpazpro.di.AppViewModelFactory
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


        composable(Rutas.ESTUDIANTE) {
            EstudianteScreen(
                esTablet = esTablet,
                onAbrirActividad = { id, tipo -> navController.navigate(Rutas.actividad(id, tipo.name)) },
                onSalir = cerrarSesion
            )
        }
        composable(
            Rutas.ACTIVIDAD,
            arguments = listOf(
                navArgument("contenidoId") { type = NavType.IntType },
                navArgument("tipo") { type = NavType.StringType }
            )
        ) { entrada ->

            val padre = remember(entrada) { navController.getBackStackEntry(Rutas.ESTUDIANTE) }
            val vm: EstudianteViewModel = viewModel(padre, factory = AppViewModelFactory.Factory)
            ActividadScreen(
                viewModel = vm,
                contenidoId = entrada.arguments?.getInt("contenidoId") ?: 0,
                tipo = TipoActividad.valueOf(entrada.arguments?.getString("tipo") ?: "DESAFIO"),
                onVolver = { navController.popBackStack() }
            )
        }


        composable(Rutas.DOCENTE) {
            DocenteScreen(
                esTablet = esTablet,
                onAsignar = { id -> navController.navigate(Rutas.asignar(id)) },
                onInforme = { id -> navController.navigate(Rutas.informe(id)) },
                onSalir = cerrarSesion
            )
        }
        composable(Rutas.ASIGNAR, arguments = listOf(navArgument("contenidoId") { type = NavType.IntType })) { entrada ->
            val padre = remember(entrada) { navController.getBackStackEntry(Rutas.DOCENTE) }
            val vm: DocenteViewModel = viewModel(padre, factory = AppViewModelFactory.Factory)
            AsignarScreen(vm, entrada.arguments?.getInt("contenidoId") ?: 0, onVolver = { navController.popBackStack() })
        }
        composable(Rutas.INFORME, arguments = listOf(navArgument("contenidoId") { type = NavType.IntType })) { entrada ->
            val padre = remember(entrada) { navController.getBackStackEntry(Rutas.DOCENTE) }
            val vm: DocenteViewModel = viewModel(padre, factory = AppViewModelFactory.Factory)
            InformeScreen(vm, entrada.arguments?.getInt("contenidoId") ?: 0, onVolver = { navController.popBackStack() })
        }


        composable(Rutas.UTP) {
            UtpScreen(esTablet = esTablet, onSalir = cerrarSesion)
        }
    }
}