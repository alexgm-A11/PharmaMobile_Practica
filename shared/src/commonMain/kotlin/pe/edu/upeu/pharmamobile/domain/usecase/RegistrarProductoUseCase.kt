package pe.edu.upeu.pharmamobile.domain.usecase

import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

enum class CampoProducto { NOMBRE, PRECIO, STOCK }

class ValidacionProductoException(
    val campo: CampoProducto,
    message: String
) : IllegalArgumentException(message)

class RegistrarProductoUseCase(
    private val repository: ProductoRepository
) {
    suspend operator fun invoke(
        nombre: String,
        precioTexto: String,
        stockTexto: String
    ): Result<Producto> = runCatching {
        val nombreLimpio = nombre.trim()
        if (nombreLimpio.isEmpty()) throw ValidacionProductoException(CampoProducto.NOMBRE, "Nombre obligatorio")

        val precio = precioTexto.trim().replace(',', '.').toDoubleOrNull()
            ?: throw ValidacionProductoException(CampoProducto.PRECIO, "Precio inválido")
        if (precio <= 0.0) throw ValidacionProductoException(CampoProducto.PRECIO, "El precio debe ser mayor a 0")

        val stock = stockTexto.trim().toIntOrNull()
            ?: throw ValidacionProductoException(CampoProducto.STOCK, "Stock debe ser un número entero")
        if (stock < 0) throw ValidacionProductoException(CampoProducto.STOCK, "Stock no puede ser negativo")

        repository.registrar(
            Producto(id = 0L, nombre = nombreLimpio, precio = precio, stock = stock)
        )
    }
}
