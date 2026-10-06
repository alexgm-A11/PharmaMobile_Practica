package pe.edu.upeu.pharmamobile.presentation.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DetalleProductoScreen(state: DetalleProductoUiState, onCompartir: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (state) {
            DetalleProductoUiState.Cargando -> CircularProgressIndicator()
            DetalleProductoUiState.NoEncontrado -> Text("Producto no encontrado")
            is DetalleProductoUiState.Error -> Text(state.mensaje, color = MaterialTheme.colorScheme.error)
            is DetalleProductoUiState.Contenido -> {
                Text(state.producto.nombre, style = MaterialTheme.typography.headlineMedium)
                Text("Precio: ${state.producto.precio}", style = MaterialTheme.typography.titleLarge)
                Text("Stock: ${state.producto.stock}")
                if (state.producto.requiereReposicion) {
                    Text("Requiere reposición", color = MaterialTheme.colorScheme.error)
                }
                Button(onClick = onCompartir, modifier = Modifier.fillMaxWidth()) {
                    Text("Compartir")
                }
            }
        }
    }
}
