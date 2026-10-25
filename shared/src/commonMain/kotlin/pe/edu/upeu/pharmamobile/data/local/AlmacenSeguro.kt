package pe.edu.upeu.pharmamobile.data.local

import kotlinx.serialization.Serializable

@Serializable
data class PerfilSesionOffline(
    val usuario: String,
    val nombreCompleto: String,
    val permisos: Set<String>
)

/** El token solo se persiste mediante el almacén protegido del sistema operativo. */
interface AlmacenSeguro {
    fun guardarToken(token: String)
    fun leerToken(): String?
    fun borrarToken()
    fun guardarPerfilSesion(perfil: PerfilSesionOffline) = Unit
    fun leerPerfilSesion(): PerfilSesionOffline? = null
    fun borrarPerfilSesion() = Unit
}
