package pe.edu.upeu.pharmamobile.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase

class ProductoViewModel(
    private val registrarProducto: RegistrarProductoUseCase,
    private val repository: ProductoRepository
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
        val validado = formulario.copy(
            errorNombre = if (formulario.nombre.isBlank()) "Nombre obligatorio" else null,
            errorPrecio = when {
                formulario.precio.replace(',', '.').toDoubleOrNull() == null -> "Precio inválido"
                formulario.precio.replace(',', '.').toDouble() <= 0 -> "El precio debe ser mayor a 0"
                else -> null
            },
            errorStock = when {
                formulario.stock.toIntOrNull() == null -> "Stock debe ser un número entero"
                formulario.stock.toInt() < 0 -> "Stock no puede ser negativo"
                else -> null
            }
        )
        _uiState.update { it.copy(formulario = validado, mensaje = null) }
        if (listOf(validado.errorNombre, validado.errorPrecio, validado.errorStock).any { it != null }) return

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true) }
            registrarProducto(validado.nombre, validado.precio, validado.stock)
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
                    _uiState.update { it.copy(guardando = false, mensaje = error.message) }
                }
        }
    }

    fun cargarProductos() {
        viewModelScope.launch {
            _uiState.update { it.copy(fase = ProductoFase.Cargando) }
            runCatching { repository.listar() }
                .onSuccess { productos ->
                    _uiState.update {
                        it.copy(
                            fase = if (productos.isEmpty()) ProductoFase.SinProductos
                            else ProductoFase.ConProductos(productos)
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
