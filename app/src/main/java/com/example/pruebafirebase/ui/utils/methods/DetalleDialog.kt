package com.example.pruebafirebase.ui.utils.methods

import android.annotation.SuppressLint
import android.widget.Toast
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pruebafirebase.model.CosasQueVender
import com.example.pruebafirebase.model.discountedPrice
import com.example.pruebafirebase.model.haveDiscount
import com.example.pruebafirebase.model.haveStock
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

//DetalleDialog de clientes e invitados
@SuppressLint("DefaultLocale")
@Composable
fun DetalleDialog(
    item: CosasQueVender,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val uid = auth.currentUser?.uid
    val favRef = uid?.let {
        db.collection("Usuarios").document(it)
            .collection("Favoritos").document(item.id!!)
    }
    val listaRef = uid?.let {
        db.collection("Usuarios").document(it)
            .collection("Lista").document(item.id!!)
    }
    val ratingRef = uid?.let {
        db.collection("Bienes").document(item.id!!)
            .collection("Valoraciones").document(it)
    }
    var esFavorito by remember { mutableStateOf(false) }
    var estaEnLista by remember { mutableStateOf(false) }
    var valoracionMedia by remember { mutableDoubleStateOf(0.0) }
    var numeroVotos by remember { mutableIntStateOf(0) }
    var miPuntuacion by remember { mutableIntStateOf(0) }

    // Metodo para calcular las puntuaciones
    fun recalcularValoraciones() {
        db.collection("Bienes").document(item.id!!)
            .collection("Valoraciones")
            .get()
            .addOnSuccessListener { query ->
                val puntuaciones = query.documents.mapNotNull {
                    it.getLong("puntuacion")?.toDouble()
                }
                numeroVotos = puntuaciones.size
                valoracionMedia =
                    if (puntuaciones.isNotEmpty()) puntuaciones.average() else 0.0
            }
    }


    // Sacar datos de la BBDD
    LaunchedEffect(Unit) {
        val itemId = item.id ?: return@LaunchedEffect
        esFavorito = favRef?.get()?.await()?.exists() == true
        estaEnLista = listaRef?.get()?.await()?.exists() == true
        miPuntuacion = ratingRef?.get()?.await()
            ?.getLong("puntuacion")?.toInt() ?: 0
        val valoracionesSnap = db.collection("Bienes").document(itemId)
            .collection("Valoraciones")
            .get()
            .await()
        val puntuacionesFinales = mutableListOf<Double>()
        for (doc in valoracionesSnap.documents) {
            val puntuacion = doc.getLong("puntuacion")?.toDouble()
            val userId = doc.getString("userId")
            if (puntuacion == null || userId == null) {
                doc.reference.delete().await()
                continue
            }
            val userSnap = db.collection("Usuarios").document(userId)
                .get()
                .await()
            if (userSnap.exists()) {
                puntuacionesFinales.add(puntuacion)
            } else {
                doc.reference.delete().await()
            }
        }
        numeroVotos = puntuacionesFinales.size
        valoracionMedia = if (numeroVotos > 0) puntuacionesFinales.average() else 0.0
    }

    // Funcion guardar puntuacion
    fun guardarPuntuacion(nueva: Int) {
        if (uid == null) {
            Toast.makeText(context, "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
            return
        }
        miPuntuacion = nueva
        ratingRef?.set(
            mapOf(
                "puntuacion" to nueva,
                "userId" to uid
            )
        )?.addOnSuccessListener {
            recalcularValoraciones()
            Toast.makeText(context, "Puntuación actualizada", Toast.LENGTH_SHORT).show()
        }
    }

    // Funcion para eliminar tu puntuacion
    fun eliminarPuntuacion() {
        if (uid == null) {
            Toast.makeText(context, "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
            return
        }
        ratingRef?.delete()?.addOnSuccessListener {
            miPuntuacion = 0
            recalcularValoraciones()
            Toast.makeText(context, "Puntuación eliminada", Toast.LENGTH_SHORT).show()
        }
    }

    // AlertDialog
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
                val img = decodeBase64ToImageBitmap(item.imagen ?: "")
                Image(
                    bitmap = img ?: ImageBitmap(1, 1),
                    contentDescription = null,
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(16.dp))
                // La media y la cantidad de votos
                Text(
                    text = "⭐ ${String.format("%.1f", valoracionMedia)}   (${numeroVotos} votos)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(10.dp))
                // Si NO es invitado se le deja puntuar
                if (!isGuest()) {
                    Row {
                        (1..5).forEach { star ->
                            Text(
                                text = if (star <= miPuntuacion) "⭐" else "☆",
                                fontSize = 28.sp,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clickable { guardarPuntuacion(star) }
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                // Si hay una puntuacion que salga este boton para poder eliminarlo
                if (miPuntuacion > 0) {
                    Button(onClick = { eliminarPuntuacion() }) {
                        Text("Eliminar mi puntuación")
                    }
                    Spacer(Modifier.height(16.dp))
                }
                Spacer(Modifier.height(20.dp))

                // Stats de los items
                if (item.haveDiscount()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
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
                                text = "-${item.descuentoPorcentaje.toInt()}%",
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
                        text = "Precio: ${item.precio}€",
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (item.haveStock()) "Stock: SI" else "Stock: NO",
                    color = if (item.haveStock()) Color(0xFF2ECC71) else Color.Red,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Si NO es invitado se le muestran los botones
                if (!isGuest()) {
                    // Añadir/Quitar de favoritos
                    Button(onClick = {
                        favRef?.get()?.addOnSuccessListener { snap ->
                            if (snap.exists()) {
                                favRef.delete()
                                esFavorito = false
                                Toast.makeText(context, "Quitado de favoritos", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                favRef.set(mapOf("itemId" to item.id!!))
                                esFavorito = true
                                Toast.makeText(context, "Añadido a favoritos", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }) {
                        Text(if (esFavorito) "⭐" else "☆")
                    }
                    // Añadir/Quitar de lista
                    Button(onClick = {
                        listaRef?.get()?.addOnSuccessListener { snap ->
                            if (snap.exists()) {
                                listaRef.delete()
                                estaEnLista = false
                                Toast.makeText(context, "Quitado de la lista", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                listaRef.set(
                                    mapOf(
                                        "itemId" to item.id!!,
                                        "cantidad" to 1
                                    )
                                )
                                estaEnLista = true
                                Toast.makeText(context, "Añadido a la lista", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }) {
                        Text(if (estaEnLista) "✔️" else "🛒")
                    }
                }
            }
        }
    )
}
