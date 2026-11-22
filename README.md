# MPAZ PRO 3.º Básico

Aplicación Android (Kotlin + Jetpack Compose) de apoyo didáctico y seguimiento del aprendizaje
para 3.º Básico de la Escuela Escritora Marcela Paz (caso DSY1105 – Desarrollo de Aplicaciones Móviles).
MVP académico: todos los usuarios, respuestas y resultados son ficticios.

## Integrantes
- Martín Escobar
- Rocío Sáez

## Funcionalidades implementadas
- **Login:** usuario y PIN validados por campo, opción "Recordar mi usuario" y navegación según el rol.
  Cada pantalla verifica la sesión y el rol; si no corresponde, vuelve al login.
- **Docente:**
  - Propone y corrige contenidos (asignatura, unidad, eje, código OA, objetivo, explicación, ejemplo y recurso
    de referencia) con validación por campo y la regla de una unidad por asignatura.
  - Agrega preguntas de desafío y de prueba final con tres alternativas, dificultad y retroalimentación.
  - Asigna los objetivos aprobados por UTP y define el máximo de intentos de la prueba final (1 a 5).
  - Revisa el informe pedagógico (rendimiento, fortalezas, aspectos a reforzar y resultados por estudiante),
    lo valida con comentario y fecha, y lo descarga en CSV. Si llegan intentos nuevos, el informe pide
    validarse otra vez.
- **UTP:** revisa cada contenido con su objetivo, explicación y preguntas, y lo aprueba o rechaza con
  observación. No permite aprobar un contenido sin desafíos o sin prueba final. Todo cambio del docente
  vuelve a revisión.
- **Estudiante:** ve solo el material aprobado y asignado, resuelve desafíos de dificultad progresiva con
  retroalimentación y pista, rinde la prueba final dentro de los intentos permitidos y revisa sus respuestas
  correctas e incorrectas (la alternativa correcta se muestra al aprobar o al agotar los intentos).
  Revisa sus logros con estrellas y medallas, y cada medalla le desbloquea un compañero Pokémon (API externa PokéAPI). El estudiante no tiene acceso al informe pedagógico.

## Próximas mejoras
- Lectura en voz alta de la explicación de cada objetivo.
- Imágenes de apoyo de elaboración propia por objetivo.

## Arquitectura
MVVM con separación por capas, todo dentro del paquete `cl.mpaz.mpazpro.grupo_2` (grupo 2):
- `data/local`: Room (usuarios, contenidos, preguntas, intentos, respuestas) y DataStore
- `data/remote`: Retrofit para la API externa PokéAPI (`ApiService`, `RetrofitInstance`, caché sin conexión)
- `data/remote/microservicios`: Retrofit para los microservicios propios (`ContenidosApi`, `ResultadosApi`, `RetrofitMicroservicios`)
- `data/repository`: acceso centralizado a los datos (Room, microservicios y PokéAPI)
- `model`: clases del JSON de PokéAPI (Gson), igual que el ejemplo `my-pokemon` del docente
- `domain/model`: modelos y reglas (logros, informe, revisión, estados de formularios)
- `ui/screens` y `ui/viewmodels`: vistas en Compose y su estado con StateFlow
- `utils`: validaciones desacopladas de la interfaz y formato de fechas
- `nativo`: recursos del dispositivo
- `di`: fábrica de ViewModels con sus dependencias

## APIs
| API | Método | Endpoint | Uso en la app |
|---|---|---|---|
| PokéAPI (externa) | GET | `https://pokeapi.co/api/v2/pokemon/{idOrName}` | Compañero Pokémon que se desbloquea con cada medalla |
| ms-contenidos (propio, Spring Boot) | GET · POST | `http://184.197.243.37:8080/api/v1/contenidos` | Listar y crear contenidos (docente) |
| | PUT · DELETE | `http://184.197.243.37:8080/api/v1/contenidos/{id}` | Editar, aprobar o rechazar (UTP), asignar y validar informe, eliminar |
| | GET · POST | `http://184.197.243.37:8080/api/v1/contenidos/{id}/preguntas` | Preguntas de desafío y prueba final |
| | DELETE | `http://184.197.243.37:8080/api/v1/preguntas/{id}` | Eliminar una pregunta |
| ms-resultados (propio, Spring Boot) | GET · POST | `http://184.197.243.37:8081/api/v1/intentos` | Intentos del estudiante con sus respuestas (informe del docente) |

**Microservicios propios:** proyecto `mpazpro-microservicios` (Spring Boot 4.1 + PostgreSQL), publicados en la instancia EC2
`mpazpro-api` de AWS (IP elástica `184.197.243.37`) y conectados a la base `mpazpro` del RDS. Al iniciar sesión y cada vez que se
abre una pantalla principal, la app descarga contenidos, preguntas e intentos y los guarda en Room; cada cambio se envía primero al microservicio y luego se
guarda en Room con el id del servidor. Si los microservicios no responden, la app sigue funcionando solo con Room.
Código: `data/remote/microservicios` (Retrofit) y `data/repository/MPazRepository.kt`.

- Detalle de endpoints, arquitectura y APK firmado en [docs/DOCUMENTACION_TECNICA.md](docs/DOCUMENTACION_TECNICA.md).

## Recursos nativos
- **Vibración:** retroalimentación al acertar o fallar.
- **Notificaciones:** aviso al terminar una actividad, con permiso en tiempo de ejecución (Android 13+).
- **TextToSpeech:** lectura en voz alta de preguntas y alternativas.
- **Almacenamiento (Storage Access Framework):** descarga del informe en CSV sin permisos de almacenamiento.
- **Cámara:** foto de perfil del estudiante.

## APK firmado
- APK release firmado: [`app/release/app-release.apk`](app/release/app-release.apk)
- Llave: [`mpazpro-grupo2.jks`](mpazpro-grupo2.jks) · configuración: `signingConfigs` en `app/build.gradle.kts`

![APK firmado](docs/capturas/apk-firmado.png)
![Llave .jks](docs/capturas/llave-jks.png)
- Pasos para generarlo: sección 6 de [docs/DOCUMENTACION_TECNICA.md](docs/DOCUMENTACION_TECNICA.md).

## Cómo ejecutar

**Microservicios (backend).** Ya están publicados en AWS y arrancan solos con la instancia:
1. Iniciar el laboratorio de AWS Academy (enciende la EC2 `mpazpro-api` y el RDS).
2. Comprobar que responden: `http://184.197.243.37:8080/api/v1/estado` y `http://184.197.243.37:8081/api/v1/estado`
   (Swagger: `http://184.197.243.37:8080/swagger-ui.html` y `http://184.197.243.37:8081/swagger-ui.html`).
3. Para ejecutarlos en un PC (respaldo): crear la base `mpazpro` en PostgreSQL, definir `DB_URL`, `DB_USER` y `DB_PASSWORD`,
   ejecutar `./mvnw spring-boot:run` en `ms-contenidos` y en `ms-resultados`, y en la app cambiar `IP_SERVIDOR` por
   `IP_PC_DESDE_EMULADOR` en `data/remote/microservicios/RetrofitMicroservicios.kt`. Detalle en el README de `mpazpro-microservicios`.

**App Android.**
1. Clonar: `git clone https://github.com/martinlabjkpsm-cpu/Proyecto---MpazPro.git` y abrir con Android Studio (Ladybug o superior), JDK 17.
2. Sincronizar Gradle y ejecutar en un emulador o dispositivo con Android 8.0 (API 26) o superior.
3. Usuarios de prueba: `utp01` / `docente01` (PIN 1234) · `est01` / `est02` (PIN 1111).

## Pruebas
Estrategia híbrida de la Guía 15: **Kotest** para la lógica, **MockK** para simular dependencias,
**kotlinx-coroutines-test** para las corrutinas (`runTest` y `advanceUntilIdle`, sin `Thread.sleep`),
**JUnit 5** como plataforma de los tests unitarios y **Compose UI Test** (ComposeTestRule + `testTag`) para la interfaz.

- `./gradlew test` (carpeta `app/src/test`, sin emulador): 147 pruebas (casos de uso, lógica, API externa y 18 de la integración con los microservicios). Los ViewModel y el repositorio son los
  reales; la base de datos es una base en memoria (`soporte/FakeDao.kt`) con los datos ficticios de la app y los
  recursos nativos (voz, avisos, vibración, foto, CSV) y las preferencias son mocks de MockK (`soporte/Entorno.kt`).
- `./gradlew connectedAndroidTest` (carpeta `app/src/androidTest`, con emulador o teléfono): base Room real,
  recursos nativos reales y pruebas de interfaz con Compose. Los componentes probados tienen `testTag`
  (`ui/components/TagsPrueba.kt`).

### Cobertura de pruebas
Se ejecutaron 147 pruebas unitarias, todas aprobadas (medición del 22-11-2026 con *Run 'Tests' with Coverage* en Android Studio).
El encargo pide al menos 80 % de la lógica (ViewModel o Repository):

| Módulo | Cobertura de líneas | Cobertura de ramas |
|---|---|---|
| ViewModels (`ui/viewmodels`) | 98 % (208/212) | 80 % |
| Repository (`data/repository`) | 100 % (95/95) | 94 % |
| Conexión con los microservicios (`data/remote/microservicios`) | 82 % (72/87) | 94 % |
| **Lógica total (ViewModels + Repository)** | **99 % (303/307)** | |

Las pantallas Compose se prueban aparte con Compose UI Test (androidTest).
<img width="2056" height="1594" alt="image" src="https://github.com/user-attachments/assets/4b3c6090-22a8-4e3f-bfad-ea0c90ea283e" />

<img width="2040" height="1154" alt="image" src="https://github.com/user-attachments/assets/c677093f-211a-4d1f-94fc-50ef17a35961" />



| Caso de uso | Perfil | Clase de prueba | Herramientas |
|---|---|---|---|
| CU01 Iniciar y cerrar sesión según perfil, recordar usuario | Todos | `CU01IniciarSesionTest` | Kotest + MockK + coroutines-test |
| | | `RutasPorPerfilTest` | JUnit 5 |
| | | `LoginScreenTest`, `FlujoPorPerfilUiTest` (androidTest) | Compose UI Test + testTag |
| CU02 Revisar contenidos y sus preguntas | UTP | `CU02UtpRevisaYAutorizaTest` | Kotest + coroutines-test |
| CU03 Aprobar (autorizar) un contenido completo | UTP | `CU02UtpRevisaYAutorizaTest`, `FlujoPorPerfilUiTest` | Kotest + coroutines-test, Compose UI Test |
| CU04 Rechazar con observación | UTP | `CU02UtpRevisaYAutorizaTest` | Kotest + coroutines-test |
| CU05 Eliminar un contenido | UTP | `CU02UtpRevisaYAutorizaTest`, `MPazRepositoryTest` | Kotest + coroutines-test, MockK |
| CU06 Proponer o corregir un contenido (una unidad por asignatura, vuelve a revisión UTP) | Docente | `CU06DocenteGestionaContenidoTest`, `ValidacionesTest`, `MPazRepositoryTest` | Kotest + coroutines-test, MockK |
| CU07 Agregar y eliminar preguntas de desafío y prueba final | Docente | `CU06DocenteGestionaContenidoTest` | Kotest + coroutines-test |
| CU08 Seleccionar objetivo, asignarlo y definir intentos (1 a 5) | Docente | `CU08DocenteAsignaActividadTest` | Kotest + coroutines-test |
| CU09 Revisar informe: rendimiento, fortalezas y aspectos a reforzar | Docente | `CU09DocenteInformeTest`, `InformeCsvTest` | Kotest + coroutines-test |
| CU10 Validar el informe (comentario, confirmación y fecha) | Docente | `CU09DocenteInformeTest` | Kotest + coroutines-test |
| CU11 Descargar el informe CSV | Docente | `CU09DocenteInformeTest` | Kotest + MockK (exportador y Uri) |
| | | `RecursosNativosTest` (androidTest) | JUnit + coroutines-test |
| CU12 Consultar el material asignado (solo aprobado y asignado) | Estudiante | `CU12EstudianteMaterialYDesafiosTest` | Kotest + coroutines-test |
| CU13 Desarrollar desafíos progresivos con retroalimentación | Estudiante | `CU12EstudianteMaterialYDesafiosTest` | Kotest + MockK + coroutines-test |
| CU14 Rendir la prueba final dentro de los intentos permitidos | Estudiante | `CU14EstudiantePruebaFinalTest` | Kotest + coroutines-test |
| CU15 Revisar respuestas correctas e incorrectas | Estudiante | `CU14EstudiantePruebaFinalTest`, `LogrosTest` | Kotest + coroutines-test |
| CU16 Conservar el progreso y ver logros (estrellas y medallas) | Estudiante | `CU16ProgresoYLogrosTest`, `LogrosTest`, `MPazRepositoryTest` | Kotest + MockK + coroutines-test |
| | | `BaseDeDatosRoomTest` (androidTest) | JUnit + coroutines-test |
| CU17 Aviso al terminar, vibración, audio de apoyo y foto de perfil | Estudiante | `CU17ConfirmacionYAudioTest` | Kotest + MockK (verify) + coroutines-test |
| | | `RecursosNativosTest` (androidTest) | JUnit + coroutines-test |
| CU18 Datos ficticios: una unidad por asignatura y tres perfiles | Sistema | `CU16ProgresoYLogrosTest` | Kotest + coroutines-test |
| API externa PokéAPI: compañeros Pokémon por medallas, errores 404/500/sin internet | Estudiante | `PokemonRepositoryTest`, `CompaneroUiTest`, `CompaneroViewModelTest` | Kotest + coroutines-test + fakes |
| | | `TarjetaCompaneroTest` (androidTest) | Compose UI Test + testTag |
| Restricción: el estudiante no ve el informe ni edita contenidos | Estudiante | `CU12EstudianteMaterialYDesafiosTest`, `RutasPorPerfilTest` | Kotest, JUnit 5 |
| | | `FlujoPorPerfilUiTest` (androidTest) | Compose UI Test + testTag |

## Planificación
Trello: https://trello.com/b/MViVQnjK/tablero-mpaz-pro-3-basico
