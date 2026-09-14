package pe.edu.upeu.pharmamobile.presentation.cliente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.domain.usecase.CampoCliente
import pe.edu.upeu.pharmamobile.domain.usecase.ListarClientesUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarClienteUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ValidacionClienteException

class ClienteViewModel(
    private val registrarCliente: RegistrarClienteUseCase,
    private val listarClientes: ListarClientesUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ClienteUiState())
    val uiState = _uiState.asStateFlow()

    init { cargarClientes() }

    fun cambiarNombre(valor: String) = actualizarFormulario { copy(nombre = valor, errorNombre = null) }
    fun cambiarCorreo(valor: String) = actualizarFormulario { copy(correo = valor, errorCorreo = null) }
    fun cambiarTelefono(valor: String) = actualizarFormulario {
        copy(telefono = valor.filter(Char::isDigit).take(9), errorTelefono = null)
    }

    fun guardar() {
        val formulario = _uiState.value.formulario
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, mensaje = null) }
            registrarCliente(formulario.nombre, formulario.correo, formulario.telefono)
                .onSuccess { cliente ->
                    _uiState.update {
                        it.copy(
                            formulario = ClienteFormularioState(),
                            guardando = false,
                            mensaje = "Cliente registrado: ${cliente.nombre}"
                        )
                    }
                    cargarClientes()
                }
                .onFailure { error ->
                    val validacion = error as? ValidacionClienteException
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            formulario = it.formulario.copy(
                                errorNombre = error.message.takeIf { validacion?.campo == CampoCliente.NOMBRE },
                                errorCorreo = error.message.takeIf { validacion?.campo == CampoCliente.CORREO },
                                errorTelefono = error.message.takeIf { validacion?.campo == CampoCliente.TELEFONO }
                            ),
                            mensaje = error.message.takeIf { validacion == null }
                        )
                    }
                }
        }
    }

    fun cargarClientes() {
        viewModelScope.launch {
            _uiState.update { it.copy(fase = ClienteFase.Cargando) }
            runCatching { listarClientes() }
                .onSuccess { clientes ->
                    _uiState.update {
                        it.copy(
                            fase = if (clientes.isEmpty()) ClienteFase.SinClientes
                            else ClienteFase.ConClientes(clientes.map(ClienteUi::from))
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(fase = ClienteFase.Error(error.message ?: "No se pudieron cargar los clientes")) }
                }
        }
    }

    private fun actualizarFormulario(cambio: ClienteFormularioState.() -> ClienteFormularioState) {
        _uiState.update { it.copy(formulario = it.formulario.cambio(), mensaje = null) }
    }
}
