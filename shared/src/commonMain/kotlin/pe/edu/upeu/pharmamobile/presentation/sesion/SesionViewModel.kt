package pe.edu.upeu.pharmamobile.presentation.sesion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.data.local.EventosSesion
import pe.edu.upeu.pharmamobile.domain.model.Sesion
import pe.edu.upeu.pharmamobile.domain.model.SesionException
import pe.edu.upeu.pharmamobile.domain.usecase.CerrarSesionUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.IniciarSesionUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.RecuperarSesionUseCase

/** El token permanece fuera del estado visible de Compose. */
data class UsuarioSesionUi(
    val usuario: String,
    val nombreCompleto: String,
    val permisos: Set<String>
) {
    fun puede(permiso: String): Boolean = permiso in permisos

    companion object {
        fun desde(sesion: Sesion) = UsuarioSesionUi(
            sesion.usuario, sesion.nombreCompleto, sesion.permisos
        )
    }
}

data class SesionUiState(
    val usuario: String = "",
    val clave: String = "",
    val cargando: Boolean = true,
    val error: String? = null,
    val sesion: UsuarioSesionUi? = null
)

class SesionViewModel(
    private val iniciarSesion: IniciarSesionUseCase,
    private val recuperarSesion: RecuperarSesionUseCase,
    private val cerrarSesion: CerrarSesionUseCase,
    eventos: EventosSesion
) : ViewModel() {
    private val _uiState = MutableStateFlow(SesionUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            eventos.expiraciones.collect { veces ->
                if (veces > 0) {
                    _uiState.update {
                        it.copy(sesion = null, clave = "", cargando = false,
                            error = "La sesión expiró. Inicia sesión de nuevo.")
                    }
                }
            }
        }
        recuperar()
    }

    fun cambiarUsuario(valor: String) { _uiState.update { it.copy(usuario = valor, error = null) } }
    fun cambiarClave(valor: String) { _uiState.update { it.copy(clave = valor, error = null) } }

    fun recuperar() = viewModelScope.launch {
        _uiState.update { it.copy(cargando = true, error = null) }
        try {
            val sesion = recuperarSesion()
            _uiState.update { it.copy(cargando = false, sesion = sesion?.let(UsuarioSesionUi::desde)) }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            _uiState.update { it.copy(cargando = false, sesion = null, error = mensaje(error)) }
        }
    }

    fun iniciar() {
        val estado = _uiState.value
        if (estado.usuario.isBlank() || estado.clave.isBlank()) {
            _uiState.update { it.copy(error = "Ingresa usuario y contraseña.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                val sesion = iniciarSesion(estado.usuario, estado.clave)
                _uiState.update {
                    it.copy(cargando = false, clave = "", error = null,
                        sesion = UsuarioSesionUi.desde(sesion))
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update { it.copy(cargando = false, clave = "", error = mensaje(error)) }
            }
        }
    }

    fun cerrar() = viewModelScope.launch {
        try {
            cerrarSesion()
            _uiState.update { it.copy(sesion = null, clave = "", error = null) }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            _uiState.update { it.copy(error = "No se pudo borrar la sesión segura.") }
        }
    }

    private fun mensaje(error: Exception): String =
        (error as? SesionException)?.message ?: "No se pudo conectar con PharmaSoft."
}
