package pe.edu.upeu.pharmamobile.data.remote

import kotlinx.serialization.Serializable
import pe.edu.upeu.pharmamobile.domain.model.ProductoCatalogo

@Serializable
data class CategoriaDto(val id: Int, val name: String)

@Serializable
data class ProductoDto(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String = "",
    val images: List<String> = emptyList(),
    val category: CategoriaDto? = null
) {
    fun toDomain() = ProductoCatalogo(
        id = id,
        nombre = title,
        precio = price,
        descripcion = description,
        imagen = images.firstOrNull(),
        categoria = category?.name
    )
}
