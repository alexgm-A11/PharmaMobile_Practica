package pe.edu.upeu.pharmamobile.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/** Sigue la API pedida por la guía; se desactivó Auto Backup para esta app. */
@Suppress("DEPRECATION")
class AlmacenSeguroAndroid(contexto: Context) : AlmacenSeguro {
    private val llave = MasterKey.Builder(contexto)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    private val preferencias = EncryptedSharedPreferences.create(
        contexto,
        "sesion_segura",
        llave,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun guardarToken(token: String) {
        check(preferencias.edit().putString(CLAVE, token).commit())
    }

    override fun leerToken(): String? = preferencias.getString(CLAVE, null)

    override fun borrarToken() {
        check(preferencias.edit().remove(CLAVE).commit())
    }

    override fun guardarPerfilSesion(perfil: PerfilSesionOffline) {
        check(preferencias.edit().putString(CLAVE_PERFIL, Json.encodeToString(perfil)).commit())
    }

    override fun leerPerfilSesion(): PerfilSesionOffline? = preferencias.getString(CLAVE_PERFIL, null)
        ?.let { runCatching { Json.decodeFromString<PerfilSesionOffline>(it) }.getOrNull() }

    override fun borrarPerfilSesion() {
        check(preferencias.edit().remove(CLAVE_PERFIL).commit())
    }

    private companion object {
        const val CLAVE = "jwt"
        const val CLAVE_PERFIL = "perfil_sesion"
    }
}
