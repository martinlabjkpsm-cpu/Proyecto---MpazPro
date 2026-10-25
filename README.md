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
  Revisa sus logros con estrellas y medallas. El estudiante no tiene acceso al informe pedagógico.

## Próximas mejoras
- Lectura en voz alta de la explicación de cada objetivo.
- Imágenes de apoyo de elaboración propia por objetivo.

## Arquitectura
MVVM con separación por capas:
- `data/local`: Room (usuarios, contenidos, preguntas, intentos, respuestas) y DataStore
- `data/repository`: acceso centralizado a los datos
- `domain/model`: modelos y reglas (logros, informe, revisión, estados de formularios)
- `ui/screens` y `ui/viewmodels`: vistas en Compose y su estado con StateFlow
- `utils`: validaciones desacopladas de la interfaz y formato de fechas
- `nativo`: recursos del dispositivo

## Recursos nativos
- **Vibración:** retroalimentación al acertar o fallar.
- **Notificaciones:** aviso al terminar una actividad, con permiso en tiempo de ejecución (Android 13+).
- **TextToSpeech:** lectura en voz alta de preguntas y alternativas.
- **Almacenamiento (Storage Access Framework):** descarga del informe en CSV sin permisos de almacenamiento.
- **Cámara:** foto de perfil del estudiante.

## Cómo ejecutar
1. Clonar: `git clone https://github.com/martinlabjkpsm-cpu/Proyecto---MpazPro.git` y abrir con Android Studio (Ladybug o superior), JDK 17.
2. Sincronizar Gradle y ejecutar en un emulador o dispositivo con Android 8.0 (API 26) o superior.
3. Usuarios de prueba: `utp01` / `docente01` (PIN 1234) · `est01` / `est02` (PIN 1111).

## Pruebas
`./gradlew test` ejecuta 17 pruebas unitarias: validaciones de login, contenidos y preguntas, regla de una
unidad por asignatura, cálculo de logros, revisión de respuestas y generación del CSV.

## Planificación
Trello: https://trello.com/b/MViVQnjK/tablero-mpaz-pro-3-basico