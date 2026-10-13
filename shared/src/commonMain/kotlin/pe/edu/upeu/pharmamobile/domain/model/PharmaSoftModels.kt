package pe.edu.upeu.pharmamobile.domain.model

data class CategoriaServidor(val id: Long, val nombre: String)

data class ProductoServidor(
    val id: Long,
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val categoriaId: Long
)

data class ProductoEntrada(
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val categoriaId: Long
)

data class ClienteServidor(
    val id: Long,
    val dni: String,
    val nombres: String,
    val apellidos: String,
    val email: String,
    val telefono: String?,
    val direccion: String?
)

data class ClienteEntrada(
    val dni: String,
    val nombres: String,
    val apellidos: String,
    val email: String,
    val telefono: String?,
    val direccion: String?
)
