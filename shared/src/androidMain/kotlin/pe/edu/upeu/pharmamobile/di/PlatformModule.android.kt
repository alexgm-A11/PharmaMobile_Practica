package pe.edu.upeu.pharmamobile.di

import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext
import android.content.pm.ApplicationInfo
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguroAndroid
import pe.edu.upeu.pharmamobile.data.remote.ConfiguracionPharmaSoft
import pe.edu.upeu.pharmamobile.domain.platform.Compartidor
import pe.edu.upeu.pharmamobile.platform.CompartidorAndroid

actual val platformModule = module {
    single<Compartidor> { CompartidorAndroid(androidContext()) }
    single<AlmacenSeguro> { AlmacenSeguroAndroid(androidContext()) }
    single {
        ConfiguracionPharmaSoft(
            depuracion = androidContext().applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        )
    }
}
