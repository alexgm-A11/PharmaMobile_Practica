package pe.edu.upeu.pharmamobile

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.presentation.catalogo.mensajeConexion

class ProductoApiTest {
    private val json = """{"id":28,"title":"Mesa","price":60,"description":"Madera","images":[],"category":{"id":3,"name":"Hogar"},"campoNuevo":true}"""

    private fun cliente(timeout: Long = 10_000, handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): HttpClient =
        HttpClient(MockEngine(handler)) {
            expectSuccess = true
            install(HttpTimeout) { requestTimeoutMillis = timeout }
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

    @Test fun respuesta200ListaRenderizable() = runBlocking {
        val client = cliente { respond("[$json]", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json")) }
        try {
            val lista = ProductoApi(client).listar(1, 0)
            assertEquals("Mesa", lista.single().toDomain().nombre)
            assertEquals("Hogar", lista.single().toDomain().categoria)
        } finally { client.close() }
    }

    @Test fun recursoInexistenteControlado() = runBlocking {
        val client = cliente { respond("{}", HttpStatusCode.NotFound) }
        try {
            val error = assertFailsWith<ClientRequestException> { ProductoApi(client).obtener(999999) }
            assertEquals("Producto no encontrado (404)", mensajeConexion(error))
        } finally { client.close() }
    }

    @Test fun sinConexionControlada() = runBlocking {
        val client = cliente { throw IllegalStateException("Simulación sin red") }
        try {
            val error = assertFailsWith<IllegalStateException> { ProductoApi(client).listar() }
            assertTrue(mensajeConexion(error).startsWith("Sin conexión"))
        } finally { client.close() }
    }

    @Test fun tiempoDeEsperaControlado() = runBlocking {
        val client = cliente(timeout = 1) { delay(100); respond("[]", HttpStatusCode.OK) }
        try {
            val error = assertFailsWith<HttpRequestTimeoutException> { ProductoApi(client).listar() }
            assertTrue(mensajeConexion(error).startsWith("Tiempo de espera"))
        } finally { client.close() }
    }

    @Test fun campoDesconocidoIgnoradoYModoEstrictoFalla() = runBlocking {
        val client = cliente { respond("[$json]", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json")) }
        try {
            assertEquals(1, ProductoApi(client).listar().size)
            assertFailsWith<SerializationException> {
                Json { ignoreUnknownKeys = false }.decodeFromString<pe.edu.upeu.pharmamobile.data.remote.ProductoDto>(json)
            }
        } finally { client.close() }
        Unit
    }
}
