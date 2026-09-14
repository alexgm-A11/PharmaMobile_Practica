package pe.edu.upeu.pharmamobile.domain.usecase

import pe.edu.upeu.pharmamobile.domain.model.Cliente
import pe.edu.upeu.pharmamobile.domain.repository.ClienteRepository

enum class CampoCliente { NOMBRE, CORREO, TELEFONO }

class ValidacionClienteException(
    val campo: CampoCliente,
    message: String
) : IllegalArgumentException(message)

class RegistrarClienteUseCase(private val repository: ClienteRepository) {
    suspend operator fun invoke(nombre: String, correo: String, telefono: String): Result<Cliente> = runCatching {
        val nombreLimpio = nombre.trim()
        if (nombreLimpio.isEmpty()) throw ValidacionClienteException(CampoCliente.NOMBRE, "Nombre obligatorio")
        if (nombreLimpio.length < 3) throw ValidacionClienteException(CampoCliente.NOMBRE, "El nombre debe tener al menos 3 caracteres")
        if (!nombreLimpio.any(Char::isLetter) || nombreLimpio.any(Char::isDigit)) {
            throw ValidacionClienteException(CampoCliente.NOMBRE, "Ingrese un nombre válido")
        }

        val correoLimpio = correo.trim().lowercase()
        if (correoLimpio.isEmpty()) throw ValidacionClienteException(CampoCliente.CORREO, "Correo obligatorio")
        if (!Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(correoLimpio)) {
            throw ValidacionClienteException(CampoCliente.CORREO, "Ingrese un correo válido")
        }

        val telefonoLimpio = telefono.trim()
        if (telefonoLimpio.isNotEmpty() && (telefonoLimpio.length != 9 || !telefonoLimpio.all(Char::isDigit))) {
            throw ValidacionClienteException(CampoCliente.TELEFONO, "El teléfono debe tener 9 dígitos")
        }
        repository.registrar(Cliente(0L, nombreLimpio, correoLimpio, telefonoLimpio.ifEmpty { null }))
    }
}
