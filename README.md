# InsertCode Ventas y Distribución

MVP académico para Android. El vendedor registra un pedido, el repartidor cambia el estado de la entrega asignada y el supervisor consulta el resultado en el panel. El administrador configura cuentas ficticias, roles y permisos. Los productos, clientes, ubicaciones y accesos se generan para la demostración.

## Requisitos

- Node.js 22 o superior para la API de prueba.
- Android Studio compatible con AGP 8.13, JDK 17 y Android SDK 35.
- Emulador o teléfono con Android 7.0 o superior. Para un teléfono por USB, activa la depuración USB.

## Ejecutar la demostración en Windows

1. Descomprime el ZIP del proyecto. En la carpeta que contiene `gradlew.bat`, haz doble clic en **`Iniciar-InsertCode-Demo.cmd`**.
2. Deja abierta la ventana que se abre. Inicia la API de prueba y muestra la contraseña ficticia de la demostración.
3. Abre esa misma carpeta en Android Studio, sincroniza Gradle y ejecuta el módulo `app` en el emulador.
4. Entra como `admin@insertcode.test` con la contraseña que muestra la ventana. También puedes usar `supervisor@insertcode.test`, `vendedor@insertcode.test` o `repartidor@insertcode.test` con esa misma contraseña.

El lanzador requiere Node.js 22 o superior. Si la ventana indica que el puerto 8080 está ocupado, cierra la instancia anterior de la API y abre de nuevo el `.cmd`.

## Iniciar la API desde la terminal

Si abriste el repositorio de GitHub, inicia la API desde la raíz con `node api/servidor.mjs`. El primer inicio genera e imprime los cuatro accesos de prueba. La API guarda sus cambios en `api/datos/`. Puedes usar otro archivo de datos para una demostración nueva mediante `INSERTCODE_DATOS`.

El emulador estándar se conecta a `http://10.0.2.2:8080/`. Para usar un teléfono por USB, ejecuta `adb reverse tcp:8080 tcp:8080`, compila con `./gradlew :app:assembleDebug -PinsertCodeApiUrl=http://127.0.0.1:8080/` e instala con `adb install -r app/build/outputs/apk/debug/app-debug.apk`. En Windows usa `gradlew.bat` en lugar de `./gradlew`. El APK firmado de la entrega también usa el reenvío con `adb reverse`. La opción `-PinsertCodeApiUrl` admite una URL HTTPS de prueba terminada en una barra.

## Verificar el recorrido

1. Como vendedor, abre el catálogo, selecciona un producto y registra el pedido con cliente, fecha, prioridad y cantidad.
2. Como repartidor, abre la ruta asignada e inicia el despacho. Confirma la entrega desde la misma sección.
3. Como supervisor, actualiza el panel y verifica que cambien los conteos y el total de ventas.
4. Como administrador, crea una cuenta ficticia y ajusta su rol y permisos. Los cambios de acceso invalidan las sesiones anteriores cuando se vuelven a usar.
5. Con una sesión iniciada y la operación cargada, detén la API y pulsa **Actualizar**. Room mantiene disponible la última consulta autorizada. Reinicia la API y vuelve a iniciar sesión si el reinicio invalidó el acceso. Para comprobar el reintento de una sesión vigente, interrumpe y restablece el reenvío durante la prueba con teléfono. La creación de pedidos usa un identificador de operación para evitar duplicados al reintentar.

## Pruebas y APK

- API y persistencia del servidor con `node --test api/servidor.test.mjs`.
- Compilación, pruebas unitarias y lint con `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`.
- Pruebas de Compose y Room con `./gradlew :app:connectedDebugAndroidTest` en un dispositivo o emulador.
- Para firmar otro APK, usa **Build → Generate Signed Bundle / APK → APK** en Android Studio y selecciona una clave propia de demostración. Mantén la clave y sus contraseñas fuera del repositorio.
