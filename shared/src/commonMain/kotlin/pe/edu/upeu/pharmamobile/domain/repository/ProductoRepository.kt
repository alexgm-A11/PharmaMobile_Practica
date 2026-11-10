package pe.edu.upeu.pharmamobile.domain.repository

import pe.edu.upeu.pharmamobile.domain.model.Producto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

interface ProductoRepository {
    suspend fun registrar(producto: Producto): Producto
    suspend fun listar(): List<Producto>

    /** Fuente de verdad reactiva; las implementaciones offline leen desde almacenamiento local. */
    fun observar(): Flow<List<Producto>> = flow { emit(listar()) }

    /** Época Unix en milisegundos de la última sincronización completa correcta. */
    suspend fun ultimaSincronizacion(): Long? = null

    fun observarPendientes(maxIntentos: Int): Flow<Int> = flowOf(0)

    fun observarFallidas(maxIntentos: Int): Flow<Int> = flowOf(0)

    suspend fun reintentarFallidas(maxIntentos: Int) = Unit
}
