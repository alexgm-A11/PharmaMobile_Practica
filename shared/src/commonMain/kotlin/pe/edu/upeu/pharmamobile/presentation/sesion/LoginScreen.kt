package pe.edu.upeu.pharmamobile.presentation.sesion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    state: SesionUiState,
    onUsuarioChange: (String) -> Unit,
    onClaveChange: (String) -> Unit,
    onIniciar: () -> Unit,
    onReintentar: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("PharmaMobile", style = MaterialTheme.typography.headlineMedium)
        Text("Iniciar sesión en PharmaSoft", style = MaterialTheme.typography.titleLarge)
        if (state.cargando) {
            CircularProgressIndicator()
        } else {
            OutlinedTextField(
                value = state.usuario,
                onValueChange = onUsuarioChange,
                label = { Text("Usuario") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.clave,
                onValueChange = onClaveChange,
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = onIniciar, modifier = Modifier.fillMaxWidth()) { Text("INGRESAR") }
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                if (it.contains("conectar", ignoreCase = true)) {
                    Button(onClick = onReintentar) { Text("REINTENTAR SESIÓN GUARDADA") }
                }
            }
        }
    }
}
