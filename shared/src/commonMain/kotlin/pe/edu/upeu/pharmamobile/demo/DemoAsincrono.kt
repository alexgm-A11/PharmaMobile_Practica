package pe.edu.upeu.pharmamobile.demo

import pe.edu.upeu.pharmamobile.data.repository.ProductoRepositorioEnMemoria
import pe.edu.upeu.pharmamobile.domain.model.Producto


suspend fun probarOperacionesAsincronas() {
    val repositorio = ProductoRepositorioEnMemoria()

    // 1) Función suspend: retorna un único valor
    println("== suspend fun listar() ==")
    val productos = repositorio.listar()
    println("Productos obtenidos: $productos")

    println("== suspend fun registrar() ==")
    val registrado = repositorio.registrar(
        Producto(0, "Loratadina", 10.0, 5)
    )
    println("Producto registrado con id asignado por repositorio: $registrado")
}
