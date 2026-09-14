package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.edu.upeu.pharmamobile.domain.model.Cliente
import pe.edu.upeu.pharmamobile.domain.repository.ClienteRepository

class ClienteRepositorioEnMemoria : ClienteRepository {
    private val mutex = Mutex()
    private val clientes = mutableListOf<Cliente>()
    private var siguienteId = 1L

    override suspend fun registrar(cliente: Cliente): Cliente {
        delay(500)
        return mutex.withLock {
            cliente.copy(id = siguienteId++).also(clientes::add)
        }
    }

    override suspend fun listar(): List<Cliente> {
        delay(500)
        return mutex.withLock { clientes.toList() }
    }
}
