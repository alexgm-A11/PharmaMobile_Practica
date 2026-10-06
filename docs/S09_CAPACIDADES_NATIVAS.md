# Sesión 09 - Capacidades nativas en PharmaMobile_Practica

## Qué se implementó

1. `expect fun formatearSoles(valor: Double)` en `commonMain`; `actual` en Android con `NumberFormat(es-PE)` y en iOS con `NSNumberFormatter(es_PE)`.
2. Conversión de precio en `ProductoUi.from`, no dentro del composable ni del modelo `Producto`.
3. Contrato `Compartidor` en `domain/platform` y función común `Producto.comoTextoParaCompartir()`.
4. `CompartidorAndroid` con `ACTION_SEND`/`FLAG_ACTIVITY_NEW_TASK`; `CompartidorIos` con `UIActivityViewController`.
5. Registros específicos de Koin en ambos `platformModule`.
6. Pantalla de detalle local y botón `Compartir`, conectado a `DetalleProductoViewModel`.

## Diferencias por plataforma

| Aspecto | Android | iOS |
| --- | --- | --- |
| Formateo de precio | Java `NumberFormat` con configuración regional `es-PE`. | Foundation `NSNumberFormatter` con `es_PE`. |
| Compartir | Abre el selector de aplicaciones del sistema con `Intent.ACTION_SEND`. | Presenta la hoja nativa con `UIActivityViewController`. |
| Dependencia para compartir | Koin entrega el `Context` de aplicación y por eso se usa `FLAG_ACTIVITY_NEW_TASK`. | Se utiliza el controlador raíz de la ventana de la aplicación. |
| Arranque | `MainApplication` inicia Koin antes de que `MainActivity` muestre `App()`. | Swift inicia Koin y `MainViewControllerKt.MainViewController()` presenta el mismo `App()`. |

## Arquitectura y alcance real

`ProductoScreen` abre `DetalleProductoScreen` pasando el ID. El ViewModel recupera el producto a través de `ProductoRepository` y construye `ProductoUi` con el precio nativo. Al compartir, toma el modelo de dominio, genera el texto en código común y delega a `Compartidor`. Ningún archivo de `presentation` importa `android.*` ni `platform.UIKit.*`.

El detalle corresponde al inventario local. El catálogo remoto de Platzi no proporciona stock. El CRUD de PharmaSoft que presupone la guía 09 aún no forma parte de esta rama; cambiar la fuente de datos requerirá integrar previamente ese backend, no inventar un campo de stock remoto.

## Evidencia comprobable

- Compilación Android: `:androidApp:assembleDebug`.
- Pruebas locales: `:shared:testAndroidHostTest`, incluidas dos pruebas nuevas de formato y texto compartido.
- Compilación del código Kotlin iOS: `:shared:compileKotlinIosSimulatorArm64`.
- El emulador Pixel 6 se detectó, pero su arranque quedó sin completar y `adb shell` no respondió durante la verificación. Por ello no se adjunta una captura Android ni se afirma que el selector se probó visualmente.

## Pendiente por limitación de entorno

- Ejecutar `iosApp` en Xcode y capturar el listado y la hoja de compartir iOS. Windows no tiene simulador iOS.
- Reintentar el emulador Android y capturar el listado con precio en soles y el selector de compartir.
- Captura del error temporal del punto de control 1: no se guardó durante el estado intermedio. El código final sí verifica que existen ambas declaraciones `actual`.
- Enlace remoto y revisión de pareja: la rama es local y no se publicó; no se fabrican commits ni evidencias de otra persona.
