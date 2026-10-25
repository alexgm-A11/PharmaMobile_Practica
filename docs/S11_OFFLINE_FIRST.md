# Sesión 11 - Persistencia SQLDelight y offline-first

## Alcance

Se implementó el criterio de persistencia offline-first sobre `feature/offline-first-guillen`, continuando desde `feature/auth-jwt-guillen`. La guía de laboratorio y la actividad autónoma se trataron como requisitos; las instrucciones de los PDF no se consideran evidencia por sí solas.

## Arquitectura

| Capa | Responsabilidad |
| --- | --- |
| Dominio | `ProductoRepository` expone `observar(): Flow<List<Producto>>`, `registrar`, `listar`, `sincronizar` y `ultimaSincronizacion`. El modelo `Producto` indica si permanece pendiente. |
| Datos local | `Producto.sq` declara `producto` y `sincronizacion`; `ProductoLocal` convierte filas a dominio, asigna ids negativos y escribe cambios en transacciones. |
| Datos remoto | `PharmaSoftProductosRemotos` adapta el puerto mínimo a `PharmaSoftRepository`, por lo que las peticiones usan el cliente Ktor con Auth Bearer de sesión 10. |
| Presentación | `ProductoViewModel` observa el Flow continuamente, inicia sincronización y expone acción de reintento, estado de red y antigüedad de la última sincronización. El perfil offline mantiene acceso local después de que `/auth/me` falla por red. |
| Plataformas | Koin registra `CreadorDeDriverAndroid` con `AndroidSqliteDriver` y `CreadorDeDriverIos` con `NativeSqliteDriver`. |
| Pruebas | Cada test crea `PharmaDb` independiente con `JdbcSqliteDriver.IN_MEMORY` y un fake remoto; no requiere internet ni emulador. |

## Política de sincronización

1. Registrar guarda el producto localmente con id negativo y `pendiente = 1`.
2. Se intenta enviar cada pendiente a PharmaSoft. Al recibir el id real, se reemplaza la fila local en transacción.
3. Después de subir pendientes se descarga la lista remota.
4. La descarga borra y reconstruye únicamente filas `pendiente = 0`; por ello nunca elimina una creación offline.
5. La hora solo se actualiza cuando termina correctamente la subida y la descarga.

El formulario local no solicita categoría. Para satisfacer el contrato de PharmaSoft, el adaptador usa categoría id 1 (`Analgesicos`, insertada por V4 del backend actual). Si esta semilla cambia, debe actualizarse el adaptador o añadirse selector de categoría.

## Actividad autónoma: seis pruebas

Archivo: `shared/src/commonTest/kotlin/pe/edu/upeu/pharmamobile/data/repository/ProductoRepositorioOfflineTest.kt`.

| Prueba | Resultado verificado |
| --- | --- |
| Sincronización con red | Catálogo remoto queda en base y lo emite el Flow; se guarda marca de tiempo. |
| Registro sin red | Se conserva id negativo, pendiente y visible por Flow; timestamp permanece nulo. |
| Envío al reconectar | Se envía una vez, se sustituye por id positivo y deja de ser pendiente. |
| La bajada no borra pendientes | Reemplazar productos sincronizados conserva la fila creada sin red. |
| Identificadores sin colisión | Conviven fila pendiente con id negativo y fila del servidor con id positivo. |
| Sincronizar dos veces | La cantidad de filas permanece igual, sin duplicados. |

## Verificación automática

Comando ejecutado en Windows:

```powershell
./gradlew.bat :shared:testAndroidHostTest :androidApp:assembleDebug :shared:compileKotlinIosSimulatorArm64
```

Resultado: `BUILD SUCCESSFUL`; 37 pruebas de host completadas, incluidas las seis de persistencia, dos de fallback/logout de sesión y una de navegación offline. Se generó el APK debug y compiló el código compartido para `iosSimulatorArm64`. Esa tarea de compilación no ejecuta la app en iOS: para ello se requiere macOS/Xcode.

## Bitácora manual solicitada y estado

La actividad pide fecha/hora, plataforma, pasos, pantalla, estado local, captura y conclusión para cinco escenarios: apertura sin red con datos previos; registro sin red; reconexión y comprobación del id en Swagger; primera apertura offline con almacenamiento vacío; y sesión expirada sin red. No se recibieron capturas de estos recorridos y no se completan con resultados inventados.

En esta ejecución no había emulador conectado y Docker Desktop estaba apagado, por lo que no se pudo probar el recorrido visual contra PharmaSoft ni generar una respuesta real de Swagger. Para el escenario 5, el perfil mínimo se conserva cifrado (EncryptedSharedPreferences en Android y Keychain en iOS). Si `/auth/me` falla por red, la app entra en modo sin conexión y limita navegación a Inicio/Productos; las altas se guardan localmente y no se presentan como autorizadas por el servidor. Si el servidor devuelve 401, borra token y perfil cacheado, y solicita nuevo login. El diseño permite consultar datos y registrar localmente con sesión no validable, pero aplica los permisos cacheados hasta que haya respuesta del backend; una revocación solo se conoce al reconectar.

La implementación quedó en la rama local `feature/offline-first-guillen`, organizada en los commits `71d2f2a` (persistencia SQLDelight y sincronización) y `ee75e41` (sesión offline, UI y pruebas). No se realizó push a GitHub.

| Escenario | Resultado/evidencia en esta entrega |
| --- | --- |
| 1. Apertura sin red con datos previos | Fallback de perfil y lectura local cubiertos por pruebas separadas; recorrido combinado manual y captura pendientes. |
| 2. Registro sin red | Verificado por prueba automatizada; captura en modo avión pendiente. |
| 3. Reconexión y envío | Verificado con fake remoto/id positivo; verificación real en Swagger pendiente. |
| 4. Primera apertura sin red | Base nueva vacía verificada por el setup de pruebas; recorrido UI/captura pendiente. Sin perfil cacheado se muestra el login; falta confirmar UX de estado vacío tras autorizar acceso local. |
| 5. Sesión expirada sin red | Fallo de red restaura perfil offline y un 401 simulado borra credencial. Falta provocar el escenario visual y capturarlo. |

## Antigüedad de datos en pantalla

La marca de la última sincronización correcta se guarda en la tabla `sincronizacion`. Productos muestra “Actualizado hace menos de un minuto”, minutos, horas o días; antes de la primera sincronización muestra “Nunca sincronizado”. No se adjuntan capturas de menos de un minuto ni de más de un día porque no se pudo recorrer la pantalla en el emulador.

## Guion de sustentación del Producto U2 (8 minutos)

| Minuto | Demostración | Preparación |
| --- | --- | --- |
| 0-1 | Login correcto y credenciales incorrectas (JWT). | Cerrar sesión previamente; tener usuarios admin y vendedor listos. |
| 1-3 | Listar, registrar y actualizar producto contra PharmaSoft (REST + CRUD). | Backend/Oracle levantados, categoría válida y permisos de escritura. |
| 3-5 | Mostrar 403 con usuario sin permiso y error de validación junto al campo. | Sesión de almacen y formulario con datos inválidos preparados. |
| 5-7 | Activar modo avión, consultar caché, registrar pendiente; reconectar y sincronizar; verificar en Swagger. | Producto/caché existente y Swagger abierto. |
| 7-8 | Mostrar mismo inventario en Android e iOS y fecha de actualización. | Emulador Android y dispositivo/simulador iOS con backend disponible. |

## Pendientes para cerrar la entrega

- Ejecutar los cinco escenarios manuales y adjuntar capturas auténticas, con bitácora completa.
- Comprobar en Swagger el producto sincronizado con id positivo.
- Obtener capturas de antigüedad reciente y >24 horas.
- Ejecutar la app en macOS/Xcode y adjuntar evidencia iOS.
- Capturar y analizar la revocación/revalidación del perfil offline cuando vuelva la red.
- Publicar la rama en GitHub si se desea entrega remota; esta tarea no realizó push.
