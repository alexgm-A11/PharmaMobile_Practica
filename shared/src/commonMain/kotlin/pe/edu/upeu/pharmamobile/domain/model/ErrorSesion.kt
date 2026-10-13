package pe.edu.upeu.pharmamobile.domain.model

sealed interface ErrorSesion {
    data object CredencialesInvalidas : ErrorSesion
    data object SesionExpirada : ErrorSesion
    data object SinPermiso : ErrorSesion
    data object SinConexion : ErrorSesion
}

class SesionException(val motivo: ErrorSesion) : Exception(
    when (motivo) {
        ErrorSesion.CredencialesInvalidas -> "Usuario o contraseña incorrectos."
        ErrorSesion.SesionExpirada -> "La sesión expiró. Inicia sesión de nuevo."
        ErrorSesion.SinPermiso -> "No tienes permiso para realizar esta acción."
        ErrorSesion.SinConexion -> "No se pudo conectar con PharmaSoft."
    }
)
