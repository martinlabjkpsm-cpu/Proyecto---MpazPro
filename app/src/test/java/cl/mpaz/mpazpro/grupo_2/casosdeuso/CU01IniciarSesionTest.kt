package cl.mpaz.mpazpro.grupo_2.casosdeuso

import cl.mpaz.mpazpro.grupo_2.domain.model.Rol
import cl.mpaz.mpazpro.grupo_2.soporte.casoDeUso
import cl.mpaz.mpazpro.grupo_2.ui.viewmodels.LoginViewModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent

// CU01 · Iniciar sesión según el perfil (UTP, Docente, Estudiante), recordar usuario y cerrar sesión.
// Herramientas: Kotest (StringSpec) + MockK (Preferencias) + kotlinx-coroutines-test.

@OptIn(ExperimentalCoroutinesApi::class)
class CU01IniciarSesionTest : StringSpec({

    fun LoginViewModel.escribir(usuario: String, pin: String) {
        onUsuarioCambia(usuario)
        onPinCambia(pin)
    }

    "la UTP ingresa con su usuario y PIN y queda con el perfil UTP" {
        casoDeUso {
            val vm = loginVm()
            vm.escribir("utp01", "1234")
            vm.ingresar()
            avanzar()
            vm.estado.value.rolIngresado shouldBe Rol.UTP
            repo.usuarioActual?.rol shouldBe Rol.UTP
        }
    }

    "el docente ingresa y queda con el perfil DOCENTE" {
        casoDeUso {
            val vm = loginVm()
            vm.escribir("docente01", "1234")
            vm.ingresar()
            avanzar()
            vm.estado.value.rolIngresado shouldBe Rol.DOCENTE
            repo.usuarioActual?.nombre shouldBe "Docente 3.º A"
        }
    }

    "el estudiante ingresa y queda con el perfil ESTUDIANTE" {
        casoDeUso {
            val vm = loginVm()
            vm.escribir("est01", "1111")
            vm.ingresar()
            avanzar()
            vm.estado.value.rolIngresado shouldBe Rol.ESTUDIANTE
        }
    }

    "mientras se guarda la sesión el botón muestra que está cargando" {
        casoDeUso {
            // guardar el usuario demora 1 segundo (tiempo virtual: el test no espera de verdad)
            coEvery { preferencias.guardarUsuario(any()) } coAnswers { delay(1_000) }
            val vm = loginVm()
            vm.escribir("est01", "1111")
            vm.ingresar()
            scope.runCurrent()
            vm.estado.value.cargando shouldBe true
            vm.estado.value.rolIngresado.shouldBeNull()

            avanzar()                                    // advanceUntilIdle: pasa el segundo virtual
            vm.estado.value.cargando shouldBe false
            vm.estado.value.rolIngresado shouldBe Rol.ESTUDIANTE
        }
    }

    "un PIN incorrecto muestra error, borra el PIN y no inicia sesión" {
        casoDeUso {
            val vm = loginVm()
            vm.escribir("est01", "9999")
            vm.ingresar()
            avanzar()
            vm.estado.value.errorGeneral shouldBe "Usuario o PIN incorrectos"
            vm.estado.value.pin shouldBe ""
            vm.estado.value.rolIngresado.shouldBeNull()
            repo.usuarioActual.shouldBeNull()
        }
    }

    "con campos vacíos o inválidos no se intenta ingresar" {
        casoDeUso {
            val vm = loginVm()
            vm.ingresar()
            avanzar()
            vm.estado.value.errorUsuario shouldBe "Escribe tu usuario"
            vm.estado.value.errorPin shouldBe "Escribe tu PIN"

            vm.escribir("EST01", "12a")
            vm.estado.value.errorUsuario.shouldNotBeNull()
            vm.estado.value.errorPin.shouldNotBeNull()
            repo.usuarioActual.shouldBeNull()
        }
    }

    "el PIN se corta a cuatro números" {
        casoDeUso {
            val vm = loginVm()
            vm.onPinCambia("123456")
            vm.estado.value.pin shouldBe "1234"
        }
    }

    "si marca recordar, el usuario se guarda y aparece escrito la próxima vez" {
        casoDeUso {
            val vm = loginVm()
            vm.onRecordarCambia(true)
            vm.escribir("docente01", "1234")
            vm.ingresar()
            avanzar()

            coVerify { preferencias.guardarUsuario("docente01") }
            repo.usuarioRecordado.first() shouldBe "docente01"

            val otraVez = loginVm()
            otraVez.estado.value.usuario shouldBe "docente01"
            otraVez.estado.value.recordar shouldBe true
        }
    }

    "si no marca recordar, se guarda un usuario vacío" {
        casoDeUso {
            val vm = loginVm()
            vm.escribir("est02", "1111")
            vm.ingresar()
            avanzar()
            coVerify { preferencias.guardarUsuario("") }
        }
    }

    "después de navegar se limpia el PIN y el rol ingresado" {
        casoDeUso {
            val vm = loginVm()
            vm.escribir("utp01", "1234")
            vm.ingresar()
            avanzar()
            vm.navegacionHecha()
            vm.estado.value.rolIngresado.shouldBeNull()
            vm.estado.value.pin shouldBe ""
        }
    }

    "cerrar sesión olvida al usuario actual" {
        casoDeUso {
            entrarComo("est01", "1111")
            repo.usuarioActual.shouldNotBeNull()
            repo.cerrarSesion()
            repo.usuarioActual.shouldBeNull()
        }
    }
})
