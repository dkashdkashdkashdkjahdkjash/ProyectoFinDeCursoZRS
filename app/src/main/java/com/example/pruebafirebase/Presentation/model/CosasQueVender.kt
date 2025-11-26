package com.example.pruebafirebase.Presentation.model

data class CosasQueVender(
    val Nombre: String? = null,
    val Imagen: String? = null,
    val Stock: Boolean = true,
    val Precio: Double? = 0.0,
    val Descuento: Boolean = false,
    val DescuentoPorcentaje: Double? = 0.0

)