package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobile.data.local.ProductoLocal
import pe.edu.upeu.pharmamobile.data.local.epochMillisNow
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

/** La base local es la única fuente de lectura de la interfaz. */
class ProductoRepositorioOffline(
    private val local: ProductoLocal,
    private val remoto: ProductosRemotos
) : ProductoRepository {
    override fun observar(): Flow<List<Producto>> = local.observar()

    override suspend fun listar(): List<Producto> = local.listar()

    override suspend fun registrar(producto: Producto): Producto {
        val guardado = local.guardarPendiente(producto)
        try {
            sincronizar()
        } catch (cancelado: CancellationException) {
            throw cancelado
        } catch (_: Exception) {
            // El guardado local ya ocurrió. Se conserva como pendiente para el próximo reintento.
        }
        val productosActuales = local.listar()
        return productosActuales.firstOrNull { it.id == guardado.id }
            ?: productosActuales.lastOrNull {
                it.nombre == guardado.nombre && it.precio == guardado.precio && it.stock == guardado.stock
            }
            ?: guardado
    }

    override suspend fun sincronizar() {
        // Orden intencional: primero subir lo pendiente para no borrarlo con la descarga.
        local.pendientes().forEach { pendiente ->
            val respuesta = remoto.registrar(pendiente)
            local.confirmarPendiente(pendiente.id, respuesta)
        }
        local.reemplazarSincronizados(remoto.listar())
        local.guardarUltimaSincronizacion(epochMillisNow())
    }

    override suspend fun ultimaSincronizacion(): Long? = local.ultimaSincronizacion()
}
