# Sesión 10 - Autenticación JWT en PharmaMobile_Practica

## Alcance implementado

La app añade una pantalla de login sin credenciales precargadas, recuperación de sesión con `/auth/me`, cierre de sesión, almacenamiento protegido del JWT y un cliente Ktor exclusivo para PharmaSoft. Dos nuevas secciones consumen el CRUD REST de productos y clientes con ese cliente: listar, crear, actualizar y eliminar. Las pantallas locales anteriores permanecen como demostraciones y están rotuladas como tales; Pedidos sigue siendo una demostración, no un CRUD REST.

La integración se desarrolló sobre `feature/auth-jwt-guillen` desde `master`, que ya contenía la sesión 9. El `develop` local seguía en una revisión antigua de la sesión 7; iniciar allí habría descartado el trabajo posterior. Esta decisión no cambia el backend ni la base de datos.

## Contrato y arquitectura

| Capa | Archivos clave | Responsabilidad |
| --- | --- | --- |
| Dominio | `domain/model/Sesion.kt`, `ErrorSesion.kt`, `repository/AuthRepository.kt`, `usecase/SesionUseCases.kt` | Credenciales, permisos y reglas sin Ktor ni APIs nativas. |
| Datos comunes | `data/remote/ClientePharmaSoft.kt`, `repository/AuthRepositorioRest.kt`, `repository/PharmaSoftRepositorioRest.kt` | Login, `/auth/me`, Auth Bearer, errores 401/403 y operaciones REST. |
| Android | `data/local/AlmacenSeguroAndroid.kt`, `di/PlatformModule.android.kt` | Preferencias cifradas, Android Keystore y configuración de depuración. |
| iOS | `data/local/AlmacenSeguroIos.kt`, `di/PlatformModule.ios.kt` | Keychain y configuración de depuración. |
| Presentación | `presentation/sesion`, `presentation/pharmasoft`, `App.kt` | Login, formularios, estados, navegación y acciones visibles por permiso. |

`pharmaSoftBaseUrl` utiliza `expect/actual`: Android `http://10.0.2.2:8080/api/v1/`, iOS `http://localhost:8080/api/v1/`. El cliente público de Platzi es independiente y no puede recibir el JWT de PharmaSoft. El cliente autenticado deshabilita redirecciones, instala ContentNegotiation, DefaultRequest, Auth Bearer, HttpTimeout y Logging. `sendWithoutRequest` envía el token en la primera petición solo al host y ruta `/api/v1/` de PharmaSoft, nunca al login. El login además marca la solicitud con `AuthCircuitBreaker`: una respuesta 401 no se reintenta con un token viejo. Este caso se descubrió y corrigió con una prueba específica. Tras guardar o borrar el token se llama a `clearToken()`.

El log registra solo cabeceras en modo de depuración y sustituye `Authorization` por `***`; fuera de depuración queda en `NONE`. Android permite HTTP claro solo en el manifiesto `debug`, y `allowBackup=false` evita restaurar las preferencias cifradas sin su llave. Para despliegue real se debe cambiar a HTTPS. AndroidX marca `EncryptedSharedPreferences` como API obsoleta, pero aquí se usa para coincidir con la guía; una evolución posterior debería emplear Android Keystore directamente.

## Flujo de sesión

1. Inicio: si no hay token, se presenta LoginScreen. Si existe, `/auth/me` lo valida y recupera permisos; un fallo de red no borra el token y permite reintentar.
2. Login: `POST /auth/login` recibe el DTO del servidor, guarda el token en `AlmacenSeguro` y muestra la aplicación. La contraseña no queda en el estado después del resultado.
3. Petición protegida: Ktor añade Bearer automáticamente. Las pantallas PharmaSoft no construyen la cabecera.
4. 401 protegido: se elimina el token, se notifica al ViewModel y se vuelve al login con aviso de expiración. El 401 del propio login se interpreta como credenciales inválidas.
5. 403: se conserva token y sesión; la pantalla muestra falta de permisos. El servidor es la autoridad final.
6. Cierre: se elimina el token protegido, se limpia la caché del proveedor Bearer y se muestra el login.

Los permisos se usan para ocultar secciones y botones. `CATALOGO_LEER` permite ver productos; `CATALOGO_ESCRIBIR` permite crearlos, editarlos y eliminarlos; `CLIENTES_LEER` permite ver clientes; `CLIENTES_ESCRIBIR` habilita su escritura. `almacen` no ve la sección de clientes. Esto no sustituye la autorización del servidor.

## Análisis de los adjuntos

La colección Postman proporcionada contiene 63 solicitudes: login correcto e incorrecto, tokens ausentes, malformados, alterados o expirados, la matriz de permisos de ADMIN/VENDEDOR/ALMACENERO, exposición de datos y CORS. Su variable `baseUrl` es `http://localhost:8080` para ejecución en el equipo anfitrión; en el emulador Android se usa `10.0.2.2`. Los valores de token de la colección están vacíos hasta ejecutar sus flujos de login. No se incorporaron tokens ni contraseñas reales al código.

El RAR contiene solo dos migraciones del servidor: `V2__seguridad_empleados_usuarios.sql` define empleados, roles, usuarios y la relación usuario-rol; `V3__datos_iniciales_seguridad.sql` carga empleados y usuarios de prueba con contraseñas BCrypt. **No es una base de datos móvil ni contiene el código Java de JWT.** La segunda línea de V2 del RAR es `BD SEGURIDAD` sin prefijo de comentario SQL; tal como está, provocaría un error de sintaxis. El repositorio original actualizado de PharmaSoft ya incluye una V2 corregida, V3 y toda la implementación de seguridad; se usó esa versión sin modificar el RAR.

La carpeta local `C:\Users\alexg\AndroidStudioProjects\pharmaSoft` se actualizó por avance rápido a la revisión de seguridad del repositorio original. Contiene `AuthController`, filtro JWT, reglas de Spring Security y V1/V2/V3; además se prepararon `compose.yaml`, un perfil `container` y V4 con categorías iniciales. Se corrigió su configuración de CORS para proveer explícitamente el bean que requiere `SecurityConfig`. La respuesta real de `/auth/me` usa `empleadoNombre`, no `nombreCompleto`, y el DTO móvil ahora la mapea. La prueba integrada sigue pendiente porque Docker Desktop informa motor Linux detenido (`hasNoVirtualization: true`) y no pudo iniciar Oracle.

## Verificación ejecutada

Comando: `./gradlew.bat :shared:testAndroidHostTest :androidApp:assembleDebug :shared:compileKotlinIosSimulatorArm64`.

Las pruebas con MockEngine verifican: login sin token y sin reintento ante 401 aunque exista uno viejo, Bearer en la primera petición posterior, recuperación de sesión, logout, 401 protegido que borra token, 403 que lo conserva, credenciales inválidas, saneamiento del log y GET/POST/PUT/DELETE autenticados de productos/clientes. Las pruebas de navegación comprueban que `almacen` no ve clientes ni ventas. El conjunto de pruebas de host Android terminó correctamente. El APK `debug` y el Kotlin compartido para iOS compilaron.

En total pasaron 28 pruebas, incluyendo el contrato corregido de `/auth/me`. El APK se instaló y abrió en un emulador Pixel 6 sin excepción de arranque. Esto no valida el inicio de sesión ni las operaciones REST reales: el motor de contenedores no estuvo disponible.

## Pendiente para la evidencia de la guía

- Habilitar/reparar Docker Desktop y WSL 2, luego ejecutar `docker compose up --build -d` en `pharmaSoft`. Flyway debe aplicar V1-V4 y levantar `/api/v1/auth/login` y `/api/v1/auth/me`.
- Ejecutar la colección Postman y conservar capturas de 401 sin token, 200 con admin y 403 de `almacen` al consultar clientes.
- Ejecutar la app en emulador Android y simulador iOS con ese backend, verificar persistencia tras reinicio y capturar login, CRUD, 403 y log con `Authorization: ***`.
- El RAR no se debe copiar a `shared/src`: pertenece a Flyway del servidor. La ejecución iOS requiere macOS/Xcode; compilar el source set desde Windows no demuestra el Keychain en funcionamiento.

Usuarios **solo de desarrollo**, según la guía: admin/Admin123, vendedor/Vendedor123 y almacen/Almacen123. Sustitúyelos antes de publicar cualquier servidor accesible desde Internet.
