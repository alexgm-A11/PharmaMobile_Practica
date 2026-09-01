package pe.edu.upeu.pharmamobile.presentation.cliente

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobile.domain.model.Cliente

@Composable
fun ClienteScreen() {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("PHARMAMOBIL", style = MaterialTheme.typography.headlineMedium)
        Text("Registro de Cliente", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it.filter(Char::isDigit).take(9) },
            label = { Text("Teléfono (opcional)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                mensaje = when (val resultado = registrarCliente(nombre, correo, telefono)) {
                    is RegistroClienteResultado.Exito ->
                        "Cliente registrado correctamente: ${resultado.cliente.nombre}"
                    is RegistroClienteResultado.Error -> resultado.mensaje
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("REGISTRAR")
        }

        Text(
            text = mensaje,
            color = if (mensaje.startsWith("Cliente registrado")) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            }
        )
    }
}

internal sealed interface RegistroClienteResultado {
    data class Exito(val cliente: Cliente) : RegistroClienteResultado
    data class Error(val mensaje: String) : RegistroClienteResultado
}

internal fun registrarCliente(
    nombre: String,
    correo: String,
    telefono: String
): RegistroClienteResultado {
    val nombreLimpio = nombre.trim()
    if (nombreLimpio.isEmpty()) {
        return RegistroClienteResultado.Error("Nombre obligatorio")
    }
    if (nombreLimpio.length < 3) {
        return RegistroClienteResultado.Error("El nombre debe tener al menos 3 caracteres")
    }
    if (!nombreLimpio.any(Char::isLetter) || nombreLimpio.any(Char::isDigit)) {
        return RegistroClienteResultado.Error("Ingrese un nombre válido")
    }

    val correoLimpio = correo.trim().lowercase()
    if (correoLimpio.isEmpty()) {
        return RegistroClienteResultado.Error("Correo obligatorio")
    }
    val correoValido = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    if (!correoValido.matches(correoLimpio)) {
        return RegistroClienteResultado.Error("Ingrese un correo válido")
    }

    val telefonoLimpio = telefono.trim()
    if (telefonoLimpio.isNotEmpty() &&
        (telefonoLimpio.length != 9 || !telefonoLimpio.all(Char::isDigit))
    ) {
        return RegistroClienteResultado.Error("El teléfono debe tener 9 dígitos")
    }

    return RegistroClienteResultado.Exito(
        Cliente(
            id = 0L,
            nombre = nombreLimpio,
            correo = correoLimpio,
            telefono = telefonoLimpio.ifEmpty { null }
        )
    )
}
