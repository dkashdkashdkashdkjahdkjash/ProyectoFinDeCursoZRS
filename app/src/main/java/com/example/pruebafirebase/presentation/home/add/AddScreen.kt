package com.example.pruebafirebase.presentation.home.add

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.pruebafirebase.model.CosasQueVender
import com.example.pruebafirebase.ui.utils.methods.uriToBase64
import com.google.firebase.firestore.FirebaseFirestore


//Pantalla añadir item
@Composable
fun AddScreen() {
    var nombre by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var descuento by remember { mutableStateOf("") }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> imagenUri = uri }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Registro de Producto",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        //Selector de imagene
        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.CenterHorizontally)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (imagenUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imagenUri),
                    contentDescription = "Imagen seleccionada",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Seleccionar imagen",
                    modifier = Modifier.size(60.dp)
                )
            }
        }

        //Selector de nombre
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del producto - obligatorio") },
            modifier = Modifier.fillMaxWidth()
        )

        //Selector de cantidad
        OutlinedTextField(
            value = cantidad,
            onValueChange = {
                val soloDigitos = it.filter { c -> c.isDigit() }
                cantidad = when {
                    soloDigitos.isEmpty() -> "0"
                    soloDigitos.toInt() < 0 -> "0"
                    else -> soloDigitos
                }
            },
            label = { Text("Cantidad - obligatorio") },
            modifier = Modifier.fillMaxWidth()
        )

        //Selector de precio
        OutlinedTextField(
            value = precio,
            onValueChange = { input ->
                val limpio = input.filter { it.isDigit() || it == '.' }
                if (limpio.count { it == '.' } <= 1) {
                    precio = limpio
                }
                val valor = precio.toDoubleOrNull() ?: 0.0
                if (valor < 0.01 && precio.isNotEmpty()) {
                    precio = "0.01"
                }
            },
            label = { Text("Precio - obligatorio (decimales)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        //Selector de descuento
        OutlinedTextField(
            value = descuento,
            onValueChange = { descuento = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Descuento (%) – opcional") },
            modifier = Modifier.fillMaxWidth()
        )

        //Boton para crear objeto
        Button(
            onClick = {
                if (imagenUri == null) {
                    Toast.makeText(context, "Selecciona una imagen", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (nombre.isBlank() || cantidad.isBlank() || precio.isBlank()) {
                    Toast.makeText(
                        context,
                        "Completa todos los campos obligatorios",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }
                val base64Image = uriToBase64(context, imagenUri!!)

                //Llamamos a la funcion para crear el objeto
                createItem(
                    db = FirebaseFirestore.getInstance(),
                    nombre = nombre,
                    imagen = base64Image,
                    cantidad = (cantidad.toIntOrNull() ?: 0),
                    precio = (precio.toDoubleOrNull() ?: 0.0),
                    descuento = (descuento.toDoubleOrNull() ?: 0.0)
                ) {
                    // ⭐ Resetear campos al guardar
                    nombre = ""
                    cantidad = ""
                    precio = ""
                    descuento = ""
                    imagenUri = null

                    Toast.makeText(context, "Producto creado con éxito", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar")
        }
    }
}


//Funcion para crear el objeto y subirlo a la base de datos
fun createItem(
    db: FirebaseFirestore,
    nombre: String,
    imagen: String,
    cantidad: Int,
    precio: Double,
    descuento: Double,
    onSuccess: () -> Unit
) {
    val cosaQueVender = CosasQueVender(
        nombre = nombre,
        imagen = imagen,
        cantidad = cantidad,
        precio = precio,
        descuentoPorcentaje = descuento
    )
    //Subir el objeto a la coleccion Bienes
    db.collection("Bienes")
        .add(cosaQueVender)
        .addOnSuccessListener { onSuccess() }
}