package pe.edu.upeu.pharmamobile

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.logging.Logger
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobile.data.local.EventosSesion
import pe.edu.upeu.pharmamobile.data.remote.crearClientePharmaSoft
import pe.edu.upeu.pharmamobile.data.repository.AuthRepositorioRest
import pe.edu.upeu.pharmamobile.data.repository.PharmaSoftRepositorioRest
import pe.edu.upeu.pharmamobile.domain.model.Credenciales
import pe.edu.upeu.pharmamobile.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobile.domain.model.Permisos
import pe.edu.upeu.pharmamobile.domain.model.SesionException
import pe.edu.upeu.pharmamobile.domain.model.ProductoEntrada
import pe.edu.upeu.pharmamobile.domain.model.ClienteEntrada

class AuthPharmaSoftTest {
    private class Memoria : AlmacenSeguro {
        var token: String? = null
        override fun guardarToken(token: String) { this.token = token }
        override fun leerToken() = token
        override fun borrarToken() { token = null }
    }

    private val jsonLogin = """{"token":"jwt-secreto","tipo":"Bearer","expiraEnSegundos":3600,"username":"admin","nombreCompleto":"Carlos Ramírez","roles":["ADMIN"],"permisos":["CATALOGO_LEER","CATALOGO_ESCRIBIR","CLIENTES_LEER"]}"""
    private val jsonMe = """{"id":1,"username":"admin","empleadoId":1,"empleadoNombre":"Carlos Ramírez","cargo":"Administrador","roles":["ADMIN"],"permisos":["CATALOGO_LEER","CLIENTES_LEER"],"estado":true,"fechaCreacion":"2026-10-13T10:00:00"}"""
    private val cabeceras = headersOf(HttpHeaders.ContentType, "application/json")

    @Test
    fun login_no_envia_token_y_luego_bearer_sale_en_primera_peticion() = runTest {
        val almacen = Memoria()
        val rutas = mutableListOf<String>()
        val autorizaciones = mutableListOf<String?>()
        val motor = MockEngine { request ->
            rutas += request.url.encodedPath
            autorizaciones += request.headers[HttpHeaders.Authorization]
            when (request.url.encodedPath) {
                "/api/v1/auth/login" -> respond(jsonLogin, HttpStatusCode.OK, cabeceras)
                "/api/v1/auth/me" -> respond(jsonMe, HttpStatusCode.OK, cabeceras)
                else -> respond("{}", HttpStatusCode.NotFound, cabeceras)
            }
        }
        val cliente = crearClientePharmaSoft(almacen, EventosSesion(), false,
            "http://localhost:8080/api/v1/", motor)
        try {
            val repo = AuthRepositorioRest(cliente, almacen)
            val sesion = repo.iniciarSesion(Credenciales("admin", "Admin123"))
            assertTrue(sesion.puede(Permisos.CATALOGO_ESCRIBIR))
            assertEquals("jwt-secreto", almacen.token)
            val recuperada = repo.sesionGuardada()
            assertEquals("admin", recuperada?.usuario)
            assertEquals("Carlos Ramírez", recuperada?.nombreCompleto)
            assertEquals(listOf("/api/v1/auth/login", "/api/v1/auth/me"), rutas)
            assertEquals(listOf(null, "Bearer jwt-secreto"), autorizaciones)
            repo.cerrarSesion()
            assertNull(almacen.token)
        } finally { cliente.close() }
    }

    @Test
    fun sesion_se_recupera_con_un_cliente_nuevo_desde_el_almacen() = runTest {
        val almacen = Memoria().apply { token = "jwt-persistido" }
        val motor = MockEngine { request ->
            assertEquals("/api/v1/auth/me", request.url.encodedPath)
            assertEquals("Bearer jwt-persistido", request.headers[HttpHeaders.Authorization])
            respond(jsonMe, HttpStatusCode.OK, cabeceras)
        }
        val clienteNuevo = crearClientePharmaSoft(almacen, EventosSesion(), false,
            "http://localhost:8080/api/v1/", motor)
        try {
            assertEquals("admin", AuthRepositorioRest(clienteNuevo, almacen).sesionGuardada()?.usuario)
        } finally { clienteNuevo.close() }
    }

    @Test
    fun un_401_protegido_borra_token_y_notifica_expiracion() = runTest {
        val almacen = Memoria().apply { token = "caducado" }
        val eventos = EventosSesion()
        var peticiones = 0
        val motor = MockEngine { request ->
            peticiones++
            assertEquals("Bearer caducado", request.headers[HttpHeaders.Authorization])
            respond("{}", HttpStatusCode.Unauthorized, cabeceras)
        }
        val cliente = crearClientePharmaSoft(almacen, eventos, false,
            "http://localhost:8080/api/v1/", motor)
        try {
            val error = assertFailsWith<SesionException> { AuthRepositorioRest(cliente, almacen).sesionGuardada() }
            assertEquals(ErrorSesion.SesionExpirada, error.motivo)
            assertNull(almacen.token)
            assertEquals(1, eventos.expiraciones.value)
            assertEquals(1, peticiones)
        } finally { cliente.close() }
    }

    @Test
    fun un_403_conserva_sesion_y_devuelve_falta_de_permiso() = runTest {
        val almacen = Memoria().apply { token = "jwt-valido" }
        val eventos = EventosSesion()
        val motor = MockEngine { respond("{}", HttpStatusCode.Forbidden, cabeceras) }
        val cliente = crearClientePharmaSoft(almacen, eventos, false,
            "http://localhost:8080/api/v1/", motor)
        try {
            val error = assertFailsWith<SesionException> { PharmaSoftRepositorioRest(cliente).listarClientes() }
            assertEquals(ErrorSesion.SinPermiso, error.motivo)
            assertEquals("jwt-valido", almacen.token)
            assertEquals(0, eventos.expiraciones.value)
        } finally { cliente.close() }
    }

    @Test
    fun login_invalido_no_guarda_token() = runTest {
        val almacen = Memoria()
        val motor = MockEngine { respond("{}", HttpStatusCode.Unauthorized, cabeceras) }
        val cliente = crearClientePharmaSoft(almacen, EventosSesion(), false,
            "http://localhost:8080/api/v1/", motor)
        try {
            val error = assertFailsWith<SesionException> {
                AuthRepositorioRest(cliente, almacen).iniciarSesion(Credenciales("admin", "mal"))
            }
            assertEquals(ErrorSesion.CredencialesInvalidas, error.motivo)
            assertNull(almacen.token)
        } finally { cliente.close() }
    }

    @Test
    fun login_invalido_con_token_viejo_no_lo_envia_ni_reintenta() = runTest {
        val almacen = Memoria().apply { token = "token-viejo" }
        val autorizaciones = mutableListOf<String?>()
        val motor = MockEngine { request ->
            autorizaciones += request.headers[HttpHeaders.Authorization]
            respond("{}", HttpStatusCode.Unauthorized, cabeceras)
        }
        val cliente = crearClientePharmaSoft(almacen, EventosSesion(), false,
            "http://localhost:8080/api/v1/", motor)
        try {
            assertFailsWith<SesionException> {
                AuthRepositorioRest(cliente, almacen).iniciarSesion(Credenciales("admin", "mal"))
            }
            assertEquals(listOf<String?>(null), autorizaciones)
        } finally { cliente.close() }
    }

    @Test
    fun logging_sanea_authorization_y_no_registra_cuerpo_de_login() = runTest {
        val almacen = Memoria().apply { token = "jwt-muy-secreto" }
        val lineas = mutableListOf<String>()
        val logger = object : Logger { override fun log(message: String) { lineas += message } }
        val motor = MockEngine { respond(jsonMe, HttpStatusCode.OK, cabeceras) }
        val cliente = crearClientePharmaSoft(almacen, EventosSesion(), true,
            "http://localhost:8080/api/v1/", motor, logger)
        try {
            AuthRepositorioRest(cliente, almacen).sesionGuardada()
            val log = lineas.joinToString("\n")
            assertTrue(log.contains("Authorization", ignoreCase = true))
            assertTrue(log.contains("***"))
            assertFalse(log.contains("jwt-muy-secreto"))
        } finally { cliente.close() }
    }

    @Test
    fun productos_y_clientes_usan_el_cliente_autenticado() = runTest {
        val almacen = Memoria().apply { token = "jwt-valido" }
        val rutas = mutableListOf<String>()
        val motor = MockEngine { request ->
            rutas += request.url.encodedPath
            assertEquals("Bearer jwt-valido", request.headers[HttpHeaders.Authorization])
            when (request.url.encodedPath) {
                "/api/v1/productos" -> respond(
                    """{"contenido":[{"id":1,"nombre":"Paracetamol","precio":8.5,"stock":4,"categoriaId":1}],"pagina":0,"tamanio":20,"totalElementos":1,"totalPaginas":1,"ultima":true}""",
                    HttpStatusCode.OK, cabeceras)
                "/api/v1/clientes" -> respond(
                    """{"contenido":[{"id":2,"dni":"12345678","nombres":"Ana","apellidos":"Paz","email":"ana@correo.pe"}],"pagina":0,"tamanio":20,"totalElementos":1,"totalPaginas":1,"ultima":true}""",
                    HttpStatusCode.OK, cabeceras)
                else -> respond("{}", HttpStatusCode.NotFound, cabeceras)
            }
        }
        val cliente = crearClientePharmaSoft(almacen, EventosSesion(), false,
            "http://localhost:8080/api/v1/", motor)
        try {
            val repo = PharmaSoftRepositorioRest(cliente)
            assertEquals("Paracetamol", repo.listarProductos().single().nombre)
            assertEquals("Ana", repo.listarClientes().single().nombres)
            assertEquals(listOf("/api/v1/productos", "/api/v1/clientes"), rutas)
        } finally { cliente.close() }
    }

    @Test
    fun crear_editar_y_eliminar_en_servidor_usa_bearer_y_rutas_correctas() = runTest {
        val almacen = Memoria().apply { token = "jwt-valido" }
        val operaciones = mutableListOf<String>()
        val motor = MockEngine { request ->
            assertEquals("Bearer jwt-valido", request.headers[HttpHeaders.Authorization])
            operaciones += "${request.method.value} ${request.url.encodedPath}"
            when (request.method) {
                HttpMethod.Delete -> respond("", HttpStatusCode.NoContent)
                else -> if (request.url.encodedPath.startsWith("/api/v1/productos")) {
                    respond("""{"id":1,"nombre":"Paracetamol","precio":8.5,"stock":4,"categoriaId":1}""",
                        HttpStatusCode.OK, cabeceras)
                } else {
                    respond("""{"id":2,"dni":"12345678","nombres":"Ana","apellidos":"Paz","email":"ana@correo.pe"}""",
                        HttpStatusCode.OK, cabeceras)
                }
            }
        }
        val cliente = crearClientePharmaSoft(almacen, EventosSesion(), false,
            "http://localhost:8080/api/v1/", motor)
        try {
            val repo = PharmaSoftRepositorioRest(cliente)
            val producto = ProductoEntrada("Paracetamol", 8.5, 4, 1)
            val persona = ClienteEntrada("12345678", "Ana", "Paz", "ana@correo.pe", null, null)
            repo.guardarProducto(producto)
            repo.guardarProducto(producto, 1)
            repo.eliminarProducto(1)
            repo.guardarCliente(persona)
            repo.guardarCliente(persona, 2)
            repo.eliminarCliente(2)
            assertEquals(
                listOf("POST /api/v1/productos", "PUT /api/v1/productos/1", "DELETE /api/v1/productos/1",
                    "POST /api/v1/clientes", "PUT /api/v1/clientes/2", "DELETE /api/v1/clientes/2"),
                operaciones
            )
        } finally { cliente.close() }
    }
}
