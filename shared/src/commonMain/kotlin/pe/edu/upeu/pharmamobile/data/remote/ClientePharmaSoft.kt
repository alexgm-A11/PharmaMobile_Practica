package pe.edu.upeu.pharmamobile.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobile.data.local.EventosSesion
import pe.edu.upeu.pharmamobile.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobile.domain.model.SesionException

/** Cliente aislado de la API pública de Platzi para no enviarle el JWT. */
fun crearClientePharmaSoft(
    almacen: AlmacenSeguro,
    eventos: EventosSesion,
    depuracion: Boolean,
    baseUrl: String = pharmaSoftBaseUrl,
    motor: HttpClientEngine? = null,
    logger: Logger = object : Logger {
        override fun log(message: String) { println(message) }
    }
): HttpClient {
    val configurar: HttpClientConfig<*>.() -> Unit = {
        expectSuccess = true
        install(HttpTimeout) { requestTimeoutMillis = 10_000 }
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        install(DefaultRequest) {
            url(baseUrl)
            contentType(ContentType.Application.Json)
        }
        install(Auth) {
            bearer {
                loadTokens { almacen.leerToken()?.let { BearerTokens(it, "") } }
                sendWithoutRequest { request ->
                    request.url.host == io.ktor.http.Url(baseUrl).host &&
                        request.url.build().encodedPath.startsWith("/api/v1/") &&
                        !request.url.build().encodedPath.endsWith("/auth/login")
                }
            }
        }
        install(Logging) {
            this.logger = logger
            level = if (depuracion) LogLevel.HEADERS else LogLevel.NONE
            sanitizeHeader { it.equals(HttpHeaders.Authorization, ignoreCase = true) }
        }
        HttpResponseValidator {
            handleResponseExceptionWithRequest { causa, solicitud ->
                val respuesta = (causa as? ClientRequestException)?.response
                    ?: return@handleResponseExceptionWithRequest
                when (respuesta.status) {
                    HttpStatusCode.Unauthorized -> {
                        if (!solicitud.url.encodedPath.endsWith("/auth/login")) {
                            almacen.borrarToken()
                            eventos.expirar()
                            throw SesionException(ErrorSesion.SesionExpirada)
                        }
                    }
                    HttpStatusCode.Forbidden -> throw SesionException(ErrorSesion.SinPermiso)
                    else -> Unit
                }
            }
        }
    }
    return if (motor == null) HttpClient(configurar) else HttpClient(motor, configurar)
}
