package pe.edu.upeu.pharmamobile.presentation.producto

import pe.edu.upeu.pharmamobile.domain.model.Producto

sealed interface ProductoFase {
    data object Cargando : ProductoFase
    data object SinProductos : ProductoFase
    data class ConProductos(val productos: List<ProductoUi>) : ProductoFase
    data class Error(val mensaje: String) : ProductoFase
}

data class ProductoUi(
    val id: Long,
    val nombre: String,
    val precio: String,
    val stock: Int,
    val requiereReposicion: Boolean
) {
    companion object {
        fun from(producto: Producto): ProductoUi {
            val centimos = (producto.precio * 100 + 0.5).toLong()
            val precio = "S/ ${centimos / 100}.${(centimos % 100).toString().padStart(2, '0')}"
            return ProductoUi(producto.id, producto.nombre, precio, producto.stock, producto.requiereReposicion)
        }
    }
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
