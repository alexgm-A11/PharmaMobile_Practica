package pe.edu.upeu.pharmamobile.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class ScreenTest {

    @Test
    fun contieneLosDestinosDeServidorYDemostracionEnOrden() {
        assertEquals(
            listOf("Inicio", "Productos PharmaSoft", "Clientes PharmaSoft", "Pedidos",
                "Productos (offline-first)", "Clientes locales (demo)", "Catálogo público (Platzi)"),
            Screen.destinos.map(::tituloPantalla)
        )
    }

    @Test
    fun almacen_no_ve_clientes_ni_ventas() {
        assertEquals(
            listOf(Screen.Inicio, Screen.ProductosServidor, Screen.Productos, Screen.Catalogo),
            destinosPermitidos(setOf("CATALOGO_LEER", "CATALOGO_ESCRIBIR"))
        )
    }

    @Test
    fun vendedor_ve_clientes_pero_solo_lee_catalogo() {
        assertEquals(
            listOf(Screen.Inicio, Screen.ProductosServidor, Screen.ClientesServidor,
                Screen.Pedidos, Screen.Productos, Screen.Clientes, Screen.Catalogo),
            destinosPermitidos(setOf("CATALOGO_LEER", "CLIENTES_LEER", "VENTAS_LEER"))
        )
    }

    @Test
    fun modo_offline_solo_permite_inicio_e_inventario_local() {
        assertEquals(
            listOf(Screen.Inicio, Screen.Productos),
            destinosOffline(setOf("CATALOGO_LEER", "CATALOGO_ESCRIBIR", "CLIENTES_LEER", "VENTAS_LEER"))
        )
    }
}
