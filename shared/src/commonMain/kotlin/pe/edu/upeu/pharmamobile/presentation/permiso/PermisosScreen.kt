package pe.edu.upeu.pharmamobile.presentation.permiso

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobile.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobile.domain.permiso.Permiso

@Composable
fun PermisosScreen(
    state: PermisosUiState,
    onSolicitar: (Permiso) -> Unit,
    onAbrirAjustes: () -> Unit,
    onActualizar: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Permisos del dispositivo", style = MaterialTheme.typography.headlineSmall)
        Text("Consulta el estado y solicita solo los permisos necesarios para la función.")
        Permiso.entries.forEach { permiso ->
            val estado = state.estados[permiso] ?: EstadoPermiso.Denegado
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(permiso.etiqueta(), style = MaterialTheme.typography.titleMedium)
                    Text(estado.etiqueta())
                    if (estado == EstadoPermiso.DenegadoParaSiempre) {
                        Text("El sistema ya no muestra la solicitud. Habilítalo desde Ajustes.", color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = onAbrirAjustes) { Text("ABRIR AJUSTES") }
                    } else if (estado != EstadoPermiso.Concedido && estado != EstadoPermiso.NoDisponible) {
                        Button(onClick = { onSolicitar(permiso) }, enabled = !state.cargando) { Text("SOLICITAR") }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onActualizar, enabled = !state.cargando) { Text("ACTUALIZAR ESTADOS") }
        }
    }
}

private fun Permiso.etiqueta(): String = when (this) {
    Permiso.CAMARA -> "Cámara"
    Permiso.UBICACION -> "Ubicación mientras se usa la app"
    Permiso.NOTIFICACIONES -> "Notificaciones"
}

private fun EstadoPermiso.etiqueta(): String = when (this) {
    EstadoPermiso.Concedido -> "Concedido"
    EstadoPermiso.Denegado -> "Denegado; se puede volver a solicitar"
    EstadoPermiso.DenegadoParaSiempre -> "Denegado permanentemente"
    EstadoPermiso.NoDisponible -> "Gestión nativa no disponible en esta plataforma"
}
