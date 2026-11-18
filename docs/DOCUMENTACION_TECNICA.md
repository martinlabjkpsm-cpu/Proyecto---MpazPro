# Documentación técnica · MPAZ PRO 3.º Básico (Grupo 2)

Asignatura DSY1105 · Desarrollo de Aplicaciones Móviles · Experiencia 3 / EFT
Integrantes: Martín Escobar · Rocío Sáez

## 1. Contexto y problemática

La Escuela Escritora Marcela Paz necesita apoyar el aprendizaje de 3.º Básico fuera de la sala y hacer
seguimiento del avance de cada estudiante. Hoy el material, las actividades y los resultados están dispersos,
el equipo UTP no tiene un flujo claro para autorizar contenidos y el docente no tiene un informe rápido por objetivo.

**Solución:** una app Android con tres perfiles:

| Perfil | Qué hace |
|---|---|
| Docente | Propone contenidos por Objetivo de Aprendizaje (OA), agrega desafíos y prueba final, asigna actividades, revisa y valida el informe pedagógico y lo descarga en CSV. |
| UTP | Revisa, aprueba o rechaza (con observación) cada contenido antes de que llegue a los estudiantes. |
| Estudiante | Ve el material aprobado y asignado, resuelve desafíos con retroalimentación, rinde la prueba final, revisa sus respuestas y gana estrellas, medallas y compañeros Pokémon. |

Alcance del MVP: una unidad por asignatura (Matemática, Lenguaje, Ciencias Naturales e Historia), usuarios y
resultados ficticios. Alineado con los parámetros del EFT: problemática definida, solución móvil funcional,
persistencia local, recursos nativos, consumo de API externa y microservicios, pruebas unitarias y APK firmado.

## 2. Arquitectura

Patrón **MVVM** con separación por capas, paquete `cl.mpaz.mpazpro.grupo_2`. La parte de PokéAPI sigue la
misma estructura del ejemplo del docente (`my-pokemon`):

```
ui/screens, ui/components (Compose) → ui/viewmodels (StateFlow) → data/repository → data/local  (Room + DataStore)
                                                                                └─→ data/remote (Retrofit: PokéAPI)
model: clases del JSON de PokéAPI (Gson) · domain/model: reglas de negocio puras
nativo: vibración, notificaciones, TextToSpeech, cámara y CSV · di/AppViewModelFactory: crea los ViewModel
```

| Capa | Archivos principales |
|---|---|
| Vista | `ui/screens/*Screen.kt`, `ui/components/TarjetaCompanero.kt` |
| Estado | `ui/viewmodels/*ViewModel.kt`, `CompaneroUiState.kt`, `CompaneroUi.kt` |
| Datos locales | `data/local/AppDatabase.kt` (Room v2 con migración 1→2), `AppDao.kt`, `PreferenciasDataStore.kt` |
| Datos remotos | `data/remote/ApiService.kt`, `RetrofitInstance.kt`, `OfflineCacheInterceptor.kt` |
| Modelo del JSON | `model/Pokemon.kt`, `Sprites.kt`, `OtherSprites.kt`, `OfficialArtwork.kt`, `Types.kt`, `Type.kt` |
| Repositorios | `data/repository/MPazRepository.kt`, `PokemonRepository.kt` (interfaz), `DefaultPokemonRepository.kt`, `PokemonException.kt` |

**Tecnologías:** Kotlin 2.0, Jetpack Compose (Material 3), Navigation Compose, Room 2.6, DataStore,
Retrofit 2.11 + Gson, OkHttp 4.12 (caché HTTP), Coil 3 (imágenes), corrutinas. minSdk 26 · targetSdk 35 · JDK 17.

## 3. Consumo de API externa: PokéAPI

- **Qué es:** API REST pública y gratuita, sin clave: https://pokeapi.co/docs/v2
- **Para qué la usamos:** como premio en la pestaña *Mis logros* del estudiante. Todos parten con un compañero
  (Pikachu) y **cada medalla desbloquea uno nuevo**. Se muestran su número y nombre, sus tipos en español y sus
  medidas en centímetros y kilos (práctica de unidades de medida de 3.º Básico).

| Método | Endpoint | Datos que usamos |
|---|---|---|
| GET | `https://pokeapi.co/api/v2/pokemon/{idOrName}` | `id`, `name`, `height` (dm → cm), `weight` (hg → kg), `types`, `sprites.other.official-artwork.front_default` |

**Flujo:** `TarjetaCompanero` → `CompaneroViewModel.actualizar(medallas)` → `PokemonRepository.getPokemon(id)`
→ `ApiService` (Retrofit) → JSON → `model/Pokemon` (Gson) → `toUi()` → `CompaneroUiState.Success`.

- **Errores:** el repositorio traduce cada fallo a `PokemonException` (`Network`, `NotFound`, `Server`, `Unknown`)
  y el ViewModel muestra un mensaje y el botón **Reintentar**. La app nunca se cae por la API.
- **Caché:** OkHttp guarda las respuestas 1 día (lo indica PokéAPI) y `OfflineCacheInterceptor` usa la copia
  guardada si no hay internet. Coil muestra un indicador mientras descarga la imagen.
- No interfiere con Room ni con los microservicios: tiene su propio `ApiService`, repositorio y ViewModel.

## 4. Microservicios (Spring Boot)

| Dato | Valor |
|---|---|
| Repositorio | _(completar)_ |
| URL base | `http://10.0.2.2:8080/` desde el emulador (es el `localhost` del PC) |
| Endpoints | _(completar)_ |

La app ya tiene el permiso `INTERNET` y `android:usesCleartextTraffic="true"` para llamar por `http://` al PC.

## 5. Pruebas unitarias

Estrategia de la Guía 15: **Kotest** (StringSpec) + **MockK** + **kotlinx-coroutines-test** + **JUnit 5**,
y **Compose UI Test** con `testTag` para la interfaz.

| Comando | Qué corre |
|---|---|
| `./gradlew test` | Pruebas unitarias en `app/src/test` (sin emulador): casos de uso CU01–CU18 y API externa. |
| `./gradlew connectedAndroidTest` | Pruebas en `app/src/androidTest` (con emulador): Room real, recursos nativos e interfaz. |

Pruebas de la API externa (`app/src/test/.../apiexterna`), con clases falsas como en el ejemplo del docente (no usan internet):

| Clase | Qué verifica |
|---|---|
| `PokemonRepositoryTest` | Respuesta correcta, 404 → `NotFound`, 500 o sin cuerpo → `Server`, sin internet → `Network`, JSON roto → `Unknown`. Usa `FakeApiService`. |
| `CompaneroUiTest` | Conversión a lo que se muestra (título, imagen, tipos en español, cm/kg), datos faltantes y desbloqueo por medallas. |
| `CompaneroViewModelTest` | Loading → Success / Error, Reintentar, nuevo compañero al ganar medalla, sin llamadas repetidas y última elección gana. Usa `FakePokemonRepository`. |

| `TarjetaCompaneroTest` (androidTest) | La tarjeta muestra el compañero y sus medidas; sin internet muestra el error y Reintentar lo carga. |

Cobertura: en Android Studio, clic derecho en `app/src/test` → **Run 'Tests in…' with Coverage**.
Todas las pruebas unitarias usan JUnit 5 (Kotest), así que no hace falta `junit-vintage-engine`.

## 6. APK firmado (release)

La firma está configurada en `app/build.gradle.kts` (`signingConfigs`). Las contraseñas se leen de
`keystore.properties`, que no se sube a GitHub.

1. Android Studio → **Build › Generate Signed App Bundle or APK…** → **APK** → Next.
2. **Create new…**: guardar la llave como `mpazpro-grupo2.jks` **en la carpeta raíz del proyecto**.
   Alias `mpazpro`, validez 25 años, nombre del grupo. Anotar las dos contraseñas.
3. Crear en la raíz del proyecto el archivo `keystore.properties`:
   ```
   storeFile=mpazpro-grupo2.jks
   storePassword=LA_CONTRASEÑA_DE_LA_LLAVE
   keyAlias=mpazpro
   keyPassword=LA_CONTRASEÑA_DEL_ALIAS
   ```
4. Terminar el asistente con la variante **release** → Create (o en Terminal: `./gradlew assembleRelease`).
5. El APK queda en `app/release/app-release.apk` (asistente) o `app/build/outputs/apk/release/` (Terminal).
6. Como pide el encargo, subir a GitHub el `.jks` y el APK firmado (copiarlo a una carpeta `apk/`), y poner en el
   README una captura del APK y del `.jks`.

## 7. Cómo ejecutar

1. Clonar `https://github.com/martinlabjkpsm-cpu/Proyecto---MpazPro.git` y abrir con Android Studio, JDK 17.
2. Sincronizar Gradle y ejecutar en emulador o teléfono con Android 8.0 (API 26) o superior, con internet
   para ver los compañeros Pokémon.
3. Usuarios de prueba: `utp01` / `docente01` (PIN 1234) · `est01` / `est02` (PIN 1111).
