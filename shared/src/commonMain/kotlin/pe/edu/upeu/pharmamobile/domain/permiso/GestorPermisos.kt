package pe.edu.upeu.pharmamobile.domain.permiso

enum class Permiso { CAMARA, UBICACION, NOTIFICACIONES }

sealed interface EstadoPermiso {
    data object Concedido : EstadoPermiso
    data object Denegado : EstadoPermiso
    data object DenegadoParaSiempre : EstadoPermiso
    data object NoDisponible : EstadoPermiso
}

interface GestorPermisos {
    suspend fun estado(permiso: Permiso): EstadoPermiso
    suspend fun solicitar(permiso: Permiso): EstadoPermiso
    fun abrirAjustes()
}

/** Respaldo explícito para plataformas sin puente nativo implementado. */
class GestorPermisosNoDisponible : GestorPermisos {
    override suspend fun estado(permiso: Permiso) = EstadoPermiso.NoDisponible
    override suspend fun solicitar(permiso: Permiso) = EstadoPermiso.NoDisponible
    override fun abrirAjustes() = Unit
}
