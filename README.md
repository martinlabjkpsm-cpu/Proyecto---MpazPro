# MPAZ PRO 3.º Básico

Aplicación Android (Kotlin + Jetpack Compose) de apoyo didáctico y seguimiento del aprendizaje
para 3.º Básico de la Escuela Escritora Marcela Paz (caso DSY1105 – Desarrollo de Aplicaciones Móviles).
MVP académico: todos los usuarios, respuestas y resultados son ficticios.

## Integrantes
- Martín Escobar
- Rocío Sáez

## Funcionalidades implementadas
- **Login:** usuario y PIN validados por campo, opción "Recordar mi usuario" y navegación según el rol.
- **UTP:** revisa los contenidos y los aprueba o rechaza con observación (obligatoria al rechazar).
- **Docente:** asigna los objetivos aprobados, define el máximo de intentos de la prueba final (1 a 5),
  revisa el informe pedagógico (fortalezas, aspectos a reforzar, resultados por estudiante) y lo valida con comentario.
- **Estudiante:** ve el material asignado con su explicación, resuelve desafíos de dificultad progresiva
  con retroalimentación y pista, rinde la prueba final dentro de los intentos permitidos y revisa sus logros
  (estrellas y medallas).

## En desarrollo
- Formulario para que el docente cree y corrija contenidos y preguntas.
- Revisión de respuestas correctas e incorrectas de la prueba final por parte del estudiante.
- Fecha de validación y descarga del informe en CSV.

## Arquitectura
MVVM con separación por capas:
- `data/local`: Room (usuarios, contenidos, preguntas, intentos, respuestas) y DataStore
- `data/repository`: acceso centralizado a los datos
- `domain/model`: modelos y reglas (logros, informe, estados de formularios)
- `ui/screens` y `ui/viewmodels`: vistas en Compose y su estado con StateFlow
- `utils/Validaciones`: validaciones desacopladas de la interfaz
- `nativo`: recursos del dispositivo

## Recursos nativos
- **Vibración:** retroalimentación al acertar o fallar.
- **Notificaciones:** aviso al terminar una actividad, con permiso en tiempo de ejecución (Android 13+).
- **TextToSpeech:** lectura en voz alta de preguntas y alternativas.
- **Cámara:** foto de perfil del estudiante.

## Cómo ejecutar
1. Clonar: `git clone https://github.com/martinlabjkpsm-cpu/Proyecto---MpazPro.git` y abrir con Android Studio (Ladybug o superior), JDK 17.
2. Sincronizar Gradle y ejecutar en un emulador o dispositivo con Android 8.0 (API 26) o superior.
3. Usuarios de prueba: `utp01` / `docente01` (PIN 1234) · `est01` / `est02` (PIN 1111).

## Pruebas
`./gradlew test` (validaciones y cálculo de logros).

## Planificación
Trello: https://trello.com/b/MViVQnjK/tablero-mpaz-pro-3-basico