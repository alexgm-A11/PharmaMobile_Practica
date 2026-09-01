package pe.edu.upeu.pharmamobile.navigation

sealed class Screen(val titulo: String) {
    data object Inicio : Screen("Inicio")
    data object Productos : Screen("Productos")
    data object Clientes : Screen("Clientes")
    data object Pedidos : Screen("Pedidos")

    companion object {
        val destinos: List<Screen>
            get() = listOf(Inicio, Productos, Clientes, Pedidos)
    }
}

fun tituloPantalla(screen: Screen): String = screen.titulo
