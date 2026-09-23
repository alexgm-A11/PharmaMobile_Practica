package pe.edu.upeu.pharmamobile.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** Diagnósticos reproducibles de la sesión 7; no modifican datos del servidor. */
class DiagnosticoConexionApi(private val client: HttpClient) {
    suspend fun probarTimeout(): String {
        val rapido = crearClienteProductos(timeoutMillis = 1, log = false)
        return try {
            ProductoApi(rapido).listar()
            "La petición terminó antes de 1 ms; repita la prueba."
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpRequestTimeoutException) {
            "HttpRequestTimeoutException controlada: la interfaz sigue respondiendo."
        } catch (e: Exception) {
            "Otro error de red durante la prueba: ${e::class.simpleName}"
        } finally {
            rapido.close()
        }
    }

    suspend fun probarCampoDesconocido(): String {
        val json = client.get("$PHARMA_API_BASE_URL/products?limit=1&offset=0").bodyAsText()
        val flexible = Json { ignoreUnknownKeys = true }
        val estricto = Json { ignoreUnknownKeys = false }
        val cantidad = flexible.decodeFromString<List<ProductoDto>>(json).size
        val falloEstricto = try {
            estricto.decodeFromString<List<ProductoDto>>(json)
            false
        } catch (_: SerializationException) {
            true
        }
        return "JSON: flexible leyó $cantidad producto(s); estricto ${if (falloEstricto) "produjo SerializationException" else "no encontró campos extra"}."
    }
}
