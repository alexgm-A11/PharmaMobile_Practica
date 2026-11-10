package pe.edu.upeu.pharmamobile.data.firebase

import android.content.Context
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics
import pe.edu.upeu.pharmamobile.domain.port.Telemetria

/** El SDK queda detrás del contrato de dominio y no recibe datos personales ni credenciales. */
class TelemetriaFirebaseAndroid(context: Context) : Telemetria {
    private val habilitada = context.resources.getIdentifier("google_app_id", "string", context.packageName) != 0

    override fun registrarError(error: Throwable, contexto: Map<String, String>) {
        if (!habilitada) return
        runCatching {
            Firebase.crashlytics.setCustomKeys(contexto.seguro())
            Firebase.crashlytics.recordException(error)
        }
    }

    override fun registrarEvento(nombre: String, datos: Map<String, String>) {
        if (!habilitada) return
        runCatching {
            Firebase.crashlytics.setCustomKeys(datos.seguro())
            Firebase.crashlytics.log("evento=${nombre.take(80)}")
        }
    }

    private fun Map<String, String>.seguro(): Map<String, Any> = entries
        .asSequence()
        .filterNot { (clave, _) -> CAMPOS_SENSIBLES.any { clave.contains(it, ignoreCase = true) } }
        .take(64)
        .associate { (clave, valor) -> clave.take(64) to valor.replace('\n', ' ').replace('\r', ' ').take(256) }

    private companion object {
        val CAMPOS_SENSIBLES = listOf("token", "password", "clave", "email", "usuario", "nombre", "patient", "auth", "credential")
    }
}
