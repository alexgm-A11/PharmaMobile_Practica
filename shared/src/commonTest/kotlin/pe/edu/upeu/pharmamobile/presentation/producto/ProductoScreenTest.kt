package pe.edu.upeu.pharmamobile.presentation.producto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ProductoScreenTest {

    @Test
    fun registraProductoConDatosCorrectos() {
        val resultado = registrarProducto("Paracetamol", "8.50", "100")

        val exito = assertIs<RegistroProductoResultado.Exito>(resultado)
        assertEquals("Paracetamol", exito.producto.nombre)
        assertEquals(8.50, exito.producto.precio)
        assertEquals(100, exito.producto.stock)
    }

    @Test
    fun rechazaNombreVacio() {
        assertError("Nombre obligatorio", "", "8.50", "100")
    }

    @Test
    fun rechazaPrecioNoNumerico() {
        assertError("Precio inválido", "Ibuprofeno", "abc", "50")
    }

    @Test
    fun rechazaPrecioIgualACero() {
        assertError("El precio debe ser mayor a 0", "Ibuprofeno", "0", "50")
    }

    @Test
    fun rechazaPrecioNegativo() {
        assertError("El precio debe ser mayor a 0", "Ibuprofeno", "-12", "50")
    }

    @Test
    fun rechazaStockNoNumerico() {
        assertError("Stock debe ser un número entero", "Amoxicilina", "18.50", "abc")
    }

    @Test
    fun rechazaStockNegativo() {
        assertError("Stock no puede ser negativo", "Amoxicilina", "18.50", "-5")
    }

    @Test
    fun permiteStockEnCero() {
        val resultado = registrarProducto("Loratadina", "10.00", "0")
        val exito = assertIs<RegistroProductoResultado.Exito>(resultado)
        assertEquals(0, exito.producto.stock)
    }

    private fun assertError(
        esperado: String,
        nombre: String,
        precio: String,
        stock: String
    ) {
        val resultado = registrarProducto(nombre, precio, stock)
        assertEquals(
            esperado,
            assertIs<RegistroProductoResultado.Error>(resultado).mensaje
        )
    }
}
