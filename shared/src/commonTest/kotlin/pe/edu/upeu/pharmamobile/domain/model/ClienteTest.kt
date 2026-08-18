package pe.edu.upeu.pharmamobile.domain.model

import kotlin.io.println
import kotlin.test.Test
import kotlin.test.assertEquals

class ClienteTest{

    @Test
    fun probarCliente(){
    val cliente = Cliente(
        id = 1L,
        nombre = "Farmacia Nueva Vida",
        correo = "ventas@central.pe",
        telefono = "923733066"
    )
    val resultado = cliente.obtenerTelefono()

        assertEquals(
            expected = "923733066",
            resultado
        )
}
}
