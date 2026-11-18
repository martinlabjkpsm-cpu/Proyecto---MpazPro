package cl.mpaz.mpazpro.grupo_2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import cl.mpaz.mpazpro.grupo_2.navigation.AppNavegacion
import cl.mpaz.mpazpro.grupo_2.ui.theme.MPazTheme



class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val tamano = calculateWindowSizeClass(this)
            val esTablet = tamano.widthSizeClass != WindowWidthSizeClass.Compact

            MPazTheme {
                AppNavegacion(esTablet = esTablet)
            }
        }
    }
}