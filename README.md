# PharmaMobile — Sesión 2 / Reto 02: Corrutinas y Flow en KMP

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
