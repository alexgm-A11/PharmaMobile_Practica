package pe.edu.upeu.pharmamobile.platform

import pe.edu.upeu.pharmamobile.domain.platform.Compartidor
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

class CompartidorIos : Compartidor {
    override fun compartir(texto: String) {
        val controlador = UIActivityViewController(
            activityItems = listOf(texto),
            applicationActivities = null
        )
        val raiz = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return
        raiz.presentViewController(controlador, animated = true, completion = null)
    }
}
