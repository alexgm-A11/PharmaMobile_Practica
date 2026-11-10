package pe.edu.upeu.pharmamobile.presentation.permiso

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobile.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobile.domain.permiso.Permiso

data class PermisosUiState(
    val estados: Map<Permiso, EstadoPermiso> = Permiso.entries.associateWith { EstadoPermiso.Denegado },
    val cargando: Boolean = true
)

class PermisosViewModel(private val gestor: GestorPermisos) : ViewModel() {
    private val _uiState = MutableStateFlow(PermisosUiState())
    val uiState = _uiState.asStateFlow()

    init { actualizarEstados() }

    fun actualizarEstados() = viewModelScope.launch {
        val estados = Permiso.entries.associateWith { gestor.estado(it) }
        _uiState.update { it.copy(estados = estados, cargando = false) }
    }

    fun solicitar(permiso: Permiso) = viewModelScope.launch {
        _uiState.update { it.copy(cargando = true) }
        val estado = gestor.solicitar(permiso)
        _uiState.update { it.copy(estados = it.estados + (permiso to estado), cargando = false) }
    }

    fun abrirAjustes() = gestor.abrirAjustes()
}
