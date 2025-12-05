package com.example.pruebafirebase.presentation.home.favorites

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.pruebafirebase.model.CosasQueVender
import com.example.pruebafirebase.ui.utils.methods.DetalleDialog
import com.example.pruebafirebase.ui.utils.methods.decodeBase64ToImageBitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

//Pantalla favoritos
@Composable
fun FavoritosScreen() {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val userRef = FirebaseFirestore.getInstance().collection("Usuarios").document(uid)
    val favoritosRef = userRef.collection("Favoritos")
    val productosRef = FirebaseFirestore.getInstance().collection("Bienes") // o "Bienes"
    var favoritos by remember { mutableStateOf<List<CosasQueVender>>(emptyList()) }
    var selectedItem by remember { mutableStateOf<CosasQueVender?>(null) }

    //Escuchar cambios en Favoritos, una coleccion de cada usuario
    LaunchedEffect(Unit) {
        favoritosRef.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            val idsFavoritos = snapshot?.documents?.mapNotNull { doc ->
                doc.getString("itemId")
            } ?: emptyList()

            //Si no hay nada devuelve una lista vacia
            if (idsFavoritos.isEmpty()) {
                favoritos = emptyList()
                return@addSnapshotListener
            }
            //Funcion para cuando un usuario tiene en su lista un item que ya no existe eliminarlo
            fun limpiarIdsInexistentes(existentes: List<String>) {
                val inexistentes = idsFavoritos.filter { it !in existentes }
                inexistentes.forEach { idInexistente ->
                    favoritosRef.whereEqualTo("itemId", idInexistente)
                        .get()
                        .addOnSuccessListener { docs ->
                            docs.forEach { it.reference.delete() }
                        }
                }
            }
            // Si hay 10 o menos ids, usamos whereIn sobre documentId()
            if (idsFavoritos.size <= 10) {
                productosRef.whereIn(FieldPath.documentId(), idsFavoritos).get()
                    .addOnSuccessListener { result ->
                        val idsExistentes = result.documents.map { it.id }
                        //Llamamos la funcion para limpiar ids inexistentes
                        limpiarIdsInexistentes(idsExistentes)
                        favoritos = result.documents.mapNotNull { doc ->
                            val item = doc.toObject(CosasQueVender::class.java)
                            // garantizamos que el id del documento quede en el modelo
                            item?.copy(id = doc.id)
                        }
                    }
            } else {
                // Si hay más de 10 ids, los pedimos por lotes (chunks de 10) y los combinamos
                val chunks = idsFavoritos.chunked(10)
                val listaTemporal = mutableListOf<CosasQueVender>()
                val idsExistentes = mutableListOf<String>()
                var processed = 0
                chunks.forEach { chunk ->
                    productosRef.whereIn(FieldPath.documentId(), chunk).get()
                        .addOnSuccessListener { result ->
                            idsExistentes.addAll(result.documents.map { it.id })
                            val loaded = result.documents.mapNotNull { doc ->
                                val item = doc.toObject(CosasQueVender::class.java)
                                item?.copy(id = doc.id)
                            }
                            listaTemporal.addAll(loaded)
                            processed++
                            if (processed == chunks.size) {
                                //Llamamos la funcion para limpiar ids inexistentes
                                limpiarIdsInexistentes(idsExistentes)
                                favoritos = listaTemporal
                            }
                        }
                }
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Favoritos",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(16.dp))
        //Mostrar los favoritos
        favoritos.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { selectedItem = item }
                    .padding(12.dp)
            ) {
                //Imagen del item
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
                //Texto del item
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.nombre ?: "Sin nombre", style = MaterialTheme.typography.bodyLarge)
                }
                //Botom para eliminar favoritos, de la lista no el item
                IconButton(onClick = {
                    favoritosRef
                        .whereEqualTo("itemId", item.id)
                        .get()
                        .addOnSuccessListener { docs ->
                            docs.forEach { it.reference.delete() }
                        }
                }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    //Si haces click en un item de favoritos lanza la funcion DetalleDialog
    selectedItem?.let { item ->
        DetalleDialog(
            item = item,
            onDismiss = { selectedItem = null }
        )
    }
}