package pe.edu.upeu.pharmamobile.presentation.catalogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp

@Composable
fun CatalogoScreen(
    state: CatalogoUiState,
    onIdChange: (String) -> Unit,
    onBuscar: () -> Unit,
    onReintentar: () -> Unit,
    onProbarTimeout: () -> Unit,
    onProbarJson: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Catálogo REST", style = MaterialTheme.typography.headlineMedium)
        Text("Productos de la API de práctica. El inventario local permanece separado.")
        Button(onClick = onReintentar) { Text("ACTUALIZAR") }
        when (val fase = state.fase) {
            CatalogoFase.Cargando -> CircularProgressIndicator()
            is CatalogoFase.Error -> Text(fase.mensaje, color = MaterialTheme.colorScheme.error)
            is CatalogoFase.Lista -> {
                if (fase.productos.isEmpty()) Text("No hay productos remotos")
                fase.productos.forEach { producto ->
                    Text("${producto.nombre} · $ ${producto.precio}", style = MaterialTheme.typography.titleMedium)
                    Text("ID ${producto.id} · ${producto.categoria ?: "Sin categoría"}")
                    HorizontalDivider()
                }
            }
        }
        OutlinedTextField(
            value = state.idTexto,
            onValueChange = onIdChange,
            label = { Text("Buscar producto por ID") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = onBuscar, modifier = Modifier.fillMaxWidth()) { Text("BUSCAR ID") }
        state.mensajeDetalle?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        state.detalle?.let {
            Text(it.nombre, style = MaterialTheme.typography.titleLarge)
            Text("$ ${it.precio} · ${it.categoria ?: "Sin categoría"}")
            Text(it.descripcion)
        }
        HorizontalDivider()
        Text("Pruebas de conexión S07", style = MaterialTheme.typography.titleMedium)
        Button(onClick = onProbarTimeout) { Text("PROBAR TIMEOUT 1 MS") }
        Button(onClick = onProbarJson) { Text("PROBAR CAMPO JSON DESCONOCIDO") }
        state.mensajeDiagnostico?.let { Text(it) }
    }
}
