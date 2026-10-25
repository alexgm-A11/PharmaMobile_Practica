package pe.edu.upeu.pharmamobile.data.repository

import pe.edu.upeu.pharmamobile.domain.model.Producto

/** Puerto remoto estrecho para sincronización de inventario y pruebas con Fake. */
interface ProductosRemotos {
    suspend fun listar(): List<Producto>
    suspend fun registrar(producto: Producto): Producto
}
