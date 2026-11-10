# Guía de instalación y checklist de entrega

## Android

1. Abrir el proyecto en Android Studio y sincronizar Gradle.
2. Conectar un emulador o dispositivo Android con depuración USB habilitada.
3. Para compilar e instalar depuración: `./gradlew.bat :androidApp:installDebug`.
4. Para generar distribución firmada: `./gradlew.bat :androidApp:bundleRelease :androidApp:assembleRelease`.
5. Instalar el APK de revisión con `adb install -r output/android/Alexander_Guillen_Mendoza_ProyectoFinal_Android.apk` o desde el directorio de salida de Gradle si aún no se copió.
6. Confirmar la firma con `apksigner verify --verbose --print-certs <APK>` y registrar la huella.

El keystore no forma parte del repositorio. En una máquina nueva, colocar un JKS privado y crear `keystore.properties` a partir de `keystore.properties.example`. No distribuir credenciales ni subir el JKS.

## Lista de control

- [ ] Revisar inicio de sesión y recuperación/cierre de sesión.
- [ ] Crear un producto conectado; confirmar sincronización y lista local.
- [ ] Crear un producto sin red; reconectar y comprobar estado y reintento.
- [ ] Revisar mensajes de rechazo permanente y reintento manual.
- [ ] Comprobar cámara, ubicación y notificaciones en Android, incluidos denegación y Ajustes.
- [ ] Ejecutar la misma matriz en dos dispositivos Android físicos y anotar defectos.
- [ ] Probar iOS en macOS/Xcode, solicitudes nativas y archivo firmado/IPA.
- [ ] Configurar Firebase de la aplicación Android y enviar un evento de prueba a Crashlytics.
- [ ] Integrar cambios en `develop`, revisar AAB/APK y entonces crear el tag final.

Las casillas no marcadas requieren evidencia de ejecución antes de declarar la entrega validada.
