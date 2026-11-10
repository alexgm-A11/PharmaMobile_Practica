package pe.edu.upeu.pharmamobile.di

import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext
import android.content.pm.ApplicationInfo
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguroAndroid
import pe.edu.upeu.pharmamobile.data.local.CreadorDeDriver
import pe.edu.upeu.pharmamobile.data.local.CreadorDeDriverAndroid
import pe.edu.upeu.pharmamobile.data.remote.ConfiguracionPharmaSoft
import pe.edu.upeu.pharmamobile.domain.platform.Compartidor
import pe.edu.upeu.pharmamobile.platform.CompartidorAndroid
import pe.edu.upeu.pharmamobile.platform.permiso.GestorPermisosAndroid
import pe.edu.upeu.pharmamobile.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobile.domain.port.Telemetria
import pe.edu.upeu.pharmamobile.data.firebase.TelemetriaFirebaseAndroid

actual val platformModule = module {
    single<Compartidor> { CompartidorAndroid(androidContext()) }
    single { GestorPermisosAndroid(androidContext()) }
    single<GestorPermisos> { get<GestorPermisosAndroid>() }
    single<Telemetria> { TelemetriaFirebaseAndroid(androidContext()) }
    single<AlmacenSeguro> { AlmacenSeguroAndroid(androidContext()) }
    single<CreadorDeDriver> { CreadorDeDriverAndroid(androidContext()) }
    single {
        ConfiguracionPharmaSoft(
            depuracion = androidContext().applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        )
    }
}
