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
    single { crearClienteProductos() }
    single { ProductoApi(get()) }
    single { DiagnosticoConexionApi(get()) }
    single<ProductoCatalogoRepository> { ProductoCatalogoRepositorioRest(get()) }
    single<ProductoRepository> { ProductoRepositorioEnMemoria() }
    single<ClienteRepository> { ClienteRepositorioEnMemoria() }
}
val domainModule = module {
    factory { RegistrarProductoUseCase(get()) }
    factory { ListarProductosUseCase(get()) }
    factory { RegistrarClienteUseCase(get()) }
    factory { ListarClientesUseCase(get()) }
}
val presentationModule = module {
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
