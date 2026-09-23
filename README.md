# PharmaMobile — Sesión 2 / Reto 02: Corrutinas y Flow en KMP

## Conectividad REST (Sesión 7)

El proyecto ahora incluye una pantalla independiente **Catálogo REST** en el menú lateral. Consulta productos reales de la API pública de práctica de Platzi mediante Ktor, sin sustituir el inventario local ni asignar un stock ficticio a los datos remotos. La URL base es `https://api.escuelajs.co/api/v1` (versión `v1`).

- `ProductoApi` ejecuta `GET /products?limit=10&offset=0` y `GET /products/{id}` con un tiempo máximo de diez segundos.
- `ProductoDto` y `CategoriaDto` representan el JSON. `ignoreUnknownKeys = true` admite campos nuevos del servidor; el repositorio convierte los DTO en `ProductoCatalogo`.
- `CatalogoViewModel` expone carga, lista y mensajes de error controlados. La pantalla permite recargar y buscar por ID.
- El cliente registra petición y respuesta con Ktor Logging. En Android la salida se consulta en Logcat; en iOS, en la consola de Xcode. No se envían credenciales.
- El catálogo de los cinco endpoints CRUD y el diccionario de DTO están en `docs/S07_ActividadAutonoma_Guillen_Mendoza.pdf`. POST, PUT y DELETE se verificaron contra la API, pero su integración en la interfaz se reserva para la sesión 8.
- Cinco pruebas de conexión reproducibles con `MockEngine`: `./gradlew.bat :shared:testAndroidHostTest` en Windows. También se verificó `:androidApp:assembleDebug` y `:shared:compileKotlinIosSimulatorArm64`.

Para ejecutar en Android, abre este proyecto en Android Studio y ejecuta `androidApp` en un emulador con Internet. Para ejecutar en iOS se requiere macOS, Xcode y el proyecto `iosApp`; compilar el módulo compartido en Windows no equivale a ejecutar en el simulador iOS.

La API es pública y mutable: sus productos y códigos de error pueden variar. En la comprobación del 22/09/2026, un ID inexistente respondió HTTP 400, aunque la actividad propone 404. La interfaz controla ambos códigos.

---

Este paquete contiene únicamente los archivos que hay que copiar/fusionar
dentro de tu proyecto local `PharmaMobile` (los mismos paths de `shared/src`).

## Cómo aplicarlo
1. Con el proyecto cerrado en Android Studio, copia el contenido de
   `shared/src/commonMain/kotlin/...` y `shared/src/commonTest/kotlin/...`
   dentro de las mismas rutas de tu repo local.
2. Copia `shared/build.gradle.kts` (reemplaza el tuyo) y agrega las líneas
   de `libs.versions.toml` a tu `gradle/libs.versions.toml` (sección
   `[versions]` y `[libraries]`).
3. Sync de Gradle en Android Studio para descargar `kotlinx-coroutines-core`
   y `kotlinx-coroutines-test`.

## Archivos nuevos
- `domain/result/ResultadoProductos.kt` — sealed class Cargando/Exito/Error (Pasos 6-8).
- `data/repository/ProductoRepository.kt` — productos simulados, `suspend fun obtenerProductos()`,
  `observarEstados(): Flow<String>`, `observarProductos(): Flow<List<Producto>>` (con `copy()`),
  `cargarProductos(): Flow<ResultadoProductos>` (Pasos 3 a 17).
- `demo/DemoAsincrono.kt` — función que ejecuta y muestra por consola cada operación.
- `commonTest/.../demo/DemoAsincronoTest.kt` — test con `runTest` para generar la
  evidencia de ejecución (Paso 21): corre el test y captura la salida de consola.

## Comandos Git sugeridos (Pasos 1 y 22)
```
cd C:\dev\PharmaMobile
git status
# ...aplicar los archivos...
git status
git add .
git commit -m "feat: agregar corrutinas y flujo de productos"
```

## Pendiente de tu parte
- Ejecutar `DemoAsincronoTest` en Android Studio y guardar la captura de
  consola como evidencia.
- Elaborar el informe PDF `Apellido_Nombre_Sesion02_Reto02.pdf` (indícame
  tu apellido y nombre si quieres que te arme el documento).
