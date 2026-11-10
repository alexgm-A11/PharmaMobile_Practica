# PharmaMobile_Practica — Kotlin Multiplatform

Aplicación de práctica con Android e iOS. El código común contiene inicio, productos del inventario local, clientes, pedidos demostrativos y catálogo REST separado. El proyecto usa Compose Multiplatform, Clean + MVVM, Koin, corrutinas y Ktor.

## Sesiones 13–14: integrador y distribución

La sesión 13 incorpora control durable de sincronización (intentos, error y estados pendientes/fallidos), reintentos exponenciales limitados, pantalla de estado y contratos de permisos/telemetría por capas. El adaptador Android de Crashlytics queda preparado pero necesita el archivo real `androidApp/google-services.json`; no se incluye configuración Firebase ni secretos. El puente de permisos iOS queda pendiente.

La sesión 14 configura `versionCode = 1`, `versionName = 1.0.0`, R8 y firma Release con un JKS fuera del repositorio. Los builds locales entregados son `output/android/Alexander_Guillen_Mendoza_ProyectoFinal_Android.apk` y `.aab`. El certificado del APK se verificó con SHA-256 `bbea15b5315bd1c594334069b5bd7d14db356a0a1c72485f03fad445a553d76d`. La llave permanece fuera de Git; véase `keystore.properties.example` y respáldese el JKS junto a sus credenciales.

Verificación en Windows: `./gradlew.bat :androidApp:compileDebugKotlin :shared:testAndroidHostTest :shared:compileKotlinIosSimulatorArm64 :androidApp:bundleRelease :androidApp:assembleRelease`. La suite agregada `:shared:allTests` incluye resolución del driver SQLite JVM para iOS y no es ejecutable en este host; las pruebas Android Host sí pasan. No había emulador ni dispositivos físicos conectados. iOS Archive/IPA, validación Firebase y pruebas en dos Android físicos aún requieren sus respectivos equipos/configuraciones. El tag final debe crearse después de integrar la rama de trabajo en `develop`.

Consulta [docs/S13_INTEGRADOR.md](docs/S13_INTEGRADOR.md), [docs/S14_BUILDS_FIRMADOS.md](docs/S14_BUILDS_FIRMADOS.md), [docs/GUIA_INSTALACION_Y_CHECKLIST.md](docs/GUIA_INSTALACION_Y_CHECKLIST.md) y [docs/MATRIZ_PRUEBAS_DISPOSITIVOS.md](docs/MATRIZ_PRUEBAS_DISPOSITIVOS.md). El informe actualizado está en `output/pdf/Informe_Actualizado_PharmaMobile_Sesiones_13_14_Guillen_Mendoza.pdf`.

## Persistencia offline-first (Sesión 11)

La rama `feature/offline-first-guillen` implementa el inventario con SQLDelight 2.1.0. `PharmaDb` y `Producto.sq` viven en `shared/src/commonMain/sqldelight`; `ProductoLocal` es la única capa que conoce las consultas. El driver es Android SQLite en Android y SQLite nativo en iOS, ambos registrados por Koin. En `commonTest` se usa SQLite JDBC en memoria.

`ProductoRepository.observar()` expone un Flow de la base local y el `ProductoViewModel` lo colecta una sola vez. El alta escribe primero con id negativo y estado pendiente; el repositorio intenta enviar a PharmaSoft, confirma la fila con el id real y, al sincronizar, descarga el catálogo sin borrar pendientes. La sincronización correcta queda guardada y visible como tiempo relativo. Si nunca sincronizó, la pantalla lo indica; sin red conserva la lista local y permite reintentar con «SINCRONIZAR AHORA».

El adaptador de alta usa la categoría con id 1 (Analgesicos, creada por V4 del backend) porque el formulario local solo solicita nombre, precio y stock. Si se cambia la semilla de PharmaSoft, habrá que reemplazar esa categoría predeterminada o incorporar su selección al formulario. El perfil mínimo de usuario/permisos se guarda cifrado junto al token (EncryptedSharedPreferences/Keychain); si `/auth/me` no responde por falta de red, se permite modo local, se limita la navegación a Inicio/Productos y las operaciones remotas siguen sujetas a validación del servidor. Un 401 real elimina el perfil cacheado y obliga a iniciar sesión.

Pruebas: `./gradlew.bat :shared:testAndroidHostTest`. La suite incluye seis escenarios offline-first: sincronización, alta sin red, reenvío al reconectar, conservación de pendientes, separación de ids y ausencia de duplicados; además verifica el fallback de sesión y el menú restringido sin red. La compilación verificada también incluye `:androidApp:assembleDebug` y `:shared:compileKotlinIosSimulatorArm64`. Las capturas manuales de modo avión, Swagger y ejecución iOS deben registrarse por separado; Windows no puede arrancar el simulador iOS.

Consulta [docs/S11_OFFLINE_FIRST.md](docs/S11_OFFLINE_FIRST.md) para flujo, verificaciones y pendientes de evidencia.

## Autenticación JWT y PharmaSoft (Sesión 10)

La rama local `feature/auth-jwt-guillen` agrega login contra `POST /api/v1/auth/login`, recuperación de sesión con `GET /api/v1/auth/me`, cierre de sesión y CRUD REST autenticado de productos y clientes en dos secciones nuevas del menú. El cliente Ktor de PharmaSoft usa el plugin Auth para agregar `Authorization: Bearer` automáticamente, evita enviarlo al login y evita reintentar un 401 de login con un token viejo, sanea esa cabecera en el log de depuración y no registra el cuerpo con la contraseña. Un 401 en una ruta protegida borra el token y devuelve al login; un 403 conserva la sesión y muestra falta de permiso. Las opciones de navegación y escritura dependen de los permisos recibidos del servidor.

`AlmacenSeguro` guarda el token cifrado con Android Keystore/EncryptedSharedPreferences en Android y Keychain en iOS. La URL base es `http://10.0.2.2:8080/api/v1/` en el emulador Android y `http://localhost:8080/api/v1/` en el simulador iOS. El tráfico HTTP claro se permite solo en la compilación Android `debug`; para producción se debe usar HTTPS. El catálogo público de Platzi conserva un cliente HTTP separado y nunca recibe el JWT.

Usuarios **solo de prueba** definidos por la guía y los datos semilla del servidor: `admin` / `Admin123`, `vendedor` / `Vendedor123`, `almacen` / `Almacen123`. No están precargados en la app ni deben reutilizarse en producción.

Para compilar y ejecutar las pruebas simuladas en Windows: `./gradlew.bat :shared:testAndroidHostTest :androidApp:assembleDebug :shared:compileKotlinIosSimulatorArm64`. El proyecto hermano `C:\Users\alexg\AndroidStudioProjects\pharmaSoft` ya incorpora la seguridad JWT y un `compose.yaml` para Oracle y el backend. La app se ajustó al campo real `empleadoNombre` que devuelve `/auth/me`. Tras reiniciar Windows, ambos contenedores arrancaron, Flyway aplicó V1-V4 y se verificaron por HTTP el login y los permisos 401/200/403. Falta la prueba manual completa desde la app. Consulta [docs/S10_AUTENTICACION_JWT.md](docs/S10_AUTENTICACION_JWT.md) para arquitectura, pruebas y pendientes.

## Capacidades nativas (Sesión 9)

La implementación de la sesión 9 añade dos capacidades nativas sin importar clases de Android ni UIKit desde `presentation`:

| Capacidad | Código compartido | Android | iOS |
| --- | --- | --- | --- |
| Moneda peruana | `platform/Formato.kt` declara `expect fun formatearSoles(Double)`; `presentation/producto/ProductoUiState.kt` la usa al convertir `Producto` en `ProductoUi`. | `platform/Formato.android.kt` usa `NumberFormat` con `es-PE`. | `platform/Formato.ios.kt` usa `NSNumberFormatter` con `es_PE`. |
| Compartir producto | `domain/platform/Compartidor.kt` declara la interfaz; `domain/usecase/TextoParaCompartir.kt` arma nombre, precio y stock. `presentation/detalle` expone la acción. | `platform/CompartidorAndroid.kt` abre el selector `ACTION_SEND` con `FLAG_ACTIVITY_NEW_TASK`; `di/PlatformModule.android.kt` registra la implementación con Koin. | `platform/CompartidorIos.kt` presenta `UIActivityViewController`; `di/PlatformModule.ios.kt` registra la implementación. |

Desde **Productos**, toca `DETALLE` en un artículo y luego `Compartir`. Este inventario local ahora es la caché offline-first de PharmaSoft y conserva stock; el catálogo REST de la sesión 7 sigue usando la API pública de Platzi por separado.

La función `expect` y sus dos `actual` tienen exactamente el mismo paquete y la misma firma. El `Compartidor` se inyecta en `DetalleProductoViewModel`: es reemplazable en pruebas, mientras Compose solo recibe un callback. `MainApplication` proporciona `androidContext` a Koin. La interfaz Swift existente importa `Shared` y llama a `MainViewControllerKt.MainViewController()` y `AppModuleKt.initKoinIos()`.

Verificación local: `./gradlew.bat :shared:testAndroidHostTest :androidApp:assembleDebug :shared:compileKotlinIosSimulatorArm64`. La compilación del código compartido iOS en Windows no acredita que el selector nativo funcione en un simulador: la captura iOS y la ejecución de Xcode requieren macOS. Consulta `docs/S09_CAPACIDADES_NATIVAS.md` para cotejo, diferencias y evidencias disponibles.

## Conectividad REST (Sesión 7)

El proyecto conserva una pantalla independiente **Catálogo público (Platzi)** en el menú lateral. Consulta productos de esa API de práctica mediante Ktor, sin sustituir el inventario local ni asignar un stock ficticio a los datos remotos. La URL base es `https://api.escuelajs.co/api/v1` (versión `v1`).

- `ProductoApi` ejecuta `GET /products?limit=10&offset=0` y `GET /products/{id}` con un tiempo máximo de diez segundos.
- `ProductoDto` y `CategoriaDto` representan el JSON. `ignoreUnknownKeys = true` admite campos nuevos del servidor; el repositorio convierte los DTO en `ProductoCatalogo`.
- `CatalogoViewModel` expone carga, lista y mensajes de error controlados. La pantalla permite recargar y buscar por ID.
- El cliente registra petición y respuesta con Ktor Logging. En Android la salida se consulta en Logcat; en iOS, en la consola de Xcode. No se envían credenciales.
- El catálogo de los cinco endpoints CRUD y el diccionario de DTO están en `docs/S07_ActividadAutonoma_Guillen_Mendoza.pdf`. POST, PUT y DELETE se verificaron contra la API, pero su integración en la interfaz se reserva para la sesión 8.
- Cinco pruebas de conexión reproducibles con `MockEngine`: `./gradlew.bat :shared:testAndroidHostTest` en Windows. También se verificó `:androidApp:assembleDebug` y `:shared:compileKotlinIosSimulatorArm64`.

Para ejecutar en Android, abre este proyecto en Android Studio y ejecuta `androidApp` en un emulador con Internet. Para ejecutar en iOS se requiere macOS, Xcode y el proyecto `iosApp`; compilar el módulo compartido en Windows no equivale a ejecutar en el simulador iOS.

La API es pública y mutable: sus productos y códigos de error pueden variar. En la comprobación del 22/09/2026, un ID inexistente respondió HTTP 400, aunque la actividad propone 404. La interfaz controla ambos códigos.

## Estructura y ejecución

- `shared/src/commonMain`: modelos, contratos, casos de uso, datos comunes, ViewModels, pantallas Compose, navegación y módulos Koin.
- `shared/src/androidMain` y `shared/src/iosMain`: implementaciones nativas de formato y compartir, junto con los `platformModule`.
- `androidApp` y `iosApp`: puntos de entrada específicos de cada sistema operativo.
- `shared/src/commonTest`: pruebas de reglas de negocio, API con MockEngine y capacidades nativas.

Abre esta carpeta en Android Studio y ejecuta `androidApp`. Para iOS se necesita macOS y Xcode con `iosApp`. En Windows se puede compilar el código compartido iOS, pero no ejecutar el simulador. Para verificar localmente: `./gradlew.bat :shared:testAndroidHostTest :androidApp:assembleDebug :shared:compileKotlinIosSimulatorArm64`.

## Estado conocido

El inventario de productos usa SQLDelight y sincroniza contra PharmaSoft; clientes locales siguen siendo demostrativos, mientras la sección de clientes del servidor conserva su CRUD JWT. Pedidos contiene únicamente modelos y una pantalla demostrativa; no hay CRUD de pedidos. El catálogo REST de Platzi solo ofrece lectura. Las seis pruebas offline-first y compilaciones Android/iOS shared pasaron localmente. Falta la bitácora con las cinco capturas manuales de la actividad autónoma, las evidencias de Swagger del ciclo offline y la ejecución iOS en macOS/Xcode. La rama offline-first está creada localmente; no se ha publicado a GitHub en esta tarea.
