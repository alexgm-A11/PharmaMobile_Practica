package pe.edu.upeu.pharmamobile.domain.model

data class DetallePedido(
    val id: Long,
    val producto: Producto,
    val cantidad: Int
){init {
 require(value = cantidad > 0){
     "la cantidad debe ser mayor a 0"
 }
}
    fun subtotal(): Double{
        return producto.precio*cantidad
    }
}