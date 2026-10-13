package pe.edu.upeu.pharmamobile.domain.usecase

import pe.edu.upeu.pharmamobile.domain.model.Credenciales
import pe.edu.upeu.pharmamobile.domain.repository.AuthRepository

class IniciarSesionUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(usuario: String, clave: String) =
        repo.iniciarSesion(Credenciales(usuario.trim(), clave))
}

class RecuperarSesionUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke() = repo.sesionGuardada()
}

class CerrarSesionUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke() = repo.cerrarSesion()
}
