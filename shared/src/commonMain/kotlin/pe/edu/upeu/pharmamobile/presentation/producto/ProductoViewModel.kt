package pe.edu.upeu.pharmamobile.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.data.local.epochMillisNow
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.domain.port.Telemetria
import pe.edu.upeu.pharmamobile.domain.usecase.CampoProducto
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.SincronizarProductosUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ValidacionProductoException

class ProductoViewModel(
    private val registrarProducto: RegistrarProductoUseCase,
    private val repository: ProductoRepository,
    private val sincronizarProductos: SincronizarProductosUseCase,
    private val telemetria: Telemetria
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observar().collect { productos ->
                _uiState.update {
                    it.copy(
                        fase = if (productos.isEmpty()) ProductoFase.SinProductos
                        else ProductoFase.ConProductos(
                            productos.map(ProductoUi::from).sortedBy { producto -> producto.nombre.lowercase() }
                        )
                    )
                }
            }
        }
        viewModelScope.launch {
            combine(
                repository.observarPendientes(SincronizarProductosUseCase.MAX_INTENTOS),
                repository.observarFallidas(SincronizarProductosUseCase.MAX_INTENTOS)
            ) { pendientes, fallidas -> pendientes to fallidas }
                .collect { (pendientes, fallidas) ->
                    _uiState.update { it.copy(pendientes = pendientes, fallidas = fallidas) }
                }
        }
        sincronizar()
    }

    fun cambiarNombre(valor: String) = actualizarFormulario { copy(nombre = valor, errorNombre = null) }
    fun cambiarPrecio(valor: String) = actualizarFormulario { copy(precio = valor, errorPrecio = null) }
    fun cambiarStock(valor: String) = actualizarFormulario { copy(stock = valor, errorStock = null) }

    fun guardar() {
        val formulario = _uiState.value.formulario
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, mensaje = null) }
            val resultado = registrarProducto(formulario.nombre, formulario.precio, formulario.stock)
            resultado.fold(
                onSuccess = { producto ->
                    _uiState.update { it.copy(formulario = ProductoFormularioState()) }
                    val resumen = try {
                        sincronizarProductos()
                    } catch (cancelado: CancellationException) {
                        throw cancelado
                    } catch (error: Exception) {
                        telemetria.registrarError(error, mapOf("screen" to "inventory", "operation" to "create_sync"))
                        null
                    }
                    _uiState.update {
                        it.copy(
                            guardando = false,
                            mensaje = if (resumen != null && resumen.pendientes == 0 && resumen.fallidas == 0) {
                                "Producto registrado y sincronizado: ${producto.nombre}"
                            } else {
                                "${producto.nombre} guardado localmente. Pendientes: ${resumen?.pendientes ?: it.pendientes}. Revisa los cambios fallidos antes de reenviar."
                            }
                        )
                    }
                    cargarUltimaSincronizacion()
                },
                onFailure = { error ->
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
            )
        }
    }

    fun cargarProductos() = sincronizar()

    fun sincronizar() {
        viewModelScope.launch {
            _uiState.update { it.copy(sincronizando = true, avisoRed = null) }
            try {
                val resumen = sincronizarProductos()
                _uiState.update {
                    it.copy(
                        sincronizando = false,
                        avisoRed = when {
                            resumen.fallidas > 0 -> "${resumen.fallidas} cambio(s) requieren revisión antes de reenviar."
                            resumen.pendientes > 0 -> "Sin conexión: ${resumen.pendientes} cambio(s) por enviar."
                            else -> null
                        }
                    )
                }
            } catch (cancelado: CancellationException) {
                throw cancelado
            } catch (error: Exception) {
                telemetria.registrarError(error, mapOf("screen" to "inventory", "operation" to "sync"))
                _uiState.update {
                    it.copy(
                        sincronizando = false,
                        avisoRed = "Sin conexión: mostrando los datos guardados en este dispositivo."
                    )
                }
            }
            cargarUltimaSincronizacion()
        }
    }

    fun reintentarFallidas() {
        viewModelScope.launch {
            repository.reintentarFallidas(SincronizarProductosUseCase.MAX_INTENTOS)
            sincronizar()
        }
    }

    private suspend fun cargarUltimaSincronizacion() {
        val instante = repository.ultimaSincronizacion()
        _uiState.update { it.copy(ultimaSincronizacion = instante?.let(::tiempoRelativo) ?: "Nunca sincronizado") }
    }

    private fun tiempoRelativo(instante: Long): String {
        val minutos = ((epochMillisNow() - instante).coerceAtLeast(0L) / 60_000L)
        return when {
            minutos == 0L -> "Actualizado hace menos de un minuto"
            minutos < 60 -> "Actualizado hace $minutos min"
            minutos < 1_440 -> "Actualizado hace ${minutos / 60} h"
            else -> "Actualizado hace ${minutos / 1_440} días"
        }
    }

    private fun actualizarFormulario(cambio: ProductoFormularioState.() -> ProductoFormularioState) {
        _uiState.update { it.copy(formulario = it.formulario.cambio(), mensaje = null) }
    }
}
