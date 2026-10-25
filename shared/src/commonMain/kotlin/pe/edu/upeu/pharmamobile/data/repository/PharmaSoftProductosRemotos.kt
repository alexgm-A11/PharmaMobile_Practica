package pe.edu.upeu.pharmamobile.data.repository

import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.model.ProductoEntrada
import pe.edu.upeu.pharmamobile.domain.repository.PharmaSoftRepository

/** Adaptador hacia PharmaSoft. La categoría 1 es Analgésicos, sembrada por V4 del backend. */
class PharmaSoftProductosRemotos(
    private val api: PharmaSoftRepository,
    private val categoriaPredeterminada: Long = CATEGORIA_PREDETERMINADA
) : ProductosRemotos {
    override suspend fun listar(): List<Producto> = api.listarProductos().map {
        Producto(it.id, it.nombre, it.precio, it.stock)
    }

    override suspend fun registrar(producto: Producto): Producto {
        val guardado = api.guardarProducto(
            ProductoEntrada(
                nombre = producto.nombre,
                precio = producto.precio,
                stock = producto.stock,
                categoriaId = categoriaPredeterminada
            )
        )
        return Producto(guardado.id, guardado.nombre, guardado.precio, guardado.stock)
    }

    private companion object {
        const val CATEGORIA_PREDETERMINADA = 1L
    }
}
