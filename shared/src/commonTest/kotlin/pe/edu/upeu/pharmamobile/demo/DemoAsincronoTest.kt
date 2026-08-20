package pe.edu.upeu.pharmamobile.demo

import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * Ejecuta probarOperacionesAsincronas() dentro de runTest (kotlinx-coroutines-test)
 * para generar por consola la evidencia de ejecución que pide el Paso 21 de la
 * guía autónoma: emisión y recolección correcta de suspend fun y Flow.
 */
class DemoAsincronoTest {

    @Test
    fun ejecutarDemoAsincrona() = runTest {
        probarOperacionesAsincronas()
    }
}
