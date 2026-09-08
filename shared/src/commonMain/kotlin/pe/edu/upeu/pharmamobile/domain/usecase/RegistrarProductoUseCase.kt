package pe.edu.upeu.pharmamobile.domain.usecase

import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

class RegistrarProductoUseCase(
    private val repository: ProductoRepository
) {
    suspend operator fun invoke(
        nombre: String,
        precioTexto: String,
        stockTexto: String
    ): Result<Producto> = runCatching {
        val nombreLimpio = nombre.trim()
        require(nombreLimpio.isNotEmpty()) { "Nombre obligatorio" }

        val precio = precioTexto.trim().replace(',', '.').toDoubleOrNull()
            ?: error("Precio inválido")
        require(precio > 0.0) { "El precio debe ser mayor a 0" }

        val stock = stockTexto.trim().toIntOrNull()
            ?: error("Stock debe ser un número entero")
        require(stock >= 0) { "Stock no puede ser negativo" }

        repository.registrar(
            Producto(id = 0L, nombre = nombreLimpio, precio = precio, stock = stock)
        )
    }
}
