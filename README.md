# PharmaMobile_Practica — Kotlin Multiplatform

Aplicación de práctica con Android e iOS. El código común contiene inicio, productos del inventario local, clientes, pedidos demostrativos y catálogo REST separado. El proyecto usa Compose Multiplatform, Clean + MVVM, Koin, corrutinas y Ktor.

## Autenticación JWT y PharmaSoft (Sesión 10)

La rama local `feature/auth-jwt-guillen` agrega login contra `POST /api/v1/auth/login`, recuperación de sesión con `GET /api/v1/auth/me`, cierre de sesión y CRUD REST autenticado de productos y clientes en dos secciones nuevas del menú. El cliente Ktor de PharmaSoft usa el plugin Auth para agregar `Authorization: Bearer` automáticamente, evita enviarlo al login y evita reintentar un 401 de login con un token viejo, sanea esa cabecera en el log de depuración y no registra el cuerpo con la contraseña. Un 401 en una ruta protegida borra el token y devuelve al login; un 403 conserva la sesión y muestra falta de permiso. Las opciones de navegación y escritura dependen de los permisos recibidos del servidor.

`AlmacenSeguro` guarda el token cifrado con Android Keystore/EncryptedSharedPreferences en Android y Keychain en iOS. La URL base es `http://10.0.2.2:8080/api/v1/` en el emulador Android y `http://localhost:8080/api/v1/` en el simulador iOS. El tráfico HTTP claro se permite solo en la compilación Android `debug`; para producción se debe usar HTTPS. El catálogo público de Platzi conserva un cliente HTTP separado y nunca recibe el JWT.

Usuarios **solo de prueba** definidos por la guía y los datos semilla del servidor: `admin` / `Admin123`, `vendedor` / `Vendedor123`, `almacen` / `Almacen123`. No están precargados en la app ni deben reutilizarse en producción.

Para compilar y ejecutar las pruebas simuladas en Windows: `./gradlew.bat :shared:testAndroidHostTest :androidApp:assembleDebug :shared:compileKotlinIosSimulatorArm64`. El proyecto hermano `C:\Users\alexg\AndroidStudioProjects\pharmaSoft` ya incorpora la seguridad JWT publicada por su repositorio original y un `compose.yaml` para Oracle y el backend. La app se ajustó al campo real `empleadoNombre` que devuelve `/auth/me`. Docker Desktop en este equipo informa que el motor Linux está detenido por falta de virtualización disponible, por lo que la prueba HTTP real aún no pudo ejecutarse. Consulta [docs/S10_AUTENTICACION_JWT.md](docs/S10_AUTENTICACION_JWT.md) para arquitectura, pruebas y pendientes.

## Capacidades nativas (Sesión 9)

La implementación de la sesión 9 añade dos capacidades nativas sin importar clases de Android ni UIKit desde `presentation`:

| Capacidad | Código compartido | Android | iOS |
| --- | --- | --- | --- |
| Moneda peruana | `platform/Formato.kt` declara `expect fun formatearSoles(Double)`; `presentation/producto/ProductoUiState.kt` la usa al convertir `Producto` en `ProductoUi`. | `platform/Formato.android.kt` usa `NumberFormat` con `es-PE`. | `platform/Formato.ios.kt` usa `NSNumberFormatter` con `es_PE`. |
| Compartir producto | `domain/platform/Compartidor.kt` declara la interfaz; `domain/usecase/TextoParaCompartir.kt` arma nombre, precio y stock. `presentation/detalle` expone la acción. | `platform/CompartidorAndroid.kt` abre el selector `ACTION_SEND` con `FLAG_ACTIVITY_NEW_TASK`; `di/PlatformModule.android.kt` registra la implementación con Koin. | `platform/CompartidorIos.kt` presenta `UIActivityViewController`; `di/PlatformModule.ios.kt` registra la implementación. |

Desde **Inventario local (demo)**, toca `DETALLE` en un artículo y luego `Compartir`. Este detalle usa el inventario local porque es el único producto de esta sección que tiene `stock`. El catálogo REST de la sesión 7 usa la API pública de Platzi y no trae stock; no se mezcló con el inventario ni se inventaron existencias. La sección nueva de productos PharmaSoft usa los DTO del servidor y no sustituye esta demostración local.

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

El inventario local permite registrar/listar productos y clientes. Pedidos contiene únicamente modelos y una pantalla demostrativa; no hay CRUD de pedidos. El catálogo REST de Platzi solo ofrece lectura (GET listado y detalle). La sesión 10 agrega login JWT y CRUD REST de productos/clientes; el backend correspondiente está preparado para contenedores, pero su verificación en vivo está pendiente porque Docker Desktop no inicia el motor Linux en este equipo. Persistencia offline-first y CRUD REST de ventas siguen pendientes. Las capacidades nativas S09 se desarrollaron sobre el detalle de producto local. Esta rama de sesión 10 es local y no se ha publicado desde esta tarea.
