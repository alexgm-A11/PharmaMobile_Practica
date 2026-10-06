package pe.edu.upeu.pharmamobile.domain.platform

/** Contrato común: el dominio no conoce Context ni UIKit. */
interface Compartidor {
    fun compartir(texto: String)
}
