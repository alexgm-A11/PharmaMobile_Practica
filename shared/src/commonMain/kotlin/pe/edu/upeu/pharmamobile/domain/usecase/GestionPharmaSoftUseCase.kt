package pe.edu.upeu.pharmamobile.domain.usecase

import pe.edu.upeu.pharmamobile.domain.model.ClienteEntrada
import pe.edu.upeu.pharmamobile.domain.model.ProductoEntrada
import pe.edu.upeu.pharmamobile.domain.repository.PharmaSoftRepository

class GestionPharmaSoftUseCase(private val repo: PharmaSoftRepository) {
    suspend fun categorias() = repo.listarCategorias()
    suspend fun productos() = repo.listarProductos()
    suspend fun clientes() = repo.listarClientes()

    suspend fun guardarProducto(nombre: String, precio: String, stock: String, categoriaId: String, id: Long? = null) =
        repo.guardarProducto(
            ProductoEntrada(
                nombre.trim().also { require(it.length in 3..150) { "Nombre: 3 a 150 caracteres." } },
                (precio.replace(',', '.').toDoubleOrNull() ?: 0.0).also {
                    require(it.isFinite() && it > 0.0) { "Precio: ingresa un valor mayor a cero." }
                },
                (stock.toIntOrNull() ?: -1).also { require(it >= 0) { "Stock: ingresa un entero no negativo." } },
                (categoriaId.toLongOrNull() ?: 0).also { require(it > 0) { "Selecciona una categoría." } }
            ), id
        )

    suspend fun eliminarProducto(id: Long) = repo.eliminarProducto(id)

    suspend fun guardarCliente(
        dni: String, nombres: String, apellidos: String, email: String,
        telefono: String, direccion: String, id: Long? = null
    ) = repo.guardarCliente(
        ClienteEntrada(
            dni.trim().also { require(it.length == 8 && it.all(Char::isDigit)) { "DNI: 8 dígitos." } },
            nombres.trim().also { require(it.length in 2..100) { "Nombres: 2 a 100 caracteres." } },
            apellidos.trim().also { require(it.length in 2..100) { "Apellidos: 2 a 100 caracteres." } },
            email.trim().also { require('@' in it && it.length <= 150) { "Correo inválido." } },
            telefono.trim().takeIf(String::isNotEmpty)?.also {
                require(it.length == 9 && it.all(Char::isDigit)) { "Teléfono: 9 dígitos." }
            },
            direccion.trim().takeIf(String::isNotEmpty)?.also {
                require(it.length <= 250) { "Dirección: máximo 250 caracteres." }
            }
        ), id
    )

    suspend fun eliminarCliente(id: Long) = repo.eliminarCliente(id)
}
