package pe.edu.upeu.pharmamobile.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PaginaDto<T>(val contenido: List<T>)

@Serializable
data class CategoriaServidorDto(val id: Long, val nombre: String)

@Serializable
data class ProductoServidorDto(
    val id: Long,
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val categoriaId: Long
)

@Serializable
data class ProductoGuardarDto(
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val estado: Boolean = true,
    val categoriaId: Long
)

@Serializable
data class ClienteServidorDto(
    val id: Long,
    val dni: String,
    val nombres: String,
    val apellidos: String,
    val email: String,
    val telefono: String? = null,
    val direccion: String? = null
)

@Serializable
data class ClienteGuardarDto(
    val dni: String,
    val nombres: String,
    val apellidos: String,
    val email: String,
    val telefono: String? = null,
    val direccion: String? = null,
    val estado: Boolean = true
)
