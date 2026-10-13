package pe.edu.upeu.pharmamobile.presentation.pharmasoft

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.domain.model.CategoriaServidor
import pe.edu.upeu.pharmamobile.domain.model.ClienteServidor
import pe.edu.upeu.pharmamobile.domain.model.ProductoServidor
import pe.edu.upeu.pharmamobile.domain.model.SesionException
import pe.edu.upeu.pharmamobile.domain.usecase.GestionPharmaSoftUseCase
import pe.edu.upeu.pharmamobile.platform.formatearSoles

data class ProductoServidorUi(val producto: ProductoServidor, val precioFormateado: String)

data class FormularioProductoServidor(
    val id: Long? = null,
    val nombre: String = "",
    val precio: String = "",
    val stock: String = "",
    val categoriaId: String = ""
)

data class ProductosServidorUiState(
    val cargando: Boolean = false,
    val categorias: List<CategoriaServidor> = emptyList(),
    val productos: List<ProductoServidorUi> = emptyList(),
    val formulario: FormularioProductoServidor = FormularioProductoServidor(),
    val mensaje: String? = null
)

class ProductosServidorViewModel(private val gestion: GestionPharmaSoftUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductosServidorUiState())
    val uiState = _uiState.asStateFlow()

    init { cargar() }

    fun formulario(cambio: (FormularioProductoServidor) -> FormularioProductoServidor) {
        _uiState.update { it.copy(formulario = cambio(it.formulario), mensaje = null) }
    }

    fun editar(producto: ProductoServidor) {
        _uiState.update {
            it.copy(formulario = FormularioProductoServidor(producto.id, producto.nombre,
                producto.precio.toString(), producto.stock.toString(), producto.categoriaId.toString()))
        }
    }

    fun limpiar() { _uiState.update { it.copy(formulario = FormularioProductoServidor()) } }

    fun cargar() = viewModelScope.launch {
        _uiState.update { it.copy(cargando = true) }
        try {
            val categorias = gestion.categorias()
            val productos = gestion.productos()
            _uiState.update {
                it.copy(cargando = false, categorias = categorias,
                    productos = productos.map { producto ->
                        ProductoServidorUi(producto, formatearSoles(producto.precio))
                    })
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            _uiState.update { it.copy(cargando = false, mensaje = mensajeBackend(error)) }
        }
    }

    fun guardar() = viewModelScope.launch {
        val f = _uiState.value.formulario
        _uiState.update { it.copy(cargando = true, mensaje = null) }
        try {
            gestion.guardarProducto(f.nombre, f.precio, f.stock, f.categoriaId, f.id)
            _uiState.update { it.copy(cargando = false, formulario = FormularioProductoServidor(),
                mensaje = "Producto guardado en PharmaSoft.") }
            cargar()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            _uiState.update { it.copy(cargando = false, mensaje = mensajeBackend(error)) }
        }
    }

    fun eliminar(id: Long) = viewModelScope.launch {
        _uiState.update { it.copy(cargando = true, mensaje = null) }
        try {
            gestion.eliminarProducto(id)
            _uiState.update { it.copy(cargando = false, mensaje = "Producto eliminado de PharmaSoft.") }
            cargar()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            _uiState.update { it.copy(cargando = false, mensaje = mensajeBackend(error)) }
        }
    }
}

data class FormularioClienteServidor(
    val id: Long? = null,
    val dni: String = "",
    val nombres: String = "",
    val apellidos: String = "",
    val email: String = "",
    val telefono: String = "",
    val direccion: String = ""
)

data class ClientesServidorUiState(
    val cargando: Boolean = false,
    val clientes: List<ClienteServidor> = emptyList(),
    val formulario: FormularioClienteServidor = FormularioClienteServidor(),
    val mensaje: String? = null
)

class ClientesServidorViewModel(private val gestion: GestionPharmaSoftUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow(ClientesServidorUiState())
    val uiState = _uiState.asStateFlow()

    init { cargar() }

    fun formulario(cambio: (FormularioClienteServidor) -> FormularioClienteServidor) {
        _uiState.update { it.copy(formulario = cambio(it.formulario), mensaje = null) }
    }

    fun editar(cliente: ClienteServidor) {
        _uiState.update {
            it.copy(formulario = FormularioClienteServidor(cliente.id, cliente.dni, cliente.nombres,
                cliente.apellidos, cliente.email, cliente.telefono.orEmpty(), cliente.direccion.orEmpty()))
        }
    }

    fun limpiar() { _uiState.update { it.copy(formulario = FormularioClienteServidor()) } }

    fun cargar() = viewModelScope.launch {
        _uiState.update { it.copy(cargando = true) }
        try {
            val clientes = gestion.clientes()
            _uiState.update { it.copy(cargando = false, clientes = clientes) }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            _uiState.update { it.copy(cargando = false, mensaje = mensajeBackend(error)) }
        }
    }

    fun guardar() = viewModelScope.launch {
        val f = _uiState.value.formulario
        _uiState.update { it.copy(cargando = true, mensaje = null) }
        try {
            gestion.guardarCliente(f.dni, f.nombres, f.apellidos, f.email, f.telefono, f.direccion, f.id)
            _uiState.update { it.copy(cargando = false, formulario = FormularioClienteServidor(),
                mensaje = "Cliente guardado en PharmaSoft.") }
            cargar()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            _uiState.update { it.copy(cargando = false, mensaje = mensajeBackend(error)) }
        }
    }

    fun eliminar(id: Long) = viewModelScope.launch {
        _uiState.update { it.copy(cargando = true, mensaje = null) }
        try {
            gestion.eliminarCliente(id)
            _uiState.update { it.copy(cargando = false, mensaje = "Cliente eliminado de PharmaSoft.") }
            cargar()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            _uiState.update { it.copy(cargando = false, mensaje = mensajeBackend(error)) }
        }
    }
}

private fun mensajeBackend(error: Exception): String = when (error) {
    is SesionException -> error.message ?: "Error de sesión."
    is IllegalArgumentException -> error.message ?: "Revisa los datos del formulario."
    is ClientRequestException -> when (error.response.status.value) {
        400 -> "Datos rechazados por PharmaSoft. Revisa el formulario."
        404 -> "Registro no encontrado en PharmaSoft."
        409 -> "El registro ya existe en PharmaSoft."
        else -> "Error HTTP ${error.response.status.value}."
    }
    else -> "No se pudo conectar con PharmaSoft. Verifica el servidor y vuelve a intentar."
}
