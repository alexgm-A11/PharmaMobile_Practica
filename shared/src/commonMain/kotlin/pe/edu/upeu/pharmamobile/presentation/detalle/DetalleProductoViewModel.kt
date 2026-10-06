package pe.edu.upeu.pharmamobile.presentation.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.platform.Compartidor
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.domain.usecase.comoTextoParaCompartir
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoUi

sealed interface DetalleProductoUiState {
    data object Cargando : DetalleProductoUiState
    data object NoEncontrado : DetalleProductoUiState
    data class Contenido(val producto: ProductoUi) : DetalleProductoUiState
    data class Error(val mensaje: String) : DetalleProductoUiState
}

class DetalleProductoViewModel(
    private val repository: ProductoRepository,
    private val compartidor: Compartidor
) : ViewModel() {
    private val _uiState = MutableStateFlow<DetalleProductoUiState>(DetalleProductoUiState.Cargando)
    val uiState = _uiState.asStateFlow()
    private var productoActual: Producto? = null

    fun cargar(id: Long) = viewModelScope.launch {
        _uiState.value = DetalleProductoUiState.Cargando
        try {
            val producto = repository.listar().firstOrNull { it.id == id }
            productoActual = producto
            _uiState.value = if (producto == null) DetalleProductoUiState.NoEncontrado
            else DetalleProductoUiState.Contenido(ProductoUi.from(producto))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _uiState.value = DetalleProductoUiState.Error(e.message ?: "No se pudo cargar el producto")
        }
    }

    fun compartir() {
        val producto = productoActual ?: return
        try {
            compartidor.compartir(producto.comoTextoParaCompartir())
        } catch (e: Exception) {
            _uiState.value = DetalleProductoUiState.Error(e.message ?: "No se pudo abrir la opción para compartir")
        }
    }
}
