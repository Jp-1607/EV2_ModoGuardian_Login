# InsertCode Ventas y Distribución

MVP académico para Android. El vendedor registra un pedido, el repartidor cambia el estado de la entrega asignada y el supervisor consulta el resultado en el panel. El administrador configura cuentas ficticias, roles y permisos. Los productos, clientes, ubicaciones y accesos se generan para la demostración.

## Requisitos

- Node.js 22 o superior para la API de prueba.
- Android Studio compatible con AGP 8.13, JDK 17 y Android SDK 35.
- Emulador o teléfono con Android 7.0 o superior. Para un teléfono por USB, activa la depuración USB.

## Ejecutar

1. Desde la raíz del proyecto, inicia la API con `node api/servidor.mjs`. El primer inicio imprime cuatro accesos de demostración. Guárdalos para usar la misma base en sesiones posteriores. El archivo que crea la API en `api/datos/` conserva los cambios.
2. Abre la raíz en Android Studio, sincroniza y ejecuta el módulo `app`. El emulador estándar conecta a `http://10.0.2.2:8080/`.
3. En teléfono físico, ejecuta `adb reverse tcp:8080 tcp:8080` y compila con `./gradlew :app:assembleDebug -PinsertCodeApiUrl=http://127.0.0.1:8080/`. Instala con `adb install -r app/build/outputs/apk/debug/app-debug.apk`. El APK de demostración firmado también utiliza la conexión reenviada de este paso, tanto en teléfono como en emulador.
4. Ingresa con uno de los accesos impresos por la API. Si necesitas un conjunto nuevo de datos ficticios sin tocar el anterior, inicia la API con otro archivo local a través de `INSERTCODE_DATOS`.

En Windows usa `gradlew.bat` en lugar de `./gradlew`. La API escucha en la interfaz local. La opción `-PinsertCodeApiUrl` también admite un endpoint HTTPS de prueba terminado en una barra.

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
