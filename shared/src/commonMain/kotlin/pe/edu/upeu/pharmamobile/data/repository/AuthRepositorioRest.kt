package pe.edu.upeu.pharmamobile.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.AuthCircuitBreaker
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobile.data.remote.dto.LoginRequestDto
import pe.edu.upeu.pharmamobile.data.remote.dto.LoginResponseDto
import pe.edu.upeu.pharmamobile.data.remote.dto.UsuarioActualDto
import pe.edu.upeu.pharmamobile.domain.model.Credenciales
import pe.edu.upeu.pharmamobile.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobile.domain.model.Sesion
import pe.edu.upeu.pharmamobile.domain.model.SesionException
import pe.edu.upeu.pharmamobile.domain.repository.AuthRepository

class AuthRepositorioRest(
    private val cliente: HttpClient,
    private val almacen: AlmacenSeguro
) : AuthRepository {
    override suspend fun iniciarSesion(credenciales: Credenciales): Sesion {
        try {
            val dto: LoginResponseDto = cliente.post("auth/login") {
                // El 401 de credenciales inválidas nunca debe reintentarse con un token anterior.
                attributes.put(AuthCircuitBreaker, Unit)
                setBody(LoginRequestDto(credenciales.usuario, credenciales.clave))
            }.body()
            if (dto.token.isBlank()) throw SesionException(ErrorSesion.SinConexion)
            almacen.guardarToken(dto.token)
            cliente.authProvider<BearerAuthProvider>()?.clearToken()
            return Sesion(dto.token, dto.username, dto.nombreCompleto, dto.permisos.toSet())
        } catch (error: CancellationException) {
            throw error
        } catch (error: SesionException) {
            throw error
        } catch (error: ClientRequestException) {
            if (error.response.status == HttpStatusCode.Unauthorized) {
                throw SesionException(ErrorSesion.CredencialesInvalidas)
            }
            throw SesionException(ErrorSesion.SinConexion)
        } catch (error: Exception) {
            throw SesionException(ErrorSesion.SinConexion)
        }
    }

    override suspend fun sesionGuardada(): Sesion? {
        val token = almacen.leerToken()?.takeIf(String::isNotBlank) ?: return null
        try {
            val dto: UsuarioActualDto = cliente.get("auth/me").body()
            return Sesion(token, dto.username, dto.nombreCompleto, dto.permisos.toSet())
        } catch (error: CancellationException) {
            throw error
        } catch (error: SesionException) {
            throw error
        } catch (error: Exception) {
            throw SesionException(ErrorSesion.SinConexion)
        }
    }

    override suspend fun cerrarSesion() {
        almacen.borrarToken()
        cliente.authProvider<BearerAuthProvider>()?.clearToken()
    }
}
