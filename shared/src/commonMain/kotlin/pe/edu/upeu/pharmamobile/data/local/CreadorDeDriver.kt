package pe.edu.upeu.pharmamobile.data.local

import app.cash.sqldelight.db.SqlDriver

/** Abstracción para abrir la base común usando el motor nativo de cada plataforma. */
interface CreadorDeDriver {
    fun crear(): SqlDriver
}
