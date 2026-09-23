package pe.edu.upeu.pharmamobile.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

const val PHARMA_API_BASE_URL = "https://api.escuelajs.co/api/v1"

fun crearClienteProductos(
    timeoutMillis: Long = 10_000,
    log: Boolean = true
): HttpClient = HttpClient {
    expectSuccess = true
    install(HttpTimeout) { requestTimeoutMillis = timeoutMillis }
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    if (log) install(Logging) {
        logger = object : Logger {
            override fun log(message: String) { println(message) }
        }
        level = LogLevel.ALL
    }
}

class ProductoApi(
    private val client: HttpClient,
    private val baseUrl: String = PHARMA_API_BASE_URL
) {
    suspend fun listar(limit: Int = 10, offset: Int = 0): List<ProductoDto> {
        require(limit in 1..100 && offset >= 0)
        return client.get(url("products")) {
            parameter("limit", limit)
            parameter("offset", offset)
        }.body()
    }

    suspend fun obtener(id: Int): ProductoDto {
        require(id > 0)
        return client.get(url("products", id.toString())).body()
    }

    private fun url(vararg segments: String) = URLBuilder(baseUrl).apply {
        appendPathSegments(*segments)
    }.buildString()
}
