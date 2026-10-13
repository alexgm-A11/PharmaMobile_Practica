package pe.edu.upeu.pharmamobile.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pe.edu.upeu.pharmamobile.data.repository.ProductoRepositorioEnMemoria
import pe.edu.upeu.pharmamobile.data.repository.ProductoCatalogoRepositorioRest
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.data.remote.DiagnosticoConexionApi
import pe.edu.upeu.pharmamobile.data.remote.crearClienteProductos
import org.koin.core.qualifier.named
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobile.data.local.EventosSesion
import pe.edu.upeu.pharmamobile.data.remote.ConfiguracionPharmaSoft
import pe.edu.upeu.pharmamobile.data.remote.crearClientePharmaSoft
import pe.edu.upeu.pharmamobile.data.repository.AuthRepositorioRest
import pe.edu.upeu.pharmamobile.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobile.domain.usecase.CerrarSesionUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.IniciarSesionUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.RecuperarSesionUseCase
import pe.edu.upeu.pharmamobile.presentation.sesion.SesionViewModel
import pe.edu.upeu.pharmamobile.data.repository.PharmaSoftRepositorioRest
import pe.edu.upeu.pharmamobile.domain.repository.PharmaSoftRepository
import pe.edu.upeu.pharmamobile.domain.usecase.GestionPharmaSoftUseCase
import pe.edu.upeu.pharmamobile.presentation.pharmasoft.ProductosServidorViewModel
import pe.edu.upeu.pharmamobile.presentation.pharmasoft.ClientesServidorViewModel
import pe.edu.upeu.pharmamobile.data.repository.ClienteRepositorioEnMemoria
import pe.edu.upeu.pharmamobile.domain.repository.ClienteRepository
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.domain.repository.ProductoCatalogoRepository
import pe.edu.upeu.pharmamobile.domain.usecase.ListarClientesUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarClienteUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobile.presentation.cliente.ClienteViewModel
import pe.edu.upeu.pharmamobile.presentation.catalogo.CatalogoViewModel
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoViewModel
import pe.edu.upeu.pharmamobile.presentation.detalle.DetalleProductoViewModel

val dataModule = module {
    single { EventosSesion() }
    single(named("pharmasoft")) {
        crearClientePharmaSoft(get<AlmacenSeguro>(), get<EventosSesion>(), get<ConfiguracionPharmaSoft>().depuracion)
    }
    single<AuthRepository> { AuthRepositorioRest(get(named("pharmasoft")), get()) }
    single<PharmaSoftRepository> { PharmaSoftRepositorioRest(get(named("pharmasoft"))) }
    single { crearClienteProductos() }
    single { ProductoApi(get()) }
    single { DiagnosticoConexionApi(get()) }
    single<ProductoCatalogoRepository> { ProductoCatalogoRepositorioRest(get()) }
    single<ProductoRepository> { ProductoRepositorioEnMemoria() }
    single<ClienteRepository> { ClienteRepositorioEnMemoria() }
}
val domainModule = module {
    factory { IniciarSesionUseCase(get()) }
    factory { RecuperarSesionUseCase(get()) }
    factory { CerrarSesionUseCase(get()) }
    factory { GestionPharmaSoftUseCase(get()) }
    factory { RegistrarProductoUseCase(get()) }
    factory { ListarProductosUseCase(get()) }
    factory { RegistrarClienteUseCase(get()) }
    factory { ListarClientesUseCase(get()) }
}
val presentationModule = module {
    viewModel { SesionViewModel(get(), get(), get(), get()) }
    viewModel { ProductosServidorViewModel(get()) }
    viewModel { ClientesServidorViewModel(get()) }
    viewModel { CatalogoViewModel(get(), get()) }
    viewModel { ProductoViewModel(get(), get()) }
    viewModel { DetalleProductoViewModel(get(), get()) }
    viewModel { ClienteViewModel(get(), get()) }
}

expect val platformModule: Module

fun initKoin(config: KoinApplication.() -> Unit = {}): KoinApplication = startKoin {
    config()
    modules(dataModule, domainModule, presentationModule, platformModule)
}

fun initKoinIos(): KoinApplication = initKoin()
