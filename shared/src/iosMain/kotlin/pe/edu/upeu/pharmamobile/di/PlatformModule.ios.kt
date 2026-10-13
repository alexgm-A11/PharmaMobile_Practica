package pe.edu.upeu.pharmamobile.di

import org.koin.dsl.module
import kotlin.experimental.ExperimentalNativeApi
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguroIos
import pe.edu.upeu.pharmamobile.data.remote.ConfiguracionPharmaSoft
import pe.edu.upeu.pharmamobile.domain.platform.Compartidor
import pe.edu.upeu.pharmamobile.platform.CompartidorIos

@OptIn(ExperimentalNativeApi::class)
actual val platformModule = module {
    single<Compartidor> { CompartidorIos() }
    single<AlmacenSeguro> { AlmacenSeguroIos() }
    single { ConfiguracionPharmaSoft(depuracion = kotlin.native.Platform.isDebugBinary) }
}
