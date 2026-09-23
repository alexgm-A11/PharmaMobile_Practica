package pe.edu.upeu.pharmamobile.domain.repository

import pe.edu.upeu.pharmamobile.domain.model.ProductoCatalogo

interface ProductoCatalogoRepository {
    suspend fun listar(limit: Int = 10, offset: Int = 0): List<ProductoCatalogo>
    suspend fun obtener(id: Int): ProductoCatalogo
}
