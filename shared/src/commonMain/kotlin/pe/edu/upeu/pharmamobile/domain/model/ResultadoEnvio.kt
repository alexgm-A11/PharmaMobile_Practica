package pe.edu.upeu.pharmamobile.domain.model

/** Resultado de subir una fila local, sin exponer códigos HTTP al dominio. */
sealed interface ResultadoEnvio {
    data class Exito(val idServidor: Long) : ResultadoEnvio

    /**
     * Solo se reintenta automáticamente si se sabe que el servidor no recibió la creación.
     * Las respuestas ambiguas requieren intervención para evitar duplicados.
     */
    data class Transitorio(
        val motivo: String,
        val reintentoAutomaticoSeguro: Boolean = false
    ) : ResultadoEnvio

    data class Permanente(val motivo: String) : ResultadoEnvio
}

data class ResumenSync(
    val enviadas: Int,
    val fallidas: Int,
    val pendientes: Int
)
