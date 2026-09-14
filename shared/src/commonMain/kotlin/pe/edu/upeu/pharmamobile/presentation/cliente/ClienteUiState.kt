package pe.edu.upeu.pharmamobile.presentation.cliente

import pe.edu.upeu.pharmamobile.domain.model.Cliente

sealed interface ClienteFase {
    data object Cargando : ClienteFase
    data object SinClientes : ClienteFase
    data class ConClientes(val clientes: List<ClienteUi>) : ClienteFase
    data class Error(val mensaje: String) : ClienteFase
}

data class ClienteUi(
    val id: Long,
    val nombre: String,
    val correo: String,
    val telefono: String
) {
    companion object {
        fun from(cliente: Cliente) = ClienteUi(
            cliente.id,
            cliente.nombre,
            cliente.correo,
            cliente.telefono ?: "Sin teléfono"
        )
    }
}

data class ClienteFormularioState(
    val nombre: String = "",
    val correo: String = "",
    val telefono: String = "",
    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorTelefono: String? = null
)

data class ClienteUiState(
    val fase: ClienteFase = ClienteFase.Cargando,
    val formulario: ClienteFormularioState = ClienteFormularioState(),
    val guardando: Boolean = false,
    val mensaje: String? = null
)
