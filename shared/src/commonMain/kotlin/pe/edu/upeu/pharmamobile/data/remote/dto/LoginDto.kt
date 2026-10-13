package pe.edu.upeu.pharmamobile.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class LoginRequestDto(val username: String, val password: String)

@Serializable
data class LoginResponseDto(
    val token: String,
    val tipo: String,
    val expiraEnSegundos: Long,
    val username: String,
    val nombreCompleto: String,
    val roles: List<String> = emptyList(),
    val permisos: List<String> = emptyList()
)

@Serializable
data class UsuarioActualDto(
    val username: String,
    @SerialName("empleadoNombre")
    val nombreCompleto: String = "",
    val roles: List<String> = emptyList(),
    val permisos: List<String> = emptyList()
)
