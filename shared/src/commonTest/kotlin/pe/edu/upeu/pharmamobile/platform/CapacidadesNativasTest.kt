package pe.edu.upeu.pharmamobile.platform

import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.usecase.comoTextoParaCompartir
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoUi
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class CapacidadesNativasTest {
    private val producto = Producto(7, "Loratadina", 12.5, 8)

    @Test
    fun precioDeLaListaUsaElFormateadorNativo() {
        assertEquals(formatearSoles(producto.precio), ProductoUi.from(producto).precio)
        assertContains(ProductoUi.from(producto).precio, "S/")
    }

    @Test
    fun textoCompartidoSeConstruyeEnCodigoComun() {
        val texto = producto.comoTextoParaCompartir()
        assertContains(texto, producto.nombre)
        assertContains(texto, formatearSoles(producto.precio))
        assertContains(texto, "Stock: 8")
    }
}
