package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.result.ResultadoProductos

/**
 * Repositorio de productos para el dominio compartido (commonMain).
 * Usa una fuente de datos simulada mientras no existe todavía una
 * capa de red o base de datos real (ver Paso 20: Arquitectura Futura).
 */
class ProductoRepository {

    // Paso 3: fuente temporal de productos simulados para pruebas de dominio
    private val productosSimulados = listOf(
        Producto(id = 1, nombre = "Paracetamol", precio = 8.50, stock = 100),
        Producto(id = 2, nombre = "Ibuprofeno", precio = 12.00, stock = 50),
        Producto(id = 3, nombre = "Amoxicilina", precio = 18.50, stock = 20)
    )

    /**
     * Paso 4-5: operación asíncrona con `suspend`.
     * Retorna un único valor al finalizar; `delay(1000)` emula la espera
     * de una llamada de red o lectura de base de datos local sin
     * bloquear el hilo que la invoca.
     */
    suspend fun obtenerProductos(): List<Producto> {
        delay(1000)
        return productosSimulados
    }

    /**
     * Paso 9-11: introducción a Flow con `emit` y `collect`.
     * A diferencia de `suspend`, Flow puede emitir varios valores
     * a lo largo del tiempo.
     */
    fun observarEstados(): Flow<String> = flow {
        emit("Iniciando")
        delay(1000)
        emit("Finalizado")
    }

    /**
     * Paso 12-14: Flow de productos que emite cambios de inventario.
     * Primero una lista vacía/base, luego la lista simulada, y por
     * último una actualización de stock generada con `copy()` para
     * imitar el movimiento dinámico del inventario.
     */
    fun observarProductos(): Flow<List<Producto>> = flow {
        emit(emptyList())
        delay(1000)
        emit(productosSimulados)
        delay(1000)
        val productosConStockActualizado = productosSimulados.map { producto ->
            producto.copy(stock = producto.stock - 1)
        }
        emit(productosConStockActualizado)
    }

    /**
     * Paso 15-17: flujo integrado de estados.
     * Combina Flow con la sealed class ResultadoProductos para emitir
     * la secuencia Cargando -> Exito (o Error ante alguna falla),
     * lista para conectarse a un ViewModel con StateFlow.
     */
    fun cargarProductos(): Flow<ResultadoProductos> = flow {
        emit(ResultadoProductos.Cargando)
        try {
            delay(1000)
            emit(ResultadoProductos.Exito(productosSimulados))
        } catch (e: Exception) {
            emit(ResultadoProductos.Error(e.message ?: "Error desconocido al cargar productos"))
        }
    }
}
