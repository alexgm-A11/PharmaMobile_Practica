package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

class ProductoRepositorioEnMemoria : ProductoRepository {
    private val mutex = Mutex()
    private val productos = mutableListOf(
        Producto(id = 1, nombre = "Paracetamol", precio = 8.50, stock = 100),
        Producto(id = 2, nombre = "Ibuprofeno", precio = 12.00, stock = 50),
        Producto(id = 3, nombre = "Amoxicilina", precio = 18.50, stock = 7)
    )
    private var siguienteId = 4L

    override suspend fun registrar(producto: Producto): Producto {
        delay(500)
        return mutex.withLock {
            producto.copy(id = siguienteId++).also(productos::add)
        }
    }

    override suspend fun listar(): List<Producto> {
        delay(500)
        return mutex.withLock { productos.toList() }
    }
}
