package pe.edu.upeu.pharmamobile.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pe.edu.upeu.pharmamobile.data.repository.ProductoRepositorioEnMemoria
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoViewModel

val repositorioModule = module { single<ProductoRepository> { ProductoRepositorioEnMemoria() } }
val casoUsoModule = module { factory { RegistrarProductoUseCase(get()) } }
val presentacionModule = module { viewModel { ProductoViewModel(get(), get()) } }

expect val platformModule: Module

fun initKoin(config: KoinApplication.() -> Unit = {}): KoinApplication = startKoin {
    config()
    modules(repositorioModule, casoUsoModule, presentacionModule, platformModule)
}

fun initKoinIos(): KoinApplication = initKoin()
