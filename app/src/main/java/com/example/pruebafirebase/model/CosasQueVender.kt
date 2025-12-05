package com.example.pruebafirebase.model

data class CosasQueVender(
    val id: String? = null,
    val nombre: String? = null,
    val imagen: String? = null,
    val cantidad: Int = 0,
    val precio: Double? = 0.0,
    val descuentoPorcentaje: Double = 0.0
)

//Funcion para ver si tiene Stock
fun CosasQueVender.haveStock(): Boolean {
    return this.cantidad > 0
}

//Funcion para ver si tiene descuento
fun CosasQueVender.haveDiscount():Boolean{
    return this.descuentoPorcentaje > 0.0
}

//Funcion para ver el precio con descuento
fun CosasQueVender.discountedPrice(): Double {
    val price = this.precio ?: 0.0
    val discount = this.descuentoPorcentaje / 100
    return price - (price * discount)
}
