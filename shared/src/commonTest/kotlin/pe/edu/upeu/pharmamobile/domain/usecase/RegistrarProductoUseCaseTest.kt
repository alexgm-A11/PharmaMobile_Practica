package pe.edu.upeu.pharmamobile.domain.usecase

import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegistrarProductoUseCaseTest {
    private val repository = ProductoRepositoryFalso()
    private val useCase = RegistrarProductoUseCase(repository)

    @Test
    fun registraProductoValidoYRepositorioAsignaId() = runTest {
        val resultado = useCase("Paracetamol", "8.50", "100")
        assertTrue(resultado.isSuccess)
        assertEquals(1L, resultado.getOrThrow().id)
    }

    @Test
    fun rechazaCamposInvalidos() = runTest {
        assertEquals("Nombre obligatorio", useCase("", "8.50", "100").exceptionOrNull()?.message)
        assertEquals("Precio inválido", useCase("Ibuprofeno", "abc", "50").exceptionOrNull()?.message)
        assertEquals("El precio debe ser mayor a 0", useCase("Ibuprofeno", "0", "50").exceptionOrNull()?.message)
        assertEquals("Stock debe ser un número entero", useCase("Amoxicilina", "18.50", "abc").exceptionOrNull()?.message)
        assertEquals("Stock no puede ser negativo", useCase("Amoxicilina", "18.50", "-1").exceptionOrNull()?.message)
    }
}

private class ProductoRepositoryFalso : ProductoRepository {
    private val productos = mutableListOf<Producto>()
    override suspend fun registrar(producto: Producto): Producto =
        producto.copy(id = (productos.size + 1).toLong()).also(productos::add)
    override suspend fun listar(): List<Producto> = productos.toList()
}
