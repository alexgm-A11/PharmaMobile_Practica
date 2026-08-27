package pe.edu.upeu.pharmamobile.presentation.producto

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
import pe.edu.upeu.pharmamobile.domain.model.Producto

@Composable
fun ProductoScreen() {
    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "PHARMAMOBIL", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Registro de Producto", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del producto") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = precio,
            onValueChange = { precio = it },
            label = { Text("Precio") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = stock,
            onValueChange = { stock = it },
            label = { Text("Stock") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val resultado = registrarProducto(nombre, precio, stock)
                mensaje = when (resultado) {
                    is RegistroProductoResultado.Exito ->
                        "Producto registrado correctamente: ${resultado.producto.nombre}"
                    is RegistroProductoResultado.Error -> resultado.mensaje
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("REGISTRAR")
        }

        Text(
            text = mensaje,
            color = if (mensaje.startsWith("Producto registrado")) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            }
        )
    }
}

internal sealed interface RegistroProductoResultado {
    data class Exito(val producto: Producto) : RegistroProductoResultado
    data class Error(val mensaje: String) : RegistroProductoResultado
}

internal fun registrarProducto(
    nombre: String,
    precioTexto: String,
    stockTexto: String
): RegistroProductoResultado {
    val nombreLimpio = nombre.trim()
    if (nombreLimpio.isEmpty()) {
        return RegistroProductoResultado.Error("Ingrese nombre del producto")
    }

    val precio = precioTexto.trim().replace(',', '.').toDoubleOrNull()
    if (precio == null || precio <= 0.0) {
        return RegistroProductoResultado.Error("Ingrese precio válido")
    }

    val stock = stockTexto.trim().toIntOrNull()
        ?: return RegistroProductoResultado.Error("Ingrese stock válido")
    if (stock < 0) {
        return RegistroProductoResultado.Error("El stock no puede ser negativo")
    }

    return RegistroProductoResultado.Exito(
        Producto(id = 0L, nombre = nombreLimpio, precio = precio, stock = stock)
    )
}
