package pe.edu.upeu.pharmamobile.presentation.pharmasoft

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ProductosServidorScreen(
    state: ProductosServidorUiState,
    puedeEscribir: Boolean,
    onFormulario: ((FormularioProductoServidor) -> FormularioProductoServidor) -> Unit,
    onGuardar: () -> Unit,
    onEditar: (pe.edu.upeu.pharmamobile.domain.model.ProductoServidor) -> Unit,
    onLimpiar: () -> Unit,
    onEliminar: (Long) -> Unit,
    onRecargar: () -> Unit
) {
    var eliminarId by remember { mutableStateOf<Long?>(null) }
    eliminarId?.let { id ->
        ConfirmarEliminacion("producto", { eliminarId = null }, {
            eliminarId = null
            onEliminar(id)
        })
    }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Productos de PharmaSoft", style = MaterialTheme.typography.titleLarge)
        Text("Datos del servidor; requieren una sesión JWT activa.")
        if (state.cargando) CircularProgressIndicator()
        state.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        Button(onClick = onRecargar, enabled = !state.cargando) { Text("RECARGAR") }
        if (puedeEscribir) {
            HorizontalDivider()
            Text(if (state.formulario.id == null) "Registrar producto" else "Editar producto #${state.formulario.id}",
                style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(state.formulario.nombre, { onFormulario { f -> f.copy(nombre = it) } },
                label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(state.formulario.precio, { onFormulario { f -> f.copy(precio = it) } },
                label = { Text("Precio") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth())
            OutlinedTextField(state.formulario.stock, { onFormulario { f -> f.copy(stock = it) } },
                label = { Text("Stock") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth())
            OutlinedTextField(state.formulario.categoriaId, { onFormulario { f -> f.copy(categoriaId = it) } },
                label = { Text("ID de categoría") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Activo")
                Switch(state.formulario.estado, { onFormulario { f -> f.copy(estado = it) } })
            }
            state.categorias.forEach { categoria ->
                TextButton(onClick = { onFormulario { it.copy(categoriaId = categoria.id.toString()) } }) {
                    Text("${categoria.id}: ${categoria.nombre}")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onGuardar, enabled = !state.cargando) {
                    Text(if (state.formulario.id == null) "REGISTRAR" else "GUARDAR CAMBIOS")
                }
                if (state.formulario.id != null) TextButton(onClick = onLimpiar) { Text("CANCELAR") }
            }
        }
        HorizontalDivider()
        Text("Listado", style = MaterialTheme.typography.titleMedium)
        state.productos.forEach { item ->
            val producto = item.producto
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(producto.nombre, style = MaterialTheme.typography.titleSmall)
                Text("${item.precioFormateado} · Stock: ${producto.stock} · Categoría: ${producto.categoriaId}")
                if (!producto.estado) Text("Inactivo")
                if (puedeEscribir) Row {
                    TextButton(onClick = { onEditar(producto) }) { Text("EDITAR") }
                    TextButton(onClick = { eliminarId = producto.id }) { Text("ELIMINAR") }
                }
            }
            HorizontalDivider()
        }
    }
}

@Composable
fun ClientesServidorScreen(
    state: ClientesServidorUiState,
    puedeEscribir: Boolean,
    onFormulario: ((FormularioClienteServidor) -> FormularioClienteServidor) -> Unit,
    onGuardar: () -> Unit,
    onEditar: (pe.edu.upeu.pharmamobile.domain.model.ClienteServidor) -> Unit,
    onLimpiar: () -> Unit,
    onEliminar: (Long) -> Unit,
    onRecargar: () -> Unit
) {
    var eliminarId by remember { mutableStateOf<Long?>(null) }
    eliminarId?.let { id ->
        ConfirmarEliminacion("cliente", { eliminarId = null }, {
            eliminarId = null
            onEliminar(id)
        })
    }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Clientes de PharmaSoft", style = MaterialTheme.typography.titleLarge)
        Text("Datos del servidor; requieren una sesión JWT activa.")
        if (state.cargando) CircularProgressIndicator()
        state.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        Button(onClick = onRecargar, enabled = !state.cargando) { Text("RECARGAR") }
        if (puedeEscribir) {
            HorizontalDivider()
            Text(if (state.formulario.id == null) "Registrar cliente" else "Editar cliente #${state.formulario.id}",
                style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(state.formulario.dni, { onFormulario { f -> f.copy(dni = it) } },
                label = { Text("DNI (8 dígitos)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth())
            OutlinedTextField(state.formulario.nombres, { onFormulario { f -> f.copy(nombres = it) } },
                label = { Text("Nombres") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(state.formulario.apellidos, { onFormulario { f -> f.copy(apellidos = it) } },
                label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(state.formulario.email, { onFormulario { f -> f.copy(email = it) } },
                label = { Text("Correo") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth())
            OutlinedTextField(state.formulario.telefono, { onFormulario { f -> f.copy(telefono = it) } },
                label = { Text("Teléfono (opcional)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth())
            OutlinedTextField(state.formulario.direccion, { onFormulario { f -> f.copy(direccion = it) } },
                label = { Text("Dirección (opcional)") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Activo")
                Switch(state.formulario.estado, { onFormulario { f -> f.copy(estado = it) } })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onGuardar, enabled = !state.cargando) {
                    Text(if (state.formulario.id == null) "REGISTRAR" else "GUARDAR CAMBIOS")
                }
                if (state.formulario.id != null) TextButton(onClick = onLimpiar) { Text("CANCELAR") }
            }
        }
        HorizontalDivider()
        Text("Listado", style = MaterialTheme.typography.titleMedium)
        state.clientes.forEach { cliente ->
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${cliente.nombres} ${cliente.apellidos}", style = MaterialTheme.typography.titleSmall)
                Text("DNI: ${cliente.dni} · ${cliente.email}")
                if (!cliente.estado) Text("Inactivo")
                if (puedeEscribir) Row {
                    TextButton(onClick = { onEditar(cliente) }) { Text("EDITAR") }
                    TextButton(onClick = { eliminarId = cliente.id }) { Text("ELIMINAR") }
                }
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun ConfirmarEliminacion(nombre: String, onCancelar: () -> Unit, onConfirmar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Eliminar $nombre") },
        text = { Text("Esta operación se ejecutará en PharmaSoft y no se puede deshacer.") },
        confirmButton = { TextButton(onClick = onConfirmar) { Text("ELIMINAR") } },
        dismissButton = { TextButton(onClick = onCancelar) { Text("CANCELAR") } }
    )
}
