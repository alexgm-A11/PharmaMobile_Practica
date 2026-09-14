package pe.edu.upeu.pharmamobile.domain.usecase

import pe.edu.upeu.pharmamobile.domain.model.Cliente
import pe.edu.upeu.pharmamobile.domain.repository.ClienteRepository

class ListarClientesUseCase(private val repository: ClienteRepository) {
    suspend operator fun invoke(): List<Cliente> = repository.listar()
}
