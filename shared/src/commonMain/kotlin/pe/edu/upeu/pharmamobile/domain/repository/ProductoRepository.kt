package pe.edu.upeu.pharmamobile.domain.repository

import pe.edu.upeu.pharmamobile.domain.model.Producto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface ProductoRepository {
    suspend fun registrar(producto: Producto): Producto
    suspend fun listar(): List<Producto>

    /** Fuente de verdad reactiva; las implementaciones offline leen desde almacenamiento local. */
    fun observar(): Flow<List<Producto>> = flow { emit(listar()) }

    /** Sincroniza el almacenamiento local con el servidor, si la implementación lo soporta. */
    suspend fun sincronizar() = Unit

    /** Época Unix en milisegundos de la última sincronización completa correcta. */
    suspend fun ultimaSincronizacion(): Long? = null
}
