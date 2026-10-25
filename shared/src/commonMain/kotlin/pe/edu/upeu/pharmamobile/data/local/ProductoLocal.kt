package pe.edu.upeu.pharmamobile.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upeu.pharmamobile.db.PharmaDb
import pe.edu.upeu.pharmamobile.domain.model.Producto

/** Traduce filas SQLDelight a dominio; esta capa no conoce Ktor ni DTO remotos. */
class ProductoLocal(base: PharmaDb) {
    private val consultas = base.productoQueries

    fun observar(): Flow<List<Producto>> = consultas.listarTodos()
        .asFlow()
        .mapToList(Dispatchers.Default)
        .map { filas -> filas.map { Producto(it.id, it.nombre, it.precio, it.stock.toInt(), it.pendiente == 1L) } }

    fun listar(): List<Producto> = consultas.listarTodos().executeAsList()
        .map { Producto(it.id, it.nombre, it.precio, it.stock.toInt(), it.pendiente == 1L) }

    fun pendientes(): List<Producto> = consultas.listarPendientes().executeAsList()
        .map { Producto(it.id, it.nombre, it.precio, it.stock.toInt(), pendiente = true) }

    fun guardarPendiente(producto: Producto): Producto {
        val menor = consultas.siguienteIdLocal().executeAsOne()
        val local = producto.copy(id = minOf(menor, 0L) - 1L, pendiente = true)
        insertar(local)
        return local
    }

    fun confirmarPendiente(idLocal: Long, productoServidor: Producto) {
        consultas.transaction {
            consultas.eliminarPorId(idLocal)
            insertar(productoServidor.copy(pendiente = false))
        }
    }

    /** Solo reemplaza filas ya sincronizadas; el trabajo offline se conserva. */
    fun reemplazarSincronizados(productos: List<Producto>) {
        consultas.transaction {
            consultas.borrarSincronizados()
            productos.forEach { insertar(it.copy(pendiente = false)) }
        }
    }

    fun guardarUltimaSincronizacion(instante: Long) =
        consultas.guardarUltimaSincronizacion(instante)

    fun ultimaSincronizacion(): Long? =
        consultas.leerUltimaSincronizacion().executeAsOneOrNull()

    private fun insertar(producto: Producto) = consultas.insertar(
        id = producto.id,
        nombre = producto.nombre,
        precio = producto.precio,
        stock = producto.stock.toLong(),
        pendiente = if (producto.pendiente) 1L else 0L
    )
}
