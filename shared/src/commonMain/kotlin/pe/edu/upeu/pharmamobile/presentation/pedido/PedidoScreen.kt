package pe.edu.upeu.pharmamobile.presentation.pedido

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PedidoScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Gestión de Pedidos", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Módulo demostrativo para el futuro control de órdenes y despachos.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
