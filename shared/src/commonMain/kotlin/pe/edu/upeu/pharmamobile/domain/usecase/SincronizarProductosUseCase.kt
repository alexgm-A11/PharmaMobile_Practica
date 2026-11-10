package pe.edu.upeu.pharmamobile.domain.usecase

import kotlinx.coroutines.delay
import pe.edu.upeu.pharmamobile.domain.model.ResumenSync
import pe.edu.upeu.pharmamobile.domain.model.ResultadoEnvio
import pe.edu.upeu.pharmamobile.domain.repository.SincronizacionProductosRepository

/** Reintenta con espera creciente y mantiene los detalles HTTP fuera del dominio. */
class SincronizarProductosUseCase(
    private val repository: SincronizacionProductosRepository
) {
    suspend operator fun invoke(): ResumenSync {
        var enviadas = 0
        var fallidas = 0

        repository.pendientes(MAX_INTENTOS).forEach { fila ->
            var intentos = fila.intentos
            var terminar = false
            while (!terminar && intentos < MAX_INTENTOS) {
                when (val resultado = repository.enviar(fila)) {
                    is ResultadoEnvio.Exito -> {
                        repository.confirmar(fila.id, resultado.idServidor)
                        enviadas++
                        terminar = true
                    }
                    is ResultadoEnvio.Permanente -> {
                        repository.marcarFallida(fila.id, resultado.motivo, MAX_INTENTOS)
                        fallidas++
                        terminar = true
                    }
                    is ResultadoEnvio.Transitorio -> {
                        if (!resultado.reintentoAutomaticoSeguro) {
                            repository.marcarFallida(fila.id, resultado.motivo, MAX_INTENTOS)
                            fallidas++
                            terminar = true
                        } else {
                            intentos = repository.sumarIntento(fila.id, resultado.motivo)
                            if (intentos >= MAX_INTENTOS) {
                                repository.marcarFallida(fila.id, resultado.motivo, MAX_INTENTOS)
                                fallidas++
                                terminar = true
                            } else {
                                delay(espera(intentos))
                            }
                        }
                    }
                }
            }
        }

        // Primero se suben los cambios; después se concilia la lista del servidor.
        repository.conciliarRemoto()
        return ResumenSync(
            enviadas = enviadas,
            fallidas = fallidas,
            pendientes = repository.pendientes(MAX_INTENTOS).size
        )
    }

    private fun espera(intentos: Int): Long = 1_000L shl (intentos - 1).coerceIn(0, 4)

    companion object {
        const val MAX_INTENTOS = 5
    }
}
