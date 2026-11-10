# Sesión 13 — Proyecto integrador

## Alcance

Se conserva PharmaMobile y su arquitectura Clean + MVVM, Kotlin Multiplatform, Compose, Koin, Ktor y SQLDelight. Se amplía el flujo offline-first de productos, se añade la capa común de permisos y se incorpora un puerto de telemetría con un adaptador Android de Crashlytics.

## Conciliación de pendientes

`Producto.sq` agrega contador de intentos y último error con migración `1.sqm`. Los cambios se persisten primero en SQLDelight y la interfaz sigue leyendo el flujo local. La sincronización se coordina en `SincronizarProductosUseCase`, que distingue resultado exitoso, rechazo permanente y fallo transitorio; limita intentos a cinco y usa pausas exponenciales de 1, 2, 4, 8 y hasta 16 segundos para reintentos automáticos seguros. Los errores permanentes quedan visibles para corrección y reintento manual. Una respuesta de red ambigua no se reenvía automáticamente para evitar duplicar un POST que el servidor quizá ya procesó.

La pantalla de productos muestra pendientes, fallidos, último error y permite reintentar. El dato de última sincronización se conserva localmente y se presenta como antigüedad relativa. Si no se alcanza el servidor, la base local conserva el producto y el estado de sincronización.

## Permisos

El dominio define cámara, ubicación y notificaciones, además de estados concedido, denegado y denegado permanentemente. `GestorPermisos` aísla la API nativa; Android usa un puente con la Activity, el rationale del sistema y Ajustes. El manifiesto declara cámara, ubicación precisa y notificaciones. iOS incorpora las descripciones de uso de cámara y ubicación en `Info.plist`, pero el puente para solicitar permisos y el uso de notificaciones todavía no están implementados allí; la implementación devuelve `NoDisponible`.

## Telemetría

La presentación depende del puerto de dominio `Telemetria`; el adaptador Android usa GitLive Crashlytics y filtra claves con nombres sensibles antes de adjuntar metadatos. La integración solo se activa al incorporar un `androidApp/google-services.json` válido del proyecto Firebase correspondiente. En este entorno no existe esa configuración, por lo que no se afirma que los reportes estén llegando a Firebase. iOS usa un adaptador inactivo hasta integrar la configuración y los frameworks nativos correspondientes. No se registran contraseñas, tokens ni cuerpos de autenticación.

## Validación ejecutada

- `:androidApp:compileDebugKotlin` — compilación Android completada.
- `:shared:testAndroidHostTest` — pruebas JVM/Android Host completadas, incluidos escenarios de sincronización local, respuesta permanente, reintento seguro y conservación de pendientes.
- La suite agregada `:shared:allTests` no puede completarse en Windows: incluye resolución de `sqlite-driver` JVM para el destino iOS Simulator, además de que el simulador y las pruebas nativas requieren macOS. No se presenta esto como una falla de las pruebas Android.

## Pendientes de plataforma

Se requiere configuración Firebase real para comprobar Crashlytics en el proyecto remoto. Falta desarrollar y probar el puente nativo de permisos iOS. Las operaciones de alta con respuesta ambigua requieren revisar el registro en servidor antes de reintentar manualmente para evitar duplicados.
