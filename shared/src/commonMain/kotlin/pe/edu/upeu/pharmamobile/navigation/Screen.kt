package pe.edu.upeu.pharmamobile.navigation

import pe.edu.upeu.pharmamobile.domain.model.Permisos

sealed class Screen(val titulo: String) {
    data object Inicio : Screen("Inicio")
    data object Productos : Screen("Productos (offline-first)")
    data object ProductosServidor : Screen("Productos PharmaSoft")
    data object Catalogo : Screen("Catálogo público (Platzi)")
    data object Clientes : Screen("Clientes locales (demo)")
    data object ClientesServidor : Screen("Clientes PharmaSoft")
    data object Pedidos : Screen("Pedidos")
    data class DetalleProducto(val id: Long) : Screen("Detalle del producto")

    companion object {
        val destinos: List<Screen>
            get() = listOf(Inicio, ProductosServidor, ClientesServidor, Pedidos, Productos, Clientes, Catalogo)
    }
}

fun tituloPantalla(screen: Screen): String = screen.titulo

/** La UI oculta opciones por permiso; el servidor sigue siendo la autoridad final. */
fun destinosPermitidos(permisos: Set<String>): List<Screen> = Screen.destinos.filter { destino ->
    when (destino) {
        Screen.Productos, Screen.ProductosServidor -> Permisos.CATALOGO_LEER in permisos
        Screen.Clientes, Screen.ClientesServidor -> Permisos.CLIENTES_LEER in permisos
        Screen.Pedidos -> Permisos.VENTAS_LEER in permisos
        else -> true
    }
}

/** En modo sin conexión solo se exponen la portada y el inventario local. */
fun destinosOffline(permisos: Set<String>): List<Screen> = destinosPermitidos(permisos)
    .filter { it == Screen.Inicio || it == Screen.Productos }
