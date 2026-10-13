package pe.edu.upeu.pharmamobile.data.remote

data class ConfiguracionPharmaSoft(val depuracion: Boolean)

/** Android usa el puente del emulador; iOS usa el host del simulador. */
expect val pharmaSoftBaseUrl: String
