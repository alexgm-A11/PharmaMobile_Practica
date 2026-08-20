package pe.edu.upeu.pharmamobile.demo

import kotlinx.coroutines.flow.collect
import pe.edu.upeu.pharmamobile.data.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.domain.result.ResultadoProductos

/**
 * Ejecuta en orden las operaciones asíncronas y flujos del repositorio
 * de productos, imprimiendo cada resultado por consola.
 * Sirve como evidencia de ejecución para el Reto 02 (Pasos 20-21):
 * al correrla desde un test con runTest, la salida por consola
 * confirma la correcta emisión y recolección de los estados asíncronos.
 */
suspend fun probarOperacionesAsincronas() {
    val repositorio = ProductoRepository()

    // 1) Función suspend: retorna un único valor
    println("== suspend fun obtenerProductos() ==")
    val productos = repositorio.obtenerProductos()
    println("Productos obtenidos: $productos")

    // 2) Flow simple de estados (emit/collect)
    println("== Flow<String> observarEstados() ==")
    repositorio.observarEstados().collect { estado ->
        println("Estado: $estado")
    }

    // 3) Flow de productos con actualización de inventario vía copy()
    println("== Flow<List<Producto>> observarProductos() ==")
    repositorio.observarProductos().collect { lista ->
        println("Inventario emitido: $lista")
    }

    // 4) Flow integrado con sealed class ResultadoProductos
    println("== Flow<ResultadoProductos> cargarProductos() ==")
    repositorio.cargarProductos().collect { resultado ->
        when (resultado) {
            is ResultadoProductos.Cargando -> println("Cargando productos...")
            is ResultadoProductos.Exito -> println("Éxito: ${resultado.lista}")
            is ResultadoProductos.Error -> println("Error: ${resultado.mensaje}")
        }
    }
}
