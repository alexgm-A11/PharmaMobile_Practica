package pe.edu.upeu.pharmamobile.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class ScreenTest {

    @Test
    fun contieneLosCuatroDestinosEnOrden() {
        assertEquals(
            listOf("Inicio", "Productos", "Clientes", "Pedidos"),
            Screen.destinos.map(::tituloPantalla)
        )
    }
}
