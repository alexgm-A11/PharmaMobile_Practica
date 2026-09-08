package pe.edu.upeu.pharmamobile.presentation.producto

import pe.edu.upeu.pharmamobile.domain.model.Producto

sealed interface ProductoFase {
    data object Cargando : ProductoFase
    data object SinProductos : ProductoFase
    data class ConProductos(val productos: List<Producto>) : ProductoFase
    data class Error(val mensaje: String) : ProductoFase
}

data class ProductoFormularioState(
    val nombre: String = "",
    val precio: String = "",
    val stock: String = "",
    val errorNombre: String? = null,
    val errorPrecio: String? = null,
    val errorStock: String? = null
)

data class ProductoUiState(
    val fase: ProductoFase = ProductoFase.Cargando,
    val formulario: ProductoFormularioState = ProductoFormularioState(),
    val guardando: Boolean = false,
    val mensaje: String? = null
)
