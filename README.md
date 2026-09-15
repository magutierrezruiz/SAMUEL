# Mundo Matemático de Samuel

Videojuego educativo Android personalizado para **Samuel**, pensado para niños menores de 10 años.

## Qué incluye

- 4 mundos: sumas, restas, multiplicaciones y divisiones.
- Banco de **400 preguntas deterministas y diferentes**:
  - 100 de sumas.
  - 100 de restas.
  - 100 de multiplicaciones.
  - 100 de divisiones exactas.
- Cada banco mezcla ejercicios directos y problemas de aplicación.
- Cada ronda toma 10 preguntas al azar y evita repetir preguntas hasta recorrer el banco durante la sesión.
- 3 vidas por ronda.
- Retroalimentación explicativa después de **cada** respuesta.
- Animación de celebración cuando la respuesta es correcta.
- Animación de sacudida cuando la respuesta es incorrecta.
- Aprendizaje guiado antes de cada mundo.
- Lectura en voz alta mediante Android Text-to-Speech cuando el dispositivo tiene voz española disponible.
- Todos los mensajes, explicaciones, preguntas y resultados se dirigen a **Samuel por su nombre**.
- Funciona sin internet y no necesita cuentas ni recopila datos.

## Compilar desde GitHub

1. Crea un repositorio nuevo.
2. Sube **el contenido de esta carpeta directamente a la raíz**.
3. Abre la pestaña **Actions**.
4. Ejecuta `Compilar APK - Samuel` o haz un push a `main`.
5. Al terminar, entra al workflow y descarga el artefacto `Mundo_Matematico_Samuel_APK`.
6. Dentro encontrarás `app-debug.apk`.

## Archivos principales

- `MainActivity.java`: juego completo y banco de preguntas.
- `AndroidManifest.xml`: configuración Android.
- `build-apk.sh`: compilación directa con Android SDK, sin Gradle.
- `.github/workflows/build-apk.yml`: compilación automática en GitHub Actions.

## Requisitos del teléfono

Android 6.0 (API 23) o posterior.

## Compilar con Codemagic
Este repositorio incluye ahora `codemagic.yaml` en la raíz. En Codemagic usa **Check for configuration file** y selecciona el workflow **Mundo Matemático de Samuel - APK**. El artefacto final es `app-debug.apk`.
