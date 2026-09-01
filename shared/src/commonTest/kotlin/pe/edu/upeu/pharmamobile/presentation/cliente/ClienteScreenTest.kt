package pe.edu.upeu.pharmamobile.presentation.cliente

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class ClienteScreenTest {

    @Test
    fun registraClienteConTelefono() {
        val resultado = registrarCliente(
            nombre = "Farmacia Nueva Vida",
            correo = "VENTAS@CENTRAL.PE",
            telefono = "923733066"
        )

        val exito = assertIs<RegistroClienteResultado.Exito>(resultado)
        assertEquals("Farmacia Nueva Vida", exito.cliente.nombre)
        assertEquals("ventas@central.pe", exito.cliente.correo)
        assertEquals("923733066", exito.cliente.telefono)
    }

    @Test
    fun registraClienteSinTelefono() {
        val resultado = registrarCliente("Juan Pérez", "juan@correo.com", "")

        assertNull(assertIs<RegistroClienteResultado.Exito>(resultado).cliente.telefono)
    }

    @Test
    fun rechazaNombreVacio() {
        assertError("Nombre obligatorio", "", "cliente@correo.com", "")
    }

    @Test
    fun rechazaNombreMuyCorto() {
        assertError(
            "El nombre debe tener al menos 3 caracteres",
            "Li",
            "cliente@correo.com",
            ""
        )
    }

    @Test
    fun rechazaNombreConNumeros() {
        assertError("Ingrese un nombre válido", "Cliente 123", "cliente@correo.com", "")
    }

    @Test
    fun rechazaCorreoVacio() {
        assertError("Correo obligatorio", "Juan Pérez", "", "")
    }

    @Test
    fun rechazaCorreoInvalido() {
        assertError("Ingrese un correo válido", "Juan Pérez", "correo-invalido", "")
    }

    @Test
    fun rechazaTelefonoIncompleto() {
        assertError(
            "El teléfono debe tener 9 dígitos",
            "Juan Pérez",
            "juan@correo.com",
            "92373"
        )
    }

    private fun assertError(
        esperado: String,
        nombre: String,
        correo: String,
        telefono: String
    ) {
        assertEquals(
            esperado,
            assertIs<RegistroClienteResultado.Error>(
                registrarCliente(nombre, correo, telefono)
            ).mensaje
        )
    }
}
