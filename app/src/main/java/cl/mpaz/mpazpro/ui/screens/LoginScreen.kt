package cl.mpaz.mpazpro.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.mpaz.mpazpro.domain.model.Rol
import cl.mpaz.mpazpro.ui.components.BotonPrincipal
import cl.mpaz.mpazpro.ui.components.CampoValidado
import cl.mpaz.mpazpro.ui.theme.Verde
import cl.mpaz.mpazpro.di.AppViewModelFactory
import cl.mpaz.mpazpro.ui.viewmodels.LoginViewModel

//vista inicio de sesion

@Composable
fun LoginScreen(
    onIngreso: (Rol) -> Unit,
    viewModel: LoginViewModel = viewModel(factory = AppViewModelFactory.Factory)
) {

    val estado by viewModel.estado.collectAsStateWithLifecycle()


    val completo by remember { derivedStateOf { estado.usuario.isNotBlank() && estado.pin.length == 4 } }


    LaunchedEffect(estado.rolIngresado) {
        estado.rolIngresado?.let {
            onIngreso(it)
            viewModel.navegacionHecha()
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Icon(Icons.Filled.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp))
            Text("MPAZ PRO", style = MaterialTheme.typography.headlineSmall)
            Text("Escuela Escritora Marcela Paz · 3.º Básico", textAlign = TextAlign.Center)

            Card(Modifier.widthIn(max = 480.dp).fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CampoValidado(estado.usuario, viewModel::onUsuarioCambia, "Usuario", Icons.Filled.Person, estado.errorUsuario)
                    CampoValidado(estado.pin, viewModel::onPinCambia, "PIN (4 números)", Icons.Filled.Lock, estado.errorPin, esPin = true)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = estado.recordar, onCheckedChange = viewModel::onRecordarCambia)
                        Text("Recordar mi usuario")
                    }

                    AnimatedVisibility(visible = estado.errorGeneral != null) {
                        Text("⚠ ${estado.errorGeneral ?: ""}", color = MaterialTheme.colorScheme.error)
                    }
                    AnimatedVisibility(visible = completo && estado.errorGeneral == null) {
                        Text("✓ Datos completos", color = Verde)
                    }

                    BotonPrincipal("Entrar", viewModel::ingresar, cargando = estado.cargando)
                }
            }
            Text(
                "Usuarios ficticios: utp01 y docente01 (PIN 1234) · est01 y est02 (PIN 1111)",
                style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center
            )
        }
    }
}