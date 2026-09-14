package pe.edu.upeu.pharmamobile.presentation.cliente

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobile.presentation.components.EstadoVacio
import pe.edu.upeu.pharmamobile.presentation.components.MensajeExito
import pe.edu.upeu.pharmamobile.presentation.components.ValidatedTextField

@Composable
fun ClienteScreen(
    state: ClienteUiState,
    onNombreChange: (String) -> Unit,
    onCorreoChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit,
    onRegistrar: () -> Unit,
    onReintentar: () -> Unit
) {
    val formulario = state.formulario
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("PHARMAMOBIL", style = MaterialTheme.typography.headlineMedium)
        Text("Registro de Cliente", style = MaterialTheme.typography.titleLarge)
        ValidatedTextField(formulario.nombre, onNombreChange, "Nombre", formulario.errorNombre)
        ValidatedTextField(formulario.correo, onCorreoChange, "Correo", formulario.errorCorreo, KeyboardType.Email)
        ValidatedTextField(formulario.telefono, onTelefonoChange, "Teléfono (opcional)", formulario.errorTelefono, KeyboardType.Phone)
        Button(onClick = onRegistrar, enabled = !state.guardando, modifier = Modifier.fillMaxWidth()) {
            Text(if (state.guardando) "REGISTRANDO..." else "REGISTRAR")
        }
        state.mensaje?.let { MensajeExito(it) }
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        Text("Clientes registrados", style = MaterialTheme.typography.titleLarge)
        when (val fase = state.fase) {
            ClienteFase.Cargando -> Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) { CircularProgressIndicator() }
            ClienteFase.SinClientes -> EstadoVacio("No hay clientes registrados")
            is ClienteFase.Error -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(fase.mensaje, color = MaterialTheme.colorScheme.error)
                Button(onClick = onReintentar) { Text("REINTENTAR") }
            }
            is ClienteFase.ConClientes -> fase.clientes.forEach { cliente ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(cliente.nombre, style = MaterialTheme.typography.titleMedium)
                    Text(cliente.correo)
                    Text(cliente.telefono, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                HorizontalDivider()
            }
        }
    }
}
