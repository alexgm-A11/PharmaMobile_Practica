package pe.edu.upeu.pharmamobile.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobile.data.local.ProductoLocal
import pe.edu.upeu.pharmamobile.db.PharmaDb
import pe.edu.upeu.pharmamobile.domain.model.Producto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProductoRepositorioOfflineTest {
    @Test
    fun `sincronizacion con red actualiza base y emite por Flow`() = runTest {
        val remoto = FakeProductosRemotos(mutableListOf(Producto(11, "Paracetamol", 8.5, 100)))
        val (repo) = nuevoRepo(remoto)

        repo.sincronizar()

        assertEquals(listOf(Producto(11, "Paracetamol", 8.5, 100)), repo.observar().first())
        assertNotNull(repo.ultimaSincronizacion())
    }

    @Test
    fun `registro sin red se conserva pendiente y el Flow lo muestra`() = runTest {
        val remoto = FakeProductosRemotos().apply { disponible = false }
        val (repo, local) = nuevoRepo(remoto)

        val creado = repo.registrar(Producto(0, "Ibuprofeno", 4.5, 20))

        assertTrue(creado.pendiente)
        assertEquals(-1L, creado.id)
        assertEquals(listOf(creado), repo.observar().first())
        assertEquals(listOf(creado), local.pendientes())
        assertTrue(repo.ultimaSincronizacion() == null)
    }

    @Test
    fun `reconexion envia pendiente y lo confirma con id del servidor`() = runTest {
        val remoto = FakeProductosRemotos().apply { disponible = false }
        val (repo, local) = nuevoRepo(remoto)
        repo.registrar(Producto(0, "Ibuprofeno", 4.5, 20))
        remoto.disponible = true

        repo.sincronizar()

        assertEquals(1, remoto.registrados)
        assertTrue(local.pendientes().isEmpty())
        assertEquals(1, repo.observar().first().size)
        assertTrue(repo.observar().first().single().id > 0)
    }

    @Test
    fun `la descarga no elimina productos que siguen pendientes`() = runTest {
        val remoto = FakeProductosRemotos(mutableListOf(Producto(1, "Servidor", 9.0, 10)))
        val (_, local) = nuevoRepo(remoto)
        val localPendiente = local.guardarPendiente(Producto(0, "Sin red", 3.0, 5))

        local.reemplazarSincronizados(remoto.listar())

        assertEquals(listOf(localPendiente), local.pendientes())
        assertEquals(setOf(-1L, 1L), local.listar().map { it.id }.toSet())
    }

    @Test
    fun `ids locales negativos conviven con ids positivos del servidor`() = runTest {
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
    fun `sincronizar varias veces no duplica filas`() = runTest {
        val remoto = FakeProductosRemotos(mutableListOf(Producto(3, "Servidor", 12.0, 2)))
        val (repo) = nuevoRepo(remoto)

        repo.sincronizar()
        repo.sincronizar()

        assertEquals(1, repo.observar().first().size)
        assertEquals(1, remoto.productos.size)
    }

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

    override suspend fun listar(): List<Producto> {
        if (!disponible) throw IllegalStateException("Sin conexión simulada")
        return productos.toList()
    }

    override suspend fun registrar(producto: Producto): Producto {
        if (!disponible) throw IllegalStateException("Sin conexión simulada")
        registrados++
        return producto.copy(id = (productos.maxOfOrNull { it.id } ?: 0L) + 1L, pendiente = false)
            .also(productos::add)
    }
}
