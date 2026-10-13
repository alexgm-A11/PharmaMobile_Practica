package pe.edu.upeu.pharmamobile.domain.repository

import pe.edu.upeu.pharmamobile.domain.model.CategoriaServidor
import pe.edu.upeu.pharmamobile.domain.model.ClienteEntrada
import pe.edu.upeu.pharmamobile.domain.model.ClienteServidor
import pe.edu.upeu.pharmamobile.domain.model.ProductoEntrada
import pe.edu.upeu.pharmamobile.domain.model.ProductoServidor

interface PharmaSoftRepository {
    suspend fun listarCategorias(): List<CategoriaServidor>
    suspend fun listarProductos(): List<ProductoServidor>
    suspend fun guardarProducto(entrada: ProductoEntrada, id: Long? = null): ProductoServidor
    suspend fun eliminarProducto(id: Long)
    suspend fun listarClientes(): List<ClienteServidor>
    suspend fun guardarCliente(entrada: ClienteEntrada, id: Long? = null): ClienteServidor
    suspend fun eliminarCliente(id: Long)
}
