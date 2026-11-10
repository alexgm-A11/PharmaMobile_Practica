package pe.edu.upeu.pharmamobile.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.model.ResultadoEnvio

/** Puerto que reúne el almacenamiento local y la comunicación remota para el caso de uso. */
interface SincronizacionProductosRepository {
    suspend fun pendientes(maxIntentos: Int): List<Producto>
    suspend fun enviar(producto: Producto): ResultadoEnvio
    suspend fun confirmar(idLocal: Long, idServidor: Long)
    suspend fun sumarIntento(idLocal: Long, error: String): Int
    suspend fun marcarFallida(idLocal: Long, error: String, maxIntentos: Int)
    suspend fun conciliarRemoto()
    fun observarPendientes(maxIntentos: Int): Flow<Int>
    fun observarFallidas(maxIntentos: Int): Flow<Int>
    suspend fun reintentarFallidas(maxIntentos: Int)
}
