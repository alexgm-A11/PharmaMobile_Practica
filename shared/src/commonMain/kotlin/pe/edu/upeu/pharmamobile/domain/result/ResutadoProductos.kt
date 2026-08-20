package pe.edu.upeu.pharmamobile.domain.result

import pe.edu.upeu.pharmamobile.domain.model.Producto

/**
 * Jerarquía de estados para una operación asíncrona sobre productos.
 * Al ser una sealed class, el compilador exige evaluar cada rama en un
 * bloque `when`, evitando estados no controlados en la UI.
 */
sealed class ResultadoProductos {
    data object Cargando : ResultadoProductos()
    data class Exito(val lista: List<Producto>) : ResultadoProductos()
    data class Error(val mensaje: String) : ResultadoProductos()
}
