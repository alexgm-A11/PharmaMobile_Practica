package pe.edu.upeu.pharmamobile.domain.usecase

import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobile.domain.model.Cliente
import pe.edu.upeu.pharmamobile.domain.repository.ClienteRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RegistrarClienteUseCaseTest {
    private val repository = ClienteRepositoryFalso()
    private val useCase = RegistrarClienteUseCase(repository)

    @Test
    fun registraClienteConDatosValidos() = runTest {
        val cliente = useCase("Farmacia Nueva Vida", "VENTAS@CENTRAL.PE", "923733066").getOrThrow()
        assertEquals(1L, cliente.id)
        assertEquals("ventas@central.pe", cliente.correo)
        assertEquals("923733066", cliente.telefono)
    }

    @Test
    fun permiteTelefonoVacio() = runTest {
        assertNull(useCase("Juan Pérez", "juan@correo.com", "").getOrThrow().telefono)
    }

    @Test
    fun rechazaDatosInvalidos() = runTest {
        assertError("Nombre obligatorio", "", "cliente@correo.com", "")
        assertError("El nombre debe tener al menos 3 caracteres", "Li", "cliente@correo.com", "")
        assertError("Ingrese un nombre válido", "Cliente 123", "cliente@correo.com", "")
        assertError("Correo obligatorio", "Juan Pérez", "", "")
        assertError("Ingrese un correo válido", "Juan Pérez", "correo-invalido", "")
        assertError("El teléfono debe tener 9 dígitos", "Juan Pérez", "juan@correo.com", "92373")
    }

    private suspend fun assertError(mensaje: String, nombre: String, correo: String, telefono: String) {
        val resultado = useCase(nombre, correo, telefono)
        assertTrue(resultado.isFailure)
        assertEquals(mensaje, resultado.exceptionOrNull()?.message)
    }
}

private class ClienteRepositoryFalso : ClienteRepository {
    private val clientes = mutableListOf<Cliente>()
    override suspend fun registrar(cliente: Cliente): Cliente =
        cliente.copy(id = (clientes.size + 1).toLong()).also(clientes::add)
    override suspend fun listar(): List<Cliente> = clientes.toList()
}
