package pe.edu.upeu.pharmamobile.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import pe.edu.upeu.pharmamobile.data.remote.dto.CategoriaServidorDto
import pe.edu.upeu.pharmamobile.data.remote.dto.ClienteGuardarDto
import pe.edu.upeu.pharmamobile.data.remote.dto.ClienteServidorDto
import pe.edu.upeu.pharmamobile.data.remote.dto.PaginaDto
import pe.edu.upeu.pharmamobile.data.remote.dto.ProductoGuardarDto
import pe.edu.upeu.pharmamobile.data.remote.dto.ProductoServidorDto
import pe.edu.upeu.pharmamobile.domain.model.CategoriaServidor
import pe.edu.upeu.pharmamobile.domain.model.ClienteEntrada
import pe.edu.upeu.pharmamobile.domain.model.ClienteServidor
import pe.edu.upeu.pharmamobile.domain.model.ProductoEntrada
import pe.edu.upeu.pharmamobile.domain.model.ProductoServidor
import pe.edu.upeu.pharmamobile.domain.repository.PharmaSoftRepository

/** Todas las llamadas pasan por el mismo cliente Ktor con Auth; ninguna escribe Authorization. */
class PharmaSoftRepositorioRest(private val cliente: HttpClient) : PharmaSoftRepository {
    override suspend fun listarCategorias(): List<CategoriaServidor> =
        cliente.get("categorias").body<List<CategoriaServidorDto>>()
            .map { CategoriaServidor(it.id, it.nombre) }

    override suspend fun listarProductos(): List<ProductoServidor> =
        cliente.get("productos").body<PaginaDto<ProductoServidorDto>>()
            .contenido.map(ProductoServidorDto::dominio)

    override suspend fun guardarProducto(entrada: ProductoEntrada, id: Long?): ProductoServidor {
        val cuerpo = ProductoGuardarDto(entrada.nombre, entrada.precio, entrada.stock, categoriaId = entrada.categoriaId)
        val respuesta = if (id == null) cliente.post("productos") { setBody(cuerpo) }
            else cliente.put("productos/$id") { setBody(cuerpo) }
        return respuesta.body<ProductoServidorDto>().dominio()
    }

    override suspend fun eliminarProducto(id: Long) { cliente.delete("productos/$id") }

    override suspend fun listarClientes(): List<ClienteServidor> =
        cliente.get("clientes").body<PaginaDto<ClienteServidorDto>>()
            .contenido.map(ClienteServidorDto::dominio)

    override suspend fun guardarCliente(entrada: ClienteEntrada, id: Long?): ClienteServidor {
        val cuerpo = ClienteGuardarDto(
            entrada.dni, entrada.nombres, entrada.apellidos, entrada.email,
            entrada.telefono, entrada.direccion
        )
        val respuesta = if (id == null) cliente.post("clientes") { setBody(cuerpo) }
            else cliente.put("clientes/$id") { setBody(cuerpo) }
        return respuesta.body<ClienteServidorDto>().dominio()
    }

    override suspend fun eliminarCliente(id: Long) { cliente.delete("clientes/$id") }
}

private fun ProductoServidorDto.dominio() = ProductoServidor(id, nombre, precio, stock, categoriaId)
private fun ClienteServidorDto.dominio() = ClienteServidor(id, dni, nombres, apellidos, email, telefono, direccion)
