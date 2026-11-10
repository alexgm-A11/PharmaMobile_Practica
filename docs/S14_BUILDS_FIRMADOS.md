# Sesión 14 — Builds firmados

## Alcance congelado

El paquete Android mantiene el identificador `pe.edu.upeu.pharmamobile`, `versionCode = 1` y `versionName = 1.0.0`. La variante Release activa R8/minificación y usa las reglas de ProGuard del proyecto. La clave de firma se mantiene fuera de Git; el archivo local `keystore.properties` y cualquier JKS/keystore quedan excluidos por `.gitignore`. Existe `keystore.properties.example` sin credenciales.

En el equipo local se creó el almacén `C:\Users\alexg\Documents\PharmaMobileKeys\pharmamobil-release.jks`. La configuración local está en la raíz del proyecto como archivo ignorado. La clave y sus contraseñas son irrecuperables si no se respaldan de forma segura: conservar una copia privada del JKS y sus credenciales. No publicar ninguno de esos elementos.

## Builds y verificación

Los comandos de entrega son:

```powershell
./gradlew.bat :androidApp:bundleRelease :androidApp:assembleRelease
```

Los artefactos se copiaron a `output/android/` y se comprobó la firma del APK con `apksigner verify --verbose --print-certs`. El certificado está a nombre de Alexander Guillen Mendoza, RSA 2048, con SHA-256 `bbea15b5315bd1c594334069b5bd7d14db356a0a1c72485f03fad445a553d76d`; confirmar que coincida con la clave privada resguardada. SHA-256 del APK: `6e13f2aed9240504fdc5c49b065a4495761f8fb47045ca7b1e9b894a86837bce`. SHA-256 del AAB: `1865f7d9508009065a51b37cd1fa8fbc396623f0026107ae292fc6139152e952`. El AAB se genera para distribución por Google Play; el APK sirve para instalación de revisión.

## iOS y matriz física

Una compilación/archive firmado de iOS y un IPA requieren macOS, Xcode, certificados Apple y perfil de aprovisionamiento válidos. El entorno de trabajo es Windows, por lo que no se ha generado ni firmado un IPA. Tampoco se atribuyen pruebas a teléfonos físicos sin dispositivos conectados. Registrar fabricante/modelo/versión, build instalada, flujos ejecutados, resultado y defectos observados en `docs/MATRIZ_PRUEBAS_DISPOSITIVOS.md` al completar esa validación.

## Instalación y checklist

Consulta `docs/GUIA_INSTALACION_Y_CHECKLIST.md` para generar/instalar el APK, verificar la firma y preparar la validación. El tag de versión final debe crearse únicamente después de integrar la rama en `develop` y revisar los artefactos definitivos.
