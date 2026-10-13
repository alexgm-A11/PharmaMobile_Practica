package pe.edu.upeu.pharmamobile.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Notifica un 401 protegido sin acoplar Ktor a la capa de presentación. */
class EventosSesion {
    private val _expiraciones = MutableStateFlow(0)
    val expiraciones = _expiraciones.asStateFlow()

    fun expirar() { _expiraciones.value += 1 }
}
