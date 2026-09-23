package pe.edu.upeu.pharmamobile.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class ScreenTest {

    @Test
    fun contieneLosCincoDestinosEnOrden() {
        assertEquals(
            listOf("Inicio", "Productos", "Catálogo REST", "Clientes", "Pedidos"),
            Screen.destinos.map(::tituloPantalla)
        )
    }
}
