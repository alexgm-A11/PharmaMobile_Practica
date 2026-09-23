package pe.edu.upeu.pharmamobile.presentation.catalogo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import pe.edu.upeu.pharmamobile.domain.model.ProductoCatalogo
import pe.edu.upeu.pharmamobile.data.remote.DiagnosticoConexionApi
import pe.edu.upeu.pharmamobile.domain.repository.ProductoCatalogoRepository

sealed interface CatalogoFase {
    data object Cargando : CatalogoFase
    data class Lista(val productos: List<ProductoCatalogo>) : CatalogoFase
    data class Error(val mensaje: String) : CatalogoFase
}

data class CatalogoUiState(
    val fase: CatalogoFase = CatalogoFase.Cargando,
    val idTexto: String = "",
    val detalle: ProductoCatalogo? = null,
    val mensajeDetalle: String? = null,
    val mensajeDiagnostico: String? = null
)

class CatalogoViewModel(
    private val repository: ProductoCatalogoRepository,
    private val diagnostico: DiagnosticoConexionApi
) : ViewModel() {
    private val _uiState = MutableStateFlow(CatalogoUiState())
    val uiState = _uiState.asStateFlow()

    init { cargar() }

    fun cambiarId(valor: String) { _uiState.update { it.copy(idTexto = valor, mensajeDetalle = null) } }

    fun cargar() = viewModelScope.launch {
        _uiState.update { it.copy(fase = CatalogoFase.Cargando) }
        try {
            val productos = repository.listar()
            _uiState.update { it.copy(fase = CatalogoFase.Lista(productos)) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _uiState.update { it.copy(fase = CatalogoFase.Error(mensajeConexion(e))) }
        }
    }

    fun buscarId() = viewModelScope.launch {
        val id = _uiState.value.idTexto.toIntOrNull()
        if (id == null || id <= 0) {
            _uiState.update { it.copy(detalle = null, mensajeDetalle = "Ingrese un ID positivo") }
            return@launch
        }
        try {
            val producto = repository.obtener(id)
            _uiState.update { it.copy(detalle = producto, mensajeDetalle = null) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _uiState.update { it.copy(detalle = null, mensajeDetalle = mensajeConexion(e)) }
        }
    }

    fun probarTimeout() = viewModelScope.launch {
        _uiState.update { it.copy(mensajeDiagnostico = "Probando límite de 1 ms...") }
        val resultado = diagnostico.probarTimeout()
        _uiState.update { it.copy(mensajeDiagnostico = resultado) }
    }

    fun probarJson() = viewModelScope.launch {
        _uiState.update { it.copy(mensajeDiagnostico = "Comparando JSON...") }
        try {
            val resultado = diagnostico.probarCampoDesconocido()
            _uiState.update { it.copy(mensajeDiagnostico = resultado) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _uiState.update { it.copy(mensajeDiagnostico = mensajeConexion(e)) }
        }
    }

}

fun mensajeConexion(error: Exception): String = when (error) {
        is ClientRequestException -> when (error.response.status.value) {
            404 -> "Producto no encontrado (404)"
            400 -> "Producto no encontrado o solicitud inválida (400)"
            else -> "Error de solicitud (${error.response.status.value})"
        }
        is HttpRequestTimeoutException -> "Tiempo de espera agotado. Intente de nuevo."
        is SerializationException -> "La respuesta del servidor no tiene el formato esperado."
        else -> "Sin conexión o error de red. Verifique Internet e intente de nuevo."
}
