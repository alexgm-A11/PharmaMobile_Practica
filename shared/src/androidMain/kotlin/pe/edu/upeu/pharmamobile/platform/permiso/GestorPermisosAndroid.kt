package pe.edu.upeu.pharmamobile.platform.permiso

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import pe.edu.upeu.pharmamobile.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobile.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobile.domain.permiso.Permiso

interface PuentePermisosAndroid {
    suspend fun solicitarPermiso(nombre: String): Boolean
    fun debeExplicarPermiso(nombre: String): Boolean
}

class GestorPermisosAndroid(private val contexto: Context) : GestorPermisos {
    private val preferencias = contexto.getSharedPreferences("estado_permisos", Context.MODE_PRIVATE)
    private var puente: PuentePermisosAndroid? = null

    fun conectar(puente: PuentePermisosAndroid) { this.puente = puente }
    fun desconectar(puente: PuentePermisosAndroid) { if (this.puente === puente) this.puente = null }

    override suspend fun estado(permiso: Permiso): EstadoPermiso {
        val nombre = permisoAndroid(permiso) ?: return EstadoPermiso.Concedido
        if (contexto.checkSelfPermission(nombre) == PackageManager.PERMISSION_GRANTED) {
            return EstadoPermiso.Concedido
        }
        val yaSolicitado = preferencias.getBoolean("pedido_$nombre", false)
        val mostrarRazon = puente?.debeExplicarPermiso(nombre) == true
        return if (!yaSolicitado || mostrarRazon) EstadoPermiso.Denegado
        else EstadoPermiso.DenegadoParaSiempre
    }

    override suspend fun solicitar(permiso: Permiso): EstadoPermiso {
        val nombre = permisoAndroid(permiso) ?: return EstadoPermiso.Concedido
        if (contexto.checkSelfPermission(nombre) == PackageManager.PERMISSION_GRANTED) {
            return EstadoPermiso.Concedido
        }
        preferencias.edit().putBoolean("pedido_$nombre", true).apply()
        val concedido = puente?.solicitarPermiso(nombre) ?: false
        return if (concedido) EstadoPermiso.Concedido else estado(permiso)
    }

    override fun abrirAjustes() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${contexto.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        contexto.startActivity(intent)
    }

    private fun permisoAndroid(permiso: Permiso): String? = when (permiso) {
        Permiso.CAMARA -> Manifest.permission.CAMERA
        Permiso.UBICACION -> Manifest.permission.ACCESS_FINE_LOCATION
        Permiso.NOTIFICACIONES -> if (Build.VERSION.SDK_INT >= 33) Manifest.permission.POST_NOTIFICATIONS else null
    }
}
