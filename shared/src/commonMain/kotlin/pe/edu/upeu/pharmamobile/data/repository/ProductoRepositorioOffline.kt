package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobile.data.local.ProductoLocal
import pe.edu.upeu.pharmamobile.data.local.epochMillisNow
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.domain.repository.SincronizacionProductosRepository
import pe.edu.upeu.pharmamobile.domain.model.ResultadoEnvio

/** La base local es la única fuente de lectura de la interfaz. */
class ProductoRepositorioOffline(
    private val local: ProductoLocal,
    private val remoto: ProductosRemotos
) : ProductoRepository, SincronizacionProductosRepository {
    override fun observar(): Flow<List<Producto>> = local.observar()

    override suspend fun listar(): List<Producto> = local.listar()

    override suspend fun registrar(producto: Producto): Producto {
        // El caso de uso de sincronización decide cuándo y cómo enviar este alta local.
        return local.guardarPendiente(producto)
    }

    override suspend fun pendientes(maxIntentos: Int): List<Producto> = local.pendientes(maxIntentos)

    override suspend fun enviar(producto: Producto): ResultadoEnvio = remoto.enviar(producto)

    override suspend fun confirmar(idLocal: Long, idServidor: Long) {
        val guardado = local.porId(idLocal) ?: return
        local.confirmarPendiente(idLocal, guardado.copy(id = idServidor, pendiente = false))
    }

    override suspend fun sumarIntento(idLocal: Long, error: String): Int = local.sumarIntento(idLocal, error)

    override suspend fun marcarFallida(idLocal: Long, error: String, maxIntentos: Int) {
        local.marcarFallida(idLocal, error, maxIntentos)
    }

    override suspend fun conciliarRemoto() {
        local.reemplazarSincronizados(remoto.listar())
        local.guardarUltimaSincronizacion(epochMillisNow())
    }

    override suspend fun ultimaSincronizacion(): Long? = local.ultimaSincronizacion()

    override fun observarPendientes(maxIntentos: Int) = local.observarPendientes(maxIntentos)

    override fun observarFallidas(maxIntentos: Int) = local.observarFallidas(maxIntentos)

    override suspend fun reintentarFallidas(maxIntentos: Int) {
        local.reiniciarFallidas(maxIntentos)
    }
}
