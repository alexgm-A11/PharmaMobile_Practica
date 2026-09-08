package pe.edu.upeu.pharmamobile.presentation.producto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ProductoScreen(
    state: ProductoUiState,
    onNombreChange: (String) -> Unit,
    onPrecioChange: (String) -> Unit,
    onStockChange: (String) -> Unit,
    onRegistrar: () -> Unit,
    onReintentar: () -> Unit
) {
    val formulario = state.formulario
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("PHARMAMOBIL", style = MaterialTheme.typography.headlineMedium)
        Text("Registro de Producto", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = formulario.nombre,
            onValueChange = onNombreChange,
            label = { Text("Nombre del producto") },
            isError = formulario.errorNombre != null,
            supportingText = formulario.errorNombre?.let { mensaje -> { Text(mensaje) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = formulario.precio,
            onValueChange = onPrecioChange,
            label = { Text("Precio") },
            isError = formulario.errorPrecio != null,
            supportingText = formulario.errorPrecio?.let { mensaje -> { Text(mensaje) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = formulario.stock,
            onValueChange = onStockChange,
            label = { Text("Stock") },
            isError = formulario.errorStock != null,
            supportingText = formulario.errorStock?.let { mensaje -> { Text(mensaje) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = onRegistrar, enabled = !state.guardando, modifier = Modifier.fillMaxWidth()) {
            Text(if (state.guardando) "REGISTRANDO..." else "REGISTRAR")
        }
        state.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        Text("Inventario", style = MaterialTheme.typography.titleLarge)
        when (val fase = state.fase) {
            ProductoFase.Cargando -> Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) { CircularProgressIndicator() }
            ProductoFase.SinProductos -> Text("No hay productos registrados")
            is ProductoFase.Error -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(fase.mensaje, color = MaterialTheme.colorScheme.error)
                Button(onClick = onReintentar) { Text("REINTENTAR") }
            }
            is ProductoFase.ConProductos -> fase.productos.forEach { producto ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(producto.nombre, style = MaterialTheme.typography.titleMedium)
                        Text("S/ ${producto.precio} · Stock: ${producto.stock}")
                    }
                    if (producto.requiereReposicion) {
                        Text("REPONER", color = MaterialTheme.colorScheme.error)
                    }
                }
                HorizontalDivider()
            }
        }
    }
}
