package com.example.pruebafirebase.presentation.home.list

import android.annotation.SuppressLint
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
import androidx.compose.ui.unit.dp
import com.example.pruebafirebase.model.CosasQueVender
import com.example.pruebafirebase.model.discountedPrice
import com.example.pruebafirebase.model.haveDiscount
import com.example.pruebafirebase.ui.utils.methods.DetalleDialog
import com.example.pruebafirebase.ui.utils.methods.decodeBase64ToImageBitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

//Pantalla lista de cliente
@SuppressLint("DefaultLocale")
@Composable
fun ListaScreen() {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val userRef = FirebaseFirestore.getInstance().collection("Usuarios").document(uid)
    val listaRef = userRef.collection("Lista")
    val productosRef = FirebaseFirestore.getInstance().collection("Bienes")
    var lista by remember { mutableStateOf<List<CosasQueVender>>(emptyList()) }
    var cantidades by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var selectedItem by remember { mutableStateOf<CosasQueVender?>(null) }
    var showVaciarDialog by remember { mutableStateOf(false) }
    val total = lista.sumOf { item ->
        val cant = cantidades[item.id] ?: 1
        val precio = if (item.haveDiscount()) item.discountedPrice() else item.precio ?: 0.0
        precio * cant
    }

    //Escuchar cambios en Lista, una coleccion de cada usuario
    LaunchedEffect(Unit) {
        listaRef.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            val mapCantidades = mutableMapOf<String, Int>()
            val ids = mutableListOf<String>()
            //Coge la lista de la base de datos
            snapshot?.documents?.forEach { doc ->
                val itemId = doc.getString("itemId")
                val cantidad = doc.getLong("cantidad")?.toInt() ?: 1
                if (itemId != null) {
                    ids.add(itemId)
                    mapCantidades[itemId] = cantidad
                }
            }
            cantidades = mapCantidades
            //Si no hay nada, devuelve una lista vacia
            if (ids.isEmpty()) {
                lista = emptyList()
                return@addSnapshotListener
            }

            //Si tiene en su lista un item que ya no existe lo elimina
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
                            //Limpiar ids inexistentes
                            limpiarIdsInexistentes(idsExistentes)
                            lista = result.documents.mapNotNull { doc ->
                                doc.toObject(CosasQueVender::class.java)?.copy(id = doc.id)
                            }
                        }
                    }
            } else {
                // Más de 10 → chunk
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
                                    //Limpiar ids inexistentes
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
            Text("Mi Lista", style = MaterialTheme.typography.titleLarge)
            Text(
                "TOTAL: ${String.format("%.2f", total)}€",
                style = MaterialTheme.typography.titleLarge
            )
        }
        Spacer(Modifier.height(12.dp))
        //Mostrar la lista
        lista.forEach { item ->
            //Cantidad en la lista
            val cantidad = cantidades[item.id] ?: 1
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

                    //Boton para disminuir la cantidad en la lista (minimo 0)
                    IconButton(onClick = {
                        val nuevaCantidad = (cantidad - 1).coerceAtLeast(1)
                        listaRef.document(item.id!!).update("cantidad", nuevaCantidad)
                    }) {
                        Text("➖")
                    }
                    Text(cantidad.toString(), modifier = Modifier.padding(horizontal = 8.dp))

                    //Boton para aumentar la cantidad en la lista
                    IconButton(onClick = {
                        val nuevaCantidad = cantidad + 1
                        listaRef.document(item.id!!).update("cantidad", nuevaCantidad)
                    }) {
                        Text("➕")
                    }
                }

                //Boton para eliminar de la lista
                IconButton(onClick = {
                    listaRef.document(item.id!!).delete()
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }

        //Boton para limpiar la lista
        if (lista.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { showVaciarDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text("Vaciar lista", color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }

    //Dialogo de confirmacion de vaciar la lista
    if (showVaciarDialog) {
        AlertDialog(
            onDismissRequest = { showVaciarDialog = false },
            title = { Text("Vaciar lista") },
            text = { Text("¿Seguro que quieres eliminar todos los elementos?") },
            confirmButton = {
                TextButton(onClick = {
                    lista.forEach { item ->
                        listaRef.document(item.id!!).delete()
                    }
                    showVaciarDialog = false
                }) {
                    Text("Sí, vaciar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVaciarDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    //Llama la funcion DetalleDialog
    selectedItem?.let { item ->
        DetalleDialog(
            item = item,
            onDismiss = { selectedItem = null }
        )
    }
}