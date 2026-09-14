package pe.edu.upeu.pharmamobile.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.domain.usecase.CampoProducto
import pe.edu.upeu.pharmamobile.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ValidacionProductoException

class ProductoViewModel(
    private val registrarProducto: RegistrarProductoUseCase,
    private val listarProductos: ListarProductosUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarProductos()
    }

    fun cambiarNombre(valor: String) = actualizarFormulario { copy(nombre = valor, errorNombre = null) }
    fun cambiarPrecio(valor: String) = actualizarFormulario { copy(precio = valor, errorPrecio = null) }
    fun cambiarStock(valor: String) = actualizarFormulario { copy(stock = valor, errorStock = null) }

    fun guardar() {
        val formulario = _uiState.value.formulario
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, mensaje = null) }
            registrarProducto(formulario.nombre, formulario.precio, formulario.stock)
                .onSuccess { producto ->
                    _uiState.update {
                        it.copy(
                            formulario = ProductoFormularioState(),
                            guardando = false,
                            mensaje = "Producto registrado: ${producto.nombre}"
                        )
                    }
                    cargarProductos()
                }
                .onFailure { error ->
                    val validacion = error as? ValidacionProductoException
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            formulario = it.formulario.copy(
                                errorNombre = error.message.takeIf { validacion?.campo == CampoProducto.NOMBRE },
                                errorPrecio = error.message.takeIf { validacion?.campo == CampoProducto.PRECIO },
                                errorStock = error.message.takeIf { validacion?.campo == CampoProducto.STOCK }
                            ),
                            mensaje = error.message.takeIf { validacion == null }
                        )
                    }
                }
        }
    }

    fun cargarProductos() {
        viewModelScope.launch {
            _uiState.update { it.copy(fase = ProductoFase.Cargando) }
            runCatching { listarProductos() }
                .onSuccess { productos ->
                    _uiState.update {
                        it.copy(
                            fase = if (productos.isEmpty()) ProductoFase.SinProductos
                            else ProductoFase.ConProductos(productos.map(ProductoUi::from))
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(fase = ProductoFase.Error(error.message ?: "No se pudieron cargar los productos"))
                    }
                }
        }
    }

    private fun actualizarFormulario(cambio: ProductoFormularioState.() -> ProductoFormularioState) {
        _uiState.update { it.copy(formulario = it.formulario.cambio(), mensaje = null) }
    }
}
