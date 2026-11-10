package pe.edu.upeu.pharmamobile.domain.port

/** Contrato de telemetría. Nunca se envían credenciales ni datos personales. */
interface Telemetria {
    fun registrarError(error: Throwable, contexto: Map<String, String> = emptyMap())
    fun registrarEvento(nombre: String, datos: Map<String, String> = emptyMap())
}

class TelemetriaInactiva : Telemetria {
    override fun registrarError(error: Throwable, contexto: Map<String, String>) = Unit
    override fun registrarEvento(nombre: String, datos: Map<String, String>) = Unit
}
