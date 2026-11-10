package pe.edu.upeu.pharmamobile.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobile.data.local.ProductoLocal
import pe.edu.upeu.pharmamobile.db.PharmaDb
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.model.ResultadoEnvio
import pe.edu.upeu.pharmamobile.domain.repository.SincronizacionProductosRepository
import pe.edu.upeu.pharmamobile.domain.usecase.SincronizarProductosUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProductoRepositorioOfflineTest {
    @Test
    fun `sincronizacion con red actualiza base y guarda fecha`() = runTest {
        val remoto = FakeProductosRemotos(mutableListOf(Producto(11, "Paracetamol", 8.5, 100)))
        val (repo) = nuevoRepo(remoto)

        sincronizar(repo)()

        assertEquals(listOf(Producto(11, "Paracetamol", 8.5, 100)), repo.observar().first())
        assertNotNull(repo.ultimaSincronizacion())
    }

    @Test
    fun `registro sin red se conserva local y no se reenvia solo`() = runTest {
        val remoto = FakeProductosRemotos().apply { disponible = false }
        val (repo, local) = nuevoRepo(remoto)

        val creado = repo.registrar(Producto(0, "Ibuprofeno", 4.5, 20))

        assertTrue(creado.pendiente)
        assertEquals(-1L, creado.id)
        assertEquals(listOf(creado), repo.observar().first())
        assertEquals(listOf(creado), local.pendientes(SincronizarProductosUseCase.MAX_INTENTOS))
        assertEquals(0, remoto.registrados)
        assertTrue(repo.ultimaSincronizacion() == null)
    }

    @Test
    fun `reconexion envia pendiente y lo confirma con id del servidor`() = runTest {
        val remoto = FakeProductosRemotos().apply { disponible = false }
        val (repo, local) = nuevoRepo(remoto)
        repo.registrar(Producto(0, "Ibuprofeno", 4.5, 20))
        remoto.disponible = true

        sincronizar(repo)()

        assertEquals(1, remoto.registrados)
        assertTrue(local.pendientes(SincronizarProductosUseCase.MAX_INTENTOS).isEmpty())
        assertEquals(1, repo.observar().first().size)
        assertTrue(repo.observar().first().single().id > 0)
    }

    @Test
    fun `fallo transitorio seguro aumenta intentos y usa reintento creciente`() = runTest {
        val remoto = FakeProductosRemotos().apply { transitoriosSegurosRestantes = 2 }
        val (repo, local) = nuevoRepo(remoto)
        repo.registrar(Producto(0, "Amoxicilina", 7.0, 12))

        sincronizar(repo)()

        assertEquals(3, remoto.intentosDeEnvio)
        assertTrue(local.pendientes(SincronizarProductosUseCase.MAX_INTENTOS).isEmpty())
        assertTrue(repo.observarFallidas(SincronizarProductosUseCase.MAX_INTENTOS).first() == 0)
    }

    @Test
    fun `fallo permanente queda visible y no se reintenta automaticamente`() = runTest {
        val remoto = FakeProductosRemotos().apply { rechazoPermanente = true }
        val (repo, local) = nuevoRepo(remoto)
        repo.registrar(Producto(0, "Producto inválido", 2.0, 1))

        val resumen = sincronizar(repo)()
        val fallida = local.listar().single()

        assertEquals(1, remoto.intentosDeEnvio)
        assertEquals(1, resumen.fallidas)
        assertEquals(SincronizarProductosUseCase.MAX_INTENTOS, fallida.intentos)
        assertEquals("Validación del servidor", fallida.ultimoError)
        assertEquals(1, repo.observarFallidas(SincronizarProductosUseCase.MAX_INTENTOS).first())
    }

    @Test
    fun `una descarga no elimina productos que siguen pendientes`() = runTest {
        val remoto = FakeProductosRemotos(mutableListOf(Producto(1, "Servidor", 9.0, 10)))
        val (_, local) = nuevoRepo(remoto)
        val localPendiente = local.guardarPendiente(Producto(0, "Sin red", 3.0, 5))

        local.reemplazarSincronizados(remoto.listar())

        assertEquals(listOf(localPendiente), local.pendientes(SincronizarProductosUseCase.MAX_INTENTOS))
        assertEquals(setOf(-1L, 1L), local.listar().map { it.id }.toSet())
    }

    @Test
    fun `ids locales negativos conviven con ids del servidor`() = runTest {
        val remoto = FakeProductosRemotos(mutableListOf(Producto(1, "Servidor", 9.0, 10)))
        val (_, local) = nuevoRepo(remoto)
        local.reemplazarSincronizados(remoto.listar())
        local.guardarPendiente(Producto(0, "Pendiente", 3.0, 5))

        assertEquals(setOf(-1L, 1L), local.listar().map { it.id }.toSet())
        assertEquals(2, local.observar().first().size)
        assertTrue(local.listar().first { it.id < 0 }.pendiente)
        assertFalse(local.listar().first { it.id > 0 }.pendiente)
    }

    @Test
    fun `dos sincronizaciones no duplican filas`() = runTest {
        val remoto = FakeProductosRemotos(mutableListOf(Producto(3, "Servidor", 12.0, 2)))
        val (repo) = nuevoRepo(remoto)

        sincronizar(repo)()
        sincronizar(repo)()

        assertEquals(1, repo.observar().first().size)
        assertEquals(1, remoto.productos.size)
    }

    private fun sincronizar(repo: ProductoRepositorioOffline) = SincronizarProductosUseCase(
        repo as SincronizacionProductosRepository
    )

    private fun nuevoRepo(remoto: FakeProductosRemotos): Pair<ProductoRepositorioOffline, ProductoLocal> {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        PharmaDb.Schema.create(driver)
        val local = ProductoLocal(PharmaDb(driver))
        return ProductoRepositorioOffline(local, remoto) to local
    }
}

private class FakeProductosRemotos(
    val productos: MutableList<Producto> = mutableListOf()
) : ProductosRemotos {
    var disponible = true
    var registrados = 0
    var intentosDeEnvio = 0
    var transitoriosSegurosRestantes = 0
    var rechazoPermanente = false

    override suspend fun listar(): List<Producto> {
        if (!disponible) throw IllegalStateException("Sin conexión simulada")
        return productos.toList()
    }

    override suspend fun enviar(producto: Producto): ResultadoEnvio {
        intentosDeEnvio++
        if (!disponible) return ResultadoEnvio.Transitorio("Sin conexión simulada")
        if (rechazoPermanente) return ResultadoEnvio.Permanente("Validación del servidor")
        if (transitoriosSegurosRestantes > 0) {
            transitoriosSegurosRestantes--
            return ResultadoEnvio.Transitorio("Conexión no establecida", reintentoAutomaticoSeguro = true)
        }
        registrados++
        val id = (productos.maxOfOrNull { it.id } ?: 0L) + 1L
        productos.add(producto.copy(id = id, pendiente = false))
        return ResultadoEnvio.Exito(id)
    }
}
