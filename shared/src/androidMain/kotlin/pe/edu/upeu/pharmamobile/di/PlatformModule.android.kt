package pe.edu.upeu.pharmamobile.di

import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext
import pe.edu.upeu.pharmamobile.domain.platform.Compartidor
import pe.edu.upeu.pharmamobile.platform.CompartidorAndroid

actual val platformModule = module {
    single<Compartidor> { CompartidorAndroid(androidContext()) }
}
