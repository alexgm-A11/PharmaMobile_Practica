package pe.edu.upeu.pharmamobile.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

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

    private companion object { const val CLAVE = "jwt" }
}
