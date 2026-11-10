package pe.edu.upeu.pharmamobile.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class ScreenTest {

    @Test
    fun contieneLosDestinosDeServidorYDemostracionEnOrden() {
        assertEquals(
            listOf("Inicio", "Productos PharmaSoft", "Clientes PharmaSoft", "Pedidos",
                "Productos (offline-first)", "Clientes locales (demo)", "Catálogo público (Platzi)", "Permisos del dispositivo"),
            Screen.destinos.map(::tituloPantalla)
        )
    }

    @Test
    fun almacen_no_ve_clientes_ni_ventas() {
        assertEquals(
            listOf(Screen.Inicio, Screen.ProductosServidor, Screen.Productos, Screen.Catalogo, Screen.Permisos),
            destinosPermitidos(setOf("CATALOGO_LEER", "CATALOGO_ESCRIBIR"))
        )
    }

    @Test
    fun vendedor_ve_clientes_pero_solo_lee_catalogo() {
        assertEquals(
            listOf(Screen.Inicio, Screen.ProductosServidor, Screen.ClientesServidor,
                Screen.Pedidos, Screen.Productos, Screen.Clientes, Screen.Catalogo, Screen.Permisos),
            destinosPermitidos(setOf("CATALOGO_LEER", "CLIENTES_LEER", "VENTAS_LEER"))
        )
    }

    @Test
    fun modo_offline_permite_inventario_local_y_gestion_local_de_permisos() {
        assertEquals(
            listOf(Screen.Inicio, Screen.Productos, Screen.Permisos),
            destinosOffline(setOf("CATALOGO_LEER", "CATALOGO_ESCRIBIR", "CLIENTES_LEER", "VENTAS_LEER"))
        )
    }
}
