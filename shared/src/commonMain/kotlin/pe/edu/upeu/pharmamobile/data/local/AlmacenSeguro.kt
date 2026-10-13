package pe.edu.upeu.pharmamobile.data.local

/** El token solo se persiste mediante el almacén protegido del sistema operativo. */
interface AlmacenSeguro {
    fun guardarToken(token: String)
    fun leerToken(): String?
    fun borrarToken()
}
