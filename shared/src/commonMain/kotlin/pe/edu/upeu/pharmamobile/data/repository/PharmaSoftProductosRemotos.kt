package pe.edu.upeu.pharmamobile.data.repository

import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CancellationException
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.model.ProductoEntrada
import pe.edu.upeu.pharmamobile.domain.model.ResultadoEnvio
import pe.edu.upeu.pharmamobile.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobile.domain.model.SesionException
import pe.edu.upeu.pharmamobile.domain.repository.PharmaSoftRepository

/** Adaptador hacia PharmaSoft. La categoría 1 es Analgésicos, sembrada por V4 del backend. */
class PharmaSoftProductosRemotos(
    private val api: PharmaSoftRepository,
    private val categoriaPredeterminada: Long = CATEGORIA_PREDETERMINADA
) : ProductosRemotos {
    override suspend fun listar(): List<Producto> = api.listarProductos().map {
        Producto(it.id, it.nombre, it.precio, it.stock)
    }

    override suspend fun enviar(producto: Producto): ResultadoEnvio = try {
        val guardado = api.guardarProducto(
            ProductoEntrada(
                nombre = producto.nombre,
                precio = producto.precio,
                stock = producto.stock,
                categoriaId = categoriaPredeterminada
            )
        )
        ResultadoEnvio.Exito(guardado.id)
    } catch (cancelado: CancellationException) {
        throw cancelado
    } catch (error: SesionException) {
        ResultadoEnvio.Permanente(
            if (error.motivo == ErrorSesion.SesionExpirada) "Sesión vencida" else error.message.orEmpty()
        )
    } catch (error: ResponseException) {
        val codigo = error.response.status.value
        val mensaje = when (codigo) {
            401 -> "Sesión vencida"
            403 -> "No tienes permiso para crear productos"
            404 -> "No se encontró la categoría del producto"
            409 -> "El producto entra en conflicto con un registro existente"
            400, 422 -> "El servidor rechazó los datos del producto"
            else -> error.message ?: "Error HTTP $codigo al sincronizar"
        }
        if (codigo == 408 || codigo == 429 || codigo >= 500) {
            // Un 5xx puede ocurrir después de guardar la fila; requiere decisión del usuario.
            ResultadoEnvio.Transitorio(mensaje, reintentoAutomaticoSeguro = false)
        } else {
            ResultadoEnvio.Permanente(mensaje)
        }
    } catch (error: HttpRequestTimeoutException) {
        ResultadoEnvio.Transitorio(
            error.message ?: "Tiempo agotado; verifica si el servidor recibió el registro",
            reintentoAutomaticoSeguro = false
        )
    } catch (error: Exception) {
        // Un error de transporte puede ocurrir después de que el servidor procese un POST.
        ResultadoEnvio.Transitorio(
            error.message ?: "No se pudo confirmar el envío; revisa antes de reintentar",
            reintentoAutomaticoSeguro = false
        )
    }

    private companion object {
        const val CATEGORIA_PREDETERMINADA = 1L
    }
}
