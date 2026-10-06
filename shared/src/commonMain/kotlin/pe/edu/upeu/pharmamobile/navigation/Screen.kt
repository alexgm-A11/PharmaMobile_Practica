package pe.edu.upeu.pharmamobile.navigation

sealed class Screen(val titulo: String) {
    data object Inicio : Screen("Inicio")
    data object Productos : Screen("Productos")
    data object Catalogo : Screen("Catálogo REST")
    data object Clientes : Screen("Clientes")
    data object Pedidos : Screen("Pedidos")
    data class DetalleProducto(val id: Long) : Screen("Detalle del producto")

    companion object {
        val destinos: List<Screen>
            get() = listOf(Inicio, Productos, Catalogo, Clientes, Pedidos)
    }
}

fun tituloPantalla(screen: Screen): String = screen.titulo
