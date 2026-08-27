package pe.edu.upeu.pharmamobile.presentation.producto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ProductoScreenTest {

    @Test
    fun registraProductoConDatosCorrectos() {
        val resultado = registrarProducto("Paracetamol 500 mg", "8.50", "100")

        val exito = assertIs<RegistroProductoResultado.Exito>(resultado)
        assertEquals("Paracetamol 500 mg", exito.producto.nombre)
        assertEquals(8.50, exito.producto.precio)
        assertEquals(100, exito.producto.stock)
    }

    @Test
    fun rechazaNombreVacio() {
        val resultado = registrarProducto("", "8.50", "100")

        assertEquals(
            "Ingrese nombre del producto",
            assertIs<RegistroProductoResultado.Error>(resultado).mensaje
        )
    }

    @Test
    fun rechazaPrecioNoNumerico() {
        val resultado = registrarProducto("Paracetamol", "abc", "100")

        assertEquals(
            "Ingrese precio válido",
            assertIs<RegistroProductoResultado.Error>(resultado).mensaje
        )
    }

    @Test
    fun rechazaStockNegativo() {
        val resultado = registrarProducto("Paracetamol", "8.50", "-10")

        assertEquals(
            "El stock no puede ser negativo",
            assertIs<RegistroProductoResultado.Error>(resultado).mensaje
        )
    }
}
