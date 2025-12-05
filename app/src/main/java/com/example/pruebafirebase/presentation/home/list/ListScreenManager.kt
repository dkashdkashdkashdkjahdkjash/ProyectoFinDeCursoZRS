package com.example.pruebafirebase.presentation.home.list

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.pruebafirebase.model.CosasQueVender
import com.example.pruebafirebase.model.discountedPrice
import com.example.pruebafirebase.model.haveDiscount
import com.example.pruebafirebase.ui.utils.methods.DetalleDialogManager
import com.example.pruebafirebase.ui.utils.methods.decodeBase64ToImageBitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

//Pantalla lista del manager lo mismo que el normal pero con algun cambio
@SuppressLint("DefaultLocale")
@Composable
fun ListaScreenManager() {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val userRef = FirebaseFirestore.getInstance().collection("Usuarios").document(uid)
    val listaRef = userRef.collection("Lista")
    val productosRef = FirebaseFirestore.getInstance().collection("Bienes")
    val context = LocalContext.current
    var lista by remember { mutableStateOf<List<CosasQueVender>>(emptyList()) }
    var cantidades by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var selectedItem by remember { mutableStateOf<CosasQueVender?>(null) }
    var showVaciarDialog by remember { mutableStateOf(false) }
    val total = lista.sumOf { item ->
        val cant = cantidades[item.id] ?: 0
        val precio = if (item.haveDiscount()) item.discountedPrice() else item.precio ?: 0.0
        precio * cant
    }

    // Buscar cambios en Lista, una coleccion de Usuarios
    LaunchedEffect(Unit) {
        listaRef.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            val mapCantidades = mutableMapOf<String, Int>()
            val ids = mutableListOf<String>()
            snapshot?.documents?.forEach { doc ->
                val itemId = doc.getString("itemId")
                val cantidad = doc.getLong("cantidad")?.toInt() ?: 0

                if (itemId != null) {
                    ids.add(itemId)
                    mapCantidades[itemId] = cantidad
                }
            }
            cantidades = mapCantidades
            if (ids.isEmpty()) {
                lista = emptyList()
                return@addSnapshotListener
            }

            //Funcion para eliminar ids inexistentes
            fun limpiarIdsInexistentes(idsExistentes: List<String>) {
                val idsInexistentes = ids.filter { it !in idsExistentes }

                idsInexistentes.forEach { id ->
                    listaRef.whereEqualTo("itemId", id)
                        .get()
                        .addOnSuccessListener { docs ->
                            docs.forEach { it.reference.delete() }
                        }
                }
            }

            // cargar datos desde la base de datos como usamos whereIn necesitamos separarlos si son mas de 10
            if (ids.size <= 10) {
                productosRef.whereIn(FieldPath.documentId(), ids)
                    .addSnapshotListener { result, _ ->
                        if (result != null) {

                            val idsExistentes = result.documents.map { it.id }
                            limpiarIdsInexistentes(idsExistentes)

                            lista = result.documents.mapNotNull { doc ->
                                doc.toObject(CosasQueVender::class.java)?.copy(id = doc.id)
                            }
                        }
                    }
            } else {
                val chunks = ids.chunked(10)
                val listaTemp = mutableListOf<CosasQueVender>()
                val idsExistentesTotales = mutableListOf<String>()
                var processed = 0

                chunks.forEach { chunk ->
                    productosRef.whereIn(FieldPath.documentId(), chunk)
                        .addSnapshotListener { result, _ ->
                            if (result != null) {
                                idsExistentesTotales.addAll(result.documents.map { it.id })
                                listaTemp.addAll(
                                    result.documents.mapNotNull { doc ->
                                        doc.toObject(CosasQueVender::class.java)?.copy(id = doc.id)
                                    }
                                )
                                processed++
                                if (processed == chunks.size) {
                                    limpiarIdsInexistentes(idsExistentesTotales)
                                    lista = listaTemp
                                }
                            }
                        }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Lista Venta", style = MaterialTheme.typography.titleLarge)
            Text(
                "TOTAL: ${String.format("%.2f", total)}€",
                style = MaterialTheme.typography.titleLarge
            )
        }
        Spacer(Modifier.height(12.dp))
        //Mostrar la lista
        lista.forEach { item ->
            val cantidad = cantidades[item.id] ?: 0
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
                    .clickable { selectedItem = item }
            ) {
                val img = decodeBase64ToImageBitmap(item.imagen ?: "")
                Image(
                    bitmap = img ?: ImageBitmap(1, 1),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.nombre ?: "Sin nombre")
                    val precioUnit =
                        if (item.haveDiscount()) item.discountedPrice()
                        else item.precio ?: 0.0
                    val precioTotal = precioUnit * cantidad
                    Text("${String.format("%.2f", precioTotal)}€")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Boton para restar cantidad
                    IconButton(onClick = {
                        val nuevaCantidad = (cantidad - 1).coerceAtLeast(0)
                        listaRef.document(item.id!!).update("cantidad", nuevaCantidad)
                    }) {
                        Text("➖")
                    }

                    Text(cantidad.toString(), modifier = Modifier.padding(horizontal = 8.dp))

                    // Sumar pero no puede ser mas que la cantidad del item
                    IconButton(onClick = {
                        val stockMax = item.cantidad
                        if (cantidad < stockMax) {
                            val nuevaCantidad = (cantidad + 1).coerceAtMost(stockMax)
                            listaRef.document(item.id!!).update("cantidad", nuevaCantidad)
                        } else {
                            Toast.makeText(
                                context,
                                "Has alcanzado el máximo disponible",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }) {
                        Text("➕")
                    }
                }

                // Eliminar de la lista
                IconButton(onClick = {
                    listaRef.document(item.id!!).delete()
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }

        // Boton "hacer la venta" y vaciar la lista
        if (lista.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { showVaciarDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text("Hacer Venta", color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }

    //Resta cantidad en la base de datos
    if (showVaciarDialog) {
        AlertDialog(
            onDismissRequest = { showVaciarDialog = false },
            title = { Text("Confirmar venta") },
            text = { Text("¿Quieres realizar la venta y actualizar el stock?") },
            confirmButton = {
                TextButton(onClick = {
                    // Restar stock de cada item
                    lista.forEach { item ->
                        val cantidadVendida = cantidades[item.id] ?: 0
                        val stockActual = item.cantidad
                        val stockNuevo = (stockActual - cantidadVendida).coerceAtLeast(0)

                        // Actualizar stock en Bienes
                        productosRef.document(item.id!!)
                            .update("cantidad", stockNuevo)
                    }

                    // Vaciar la lista del usuario
                    lista.forEach { item ->
                        listaRef.document(item.id!!).delete()
                    }

                    Toast.makeText(context, "Venta realizada correctamente", Toast.LENGTH_SHORT)
                        .show()

                    showVaciarDialog = false
                }) {
                    Text("Sí, vender")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVaciarDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    //Llama a DetalleDialogManager
    selectedItem?.let { item ->
        DetalleDialogManager(
            item = item,
            onDismiss = { selectedItem = null }
        )
    }
}
