package pe.edu.upeu.pharmamobile.di

import org.koin.dsl.module
import kotlin.experimental.ExperimentalNativeApi
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguroIos
import pe.edu.upeu.pharmamobile.data.local.CreadorDeDriver
import pe.edu.upeu.pharmamobile.data.local.CreadorDeDriverIos
import pe.edu.upeu.pharmamobile.data.remote.ConfiguracionPharmaSoft
import pe.edu.upeu.pharmamobile.domain.platform.Compartidor
import pe.edu.upeu.pharmamobile.platform.CompartidorIos
import pe.edu.upeu.pharmamobile.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobile.domain.permiso.GestorPermisosNoDisponible
import pe.edu.upeu.pharmamobile.domain.port.Telemetria
import pe.edu.upeu.pharmamobile.domain.port.TelemetriaInactiva

@OptIn(ExperimentalNativeApi::class)
actual val platformModule = module {
    single<Compartidor> { CompartidorIos() }
    single<GestorPermisos> { GestorPermisosNoDisponible() }
    single<Telemetria> { TelemetriaInactiva() }
    single<AlmacenSeguro> { AlmacenSeguroIos() }
    single<CreadorDeDriver> { CreadorDeDriverIos() }
    single { ConfiguracionPharmaSoft(depuracion = kotlin.native.Platform.isDebugBinary) }
}
