package com.example.pruebafirebase.ui.utils.methods

import android.annotation.SuppressLint
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.pruebafirebase.model.CosasQueVender
import com.example.pruebafirebase.model.discountedPrice
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// DetalleDialog del manager
@SuppressLint("DefaultLocale")
@Composable
fun DetalleDialogManager(
    item: CosasQueVender,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val uid = auth.currentUser?.uid
    val listaRef = uid?.let {
        db.collection("Usuarios").document(it)
            .collection("Lista").document(item.id!!)
    }
    var estaEnLista by remember { mutableStateOf(false) }
    var vistaExtendida by remember { mutableStateOf(0) }
    var nombreEdit by remember { mutableStateOf(item.nombre ?: "") }
    var precioEdit by remember { mutableStateOf(item.precio?.toString() ?: "") }
    var cantidadEdit by remember { mutableStateOf(item.cantidad.toString()) }
    var descuentoEdit by remember { mutableStateOf(item.descuentoPorcentaje.toString()) }
    var imagenNuevaUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        imagenNuevaUri = uri
    }
    LaunchedEffect(Unit) {
        estaEnLista = listaRef?.get()?.await()?.exists() == true
    }
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                item.nombre ?: "Sin nombre",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // La imagen
                val img: ImageBitmap? =
                    if (imagenNuevaUri == null)
                        decodeBase64ToImageBitmap(item.imagen ?: "")
                    else {
                        decodeBase64ToImageBitmap(uriToBase64(context, imagenNuevaUri!!))
                    }
                // Dependiendo del valor de vistaExtendida muestra cosas diferentes
                when (vistaExtendida) {
                    // Vista normal con las stats del item
                    0 -> Column {
                        Image(
                            bitmap = img ?: ImageBitmap(1, 1),
                            contentDescription = null,
                            modifier = Modifier
                                .size(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(vistaExtendida == 1) {
                                    launcher.launch("image/*")
                                },
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(16.dp))
                        if (item.descuentoPorcentaje > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${item.precio}€",
                                    color = Color.Red.copy(alpha = 0.6f),
                                    textDecoration = TextDecoration.LineThrough
                                )
                                Spacer(Modifier.width(10.dp))
                                Box(
                                    Modifier
                                        .background(Color.Red, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "-${item.descuentoPorcentaje.toInt()}%",
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(Modifier.height(6.dp))

                            Text(
                                text = "Precio: ${String.format("%.2f", item.discountedPrice())}€",
                                color = Color(0xFF2ECC71),
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                "Precio: ${item.precio}€",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Vista para modificar stats
                    1 -> Column {
                        Image(
                            bitmap = img ?: ImageBitmap(1, 1),
                            contentDescription = null,
                            modifier = Modifier
                                .size(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(vistaExtendida == 1) {
                                    launcher.launch("image/*")
                                },
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = nombreEdit,
                            onValueChange = { nombreEdit = it },
                            label = { Text("Nombre") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = precioEdit,
                            onValueChange = { input ->
                                val limpio = input.filter { it.isDigit() || it == '.' }
                                if (limpio.count { it == '.' } <= 1) {
                                    precioEdit = limpio
                                }
                                val valor = precioEdit.toDoubleOrNull() ?: 0.0
                                if (valor < 0.01 && precioEdit.isNotEmpty()) {
                                    precioEdit = "0.01"
                                }
                            },
                            label = { Text("Precio - obligatorio (decimales)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = cantidadEdit,
                            onValueChange = {
                                val soloDigitos = it.filter { c -> c.isDigit() }
                                cantidadEdit = when {
                                    soloDigitos.isEmpty() -> "0"
                                    soloDigitos.toInt() < 0 -> "0"
                                    else -> soloDigitos
                                }
                            },
                            label = { Text("Cantidad - obligatorio") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = descuentoEdit,
                            onValueChange = {
                                val soloDigitos = it.filter { c -> c.isDigit() }
                                descuentoEdit = when {
                                    soloDigitos.isEmpty() -> "0"
                                    else -> soloDigitos
                                }
                            },
                            label = { Text("Descuento - obligatorio") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(16.dp))
                        // GUARDAR
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {

                                val precio = precioEdit.toDoubleOrNull()
                                val cantidad = cantidadEdit.toIntOrNull()
                                val descuento = descuentoEdit.toDoubleOrNull()

                                if (precio == null || cantidad == null || descuento == null) {
                                    Toast.makeText(context, "Valores inválidos", Toast.LENGTH_SHORT)
                                        .show()
                                    return@Button
                                }

                                val imagenFinal = if (imagenNuevaUri != null)
                                    uriToBase64(context, imagenNuevaUri!!)
                                else item.imagen

                                db.collection("Bienes").document(item.id!!)
                                    .update(
                                        mapOf(
                                            "nombre" to nombreEdit,
                                            "precio" to precio,
                                            "cantidad" to cantidad,
                                            "descuentoPorcentaje" to descuento,
                                            "imagen" to imagenFinal
                                        )
                                    )
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Cambios guardados",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                        onDismiss()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            context,
                                            "Error al guardar",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }
                            }
                        ) { Text("Guardar cambios") }

                        Spacer(Modifier.height(12.dp))

                        // ELIMINAR
                        Button(
                            onClick = {
                                db.collection("Bienes").document(item.id!!)
                                    .delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Producto eliminado",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onDismiss()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            context,
                                            "Error eliminando",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Eliminar", color = Color.White)
                        }

                        return@AlertDialog
                    }

                    // Vista que muestra el QR generado con el id del item
                    2 -> Column {
                        val generatedQr = item.id?.let { generateQRCode(it) }
                        Image(
                            bitmap = generatedQr ?: ImageBitmap(1, 1),
                            contentDescription = null,
                            modifier = Modifier
                                .size(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(vistaExtendida == 1) {
                                    launcher.launch("image/*")
                                },
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = {}) {
                            Text(text = "Imprimir")
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Cantidad: ${item.cantidad} uds",
                    color = if (item.cantidad > 0) Color(0xFF2ECC71) else Color.Red,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Cambiar vista entre editar y normal
                Button(onClick = {
                    if (vistaExtendida != 1) {
                        vistaExtendida = 1
                    } else {
                        vistaExtendida = 0
                    }
                }) {
                    Text(if (vistaExtendida == 1) "❇️" else "✏️") // editar / volver
                }

                // Cambiar vista entre QR y normal
                Button(onClick = {
                    if (vistaExtendida != 2) {
                        vistaExtendida = 2
                    } else {
                        vistaExtendida = 0
                    }
                }) {
                    if (vistaExtendida == 2) {
                        Text("❇️")
                    } else {
                        Icon(Icons.Filled.QrCode, contentDescription = "QR")
                    } // editar / volver
                }

                // Añadir/Quitar de la lista
                Button(onClick = {
                    listaRef?.get()?.addOnSuccessListener { snap ->
                        if (snap.exists()) {
                            listaRef.delete()
                            estaEnLista = false
                            Toast.makeText(context, "Quitado de la lista", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            val cantidadInicial =
                                if (item.cantidad == 0) 0 else 1

                            listaRef.set(
                                mapOf(
                                    "itemId" to item.id!!,
                                    "cantidad" to cantidadInicial
                                )
                            )
                            estaEnLista = true
                            Toast.makeText(context, "Añadido a la lista", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Text(if (estaEnLista) "✔️" else "🛒")
                }

            }
        }
    )
}



