package pe.edu.upeu.pharmamobile.domain.model

/** Producto remoto: la API pública no proporciona existencias de inventario. */
data class ProductoCatalogo(
    val id: Int,
    val nombre: String,
    val precio: Double,
    val descripcion: String,
    val imagen: String?,
    val categoria: String?
)
