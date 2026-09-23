package pe.edu.upeu.pharmamobile.data.repository

import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.domain.model.ProductoCatalogo
import pe.edu.upeu.pharmamobile.domain.repository.ProductoCatalogoRepository

class ProductoCatalogoRepositorioRest(private val api: ProductoApi) : ProductoCatalogoRepository {
    override suspend fun listar(limit: Int, offset: Int): List<ProductoCatalogo> =
        api.listar(limit, offset).map { it.toDomain() }

    override suspend fun obtener(id: Int): ProductoCatalogo = api.obtener(id).toDomain()
}
