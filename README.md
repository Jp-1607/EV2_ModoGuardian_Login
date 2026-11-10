# Modo Guardián

Proyecto en Kotlin con Jetpack Compose. Abre esta carpeta en Android Studio y sincroniza Gradle con el SDK 35 instalado.

- Guías 7 a 9: estructura MVVM, pantallas Compose y login adaptable con vistas previas.
- Guías 10 y 11: navegación, menú inferior y registro con validación y resumen. El registro no crea cuentas ni muestra la contraseña.
- Guías 12 y 13: modo especial guardado con DataStore y animaciones; foto de perfil desde galería o cámara.
- Guías 14 y 15: publicaciones con Retrofit, pruebas unitarias con Kotest y MockK, y prueba de interfaz con Compose.

Para entrar como Admin, Supervisor u Operador, usa admin@guardian.test, supervisor@guardian.test u operador@guardian.test. La clave de demostración es 123456. Las cuentas no aparecen en la pantalla de acceso.

JSONPlaceholder simula los cambios. Al recargar, vuelve la lista original.

En Windows: `gradlew.bat testDebugUnitTest assembleDebug`. Para la prueba de interfaz, inicia un emulador y ejecuta `gradlew.bat connectedDebugAndroidTest`.

Para la entrega quedan las capturas reales del formulario, cámara, galería, pantallas adaptables y pruebas, además de las tarjetas de Trello y la coordinación del repositorio con el equipo y el docente.
