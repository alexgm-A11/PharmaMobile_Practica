# PharmaSoft local para PharmaMobile

El backend Spring Boot y Oracle Free se ejecutan con Docker Compose. Flyway crea el esquema desde V1, aplica la seguridad de V2/V3 y carga tres categorias iniciales en V4 para que el formulario de productos tenga opciones. Las migraciones originales del RAR no se copiaron: la version V2 del repositorio oficial ya corrige su linea SQL invalida y aporta tambien el codigo JWT.

## Inicio

Requiere Docker Desktop con motor Linux y virtualizacion activa. En esta carpeta ya existe un `.env` local (ignorado por Git); para otra computadora, copia `.env.example` a `.env` y sustituye las tres claves. `JWT_SECRET` debe ser Base64 de al menos 32 bytes aleatorios. No publiques `.env`.

```powershell
docker compose up --build -d
docker compose ps
docker compose logs --tail=100 backend
```

La primera creacion de Oracle puede tardar varios minutos. El volumen `oracle_data` conserva datos entre reinicios. No uses `docker compose down -v` si quieres conservarlos.

- API y Swagger: `http://localhost:8080/swagger-ui.html`
- Salud: `http://localhost:8080/api/health`
- Oracle desde Windows: `localhost:1522/FREEPDB1`, usuario `PHARMADB`, contraseña `DB_PASSWORD` de `.env`.
- Android emulador: `http://10.0.2.2:8080/api/v1/`; iOS simulador: `http://localhost:8080/api/v1/`.

Usuarios semilla **solo para desarrollo**: `admin` / `Admin123`, `vendedor` / `Vendedor123`, `almacen` / `Almacen123`. Cambia las credenciales y habilita HTTPS antes de usarlo fuera de la computadora local. Los puertos de Compose se publican solo en `127.0.0.1`.

## Comprobacion HTTP

Primero, `GET /api/v1/productos` sin token debe devolver 401. `POST /api/v1/auth/login` con `{"username":"admin","password":"Admin123"}` devuelve el JWT; con `Authorization: Bearer <token>` el GET debe devolver 200. El token de `almacen` en `GET /api/v1/clientes` debe dar 403. La coleccion Postman de la sesion 10 contiene el resto de la matriz.

## Estado de verificacion en este equipo

El proyecto Maven compila, 22 pruebas unitarias de seguridad/productos/ventas pasan y la definicion de Compose pasa `docker compose config --quiet`. La prueba `PharmaBackendApplicationTests.contextLoads` no puede ejecutarse sin Oracle; falla por ausencia de listener en `localhost:1522`, no por compilacion. Docker Desktop, sin embargo, informa `Status: stopped` y su backend registra `hasNoVirtualization: true`; `wsl -l -v` no lista ninguna distribucion. Por ello no se pudo crear el contenedor Oracle ni ejecutar Flyway o la matriz HTTP real en este equipo. Antes de usar `docker compose up`, hay que reparar/habilitar el motor Linux de Docker Desktop y WSL 2; puede requerir permisos de administrador y reiniciar Windows.
