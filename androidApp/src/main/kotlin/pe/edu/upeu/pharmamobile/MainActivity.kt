package pe.edu.upeu.pharmamobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import org.koin.core.context.GlobalContext
import pe.edu.upeu.pharmamobile.platform.permiso.GestorPermisosAndroid
import pe.edu.upeu.pharmamobile.platform.permiso.PuentePermisosAndroid

class MainActivity : ComponentActivity(), PuentePermisosAndroid {
    private var permisoPendiente: CancellableContinuation<Boolean>? = null
    private val lanzadorPermiso = registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        permisoPendiente?.let { espera ->
            permisoPendiente = null
            if (espera.isActive) espera.resume(concedido)
        }
    }
    private val gestorPermisos by lazy { GlobalContext.get().get<GestorPermisosAndroid>() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gestorPermisos.conectar(this)
        enableEdgeToEdge()

        setContent {
            App()
        }
    }

    override suspend fun solicitarPermiso(nombre: String): Boolean = suspendCancellableCoroutine { espera ->
        if (permisoPendiente != null) {
            espera.resume(false)
        } else {
            permisoPendiente = espera
            espera.invokeOnCancellation { if (permisoPendiente === espera) permisoPendiente = null }
            lanzadorPermiso.launch(nombre)
        }
    }

    override fun debeExplicarPermiso(nombre: String): Boolean =
        ActivityCompat.shouldShowRequestPermissionRationale(this, nombre)

    override fun onDestroy() {
        gestorPermisos.desconectar(this)
        super.onDestroy()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
