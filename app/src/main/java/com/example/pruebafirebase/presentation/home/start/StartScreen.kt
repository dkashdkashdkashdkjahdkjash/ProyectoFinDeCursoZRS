package com.example.pruebafirebase.presentation.home.start

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pruebafirebase.sharedPreferences.SharedPreferenceManager
import com.example.pruebafirebase.model.CosasQueVender
import com.example.pruebafirebase.model.haveDiscount
import com.example.pruebafirebase.model.haveStock
import com.example.pruebafirebase.ui.utils.methods.DetalleDialog
import com.example.pruebafirebase.ui.utils.methods.decodeBase64ToImageBitmap
import com.google.firebase.firestore.FirebaseFirestore

// Pantalla incial que se muestra al entrar en HomeScreen()
@Composable
fun InicioScreen() {
    val db = FirebaseFirestore.getInstance()
    var items by remember { mutableStateOf<List<CosasQueVender>>(emptyList()) }
    var selectedItem by remember { mutableStateOf<CosasQueVender?>(null) }
    val context = LocalContext.current
    val sharedPreferenceManager = remember { SharedPreferenceManager(context) }
    val darkMode by rememberSaveable { mutableStateOf(sharedPreferenceManager.darkMode) }

    // Lee items de la base de datos
    LaunchedEffect(Unit) {
        db.collection("Bienes")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                items = snapshot?.documents?.mapNotNull { doc ->
                    val item = doc.toObject(CosasQueVender::class.java)
                    item?.copy(id = doc.id)
                } ?: emptyList()
            }
    }

    // Hace filas de 7
    val chunkedWithDiscount = items.filter { it.haveDiscount() }.chunked(7)
    val chunkedWithoutStock = items.filter { !it.haveStock() }.chunked(7)
    val chunkedRandom = items.shuffled().chunked(7)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        //Muestra la primera fila de chunkedWithDiscount
        SectionRow(title = "Con Descuento") {
            val row = chunkedWithDiscount.firstOrNull() ?: List(7) { null }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { item ->
                    if (item == null) {
                        EmptyItemPlaceholder()
                    } else {
                        Column(
                            modifier = Modifier
                                .width(100.dp)
                                .clickable { selectedItem = item }
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val imageBitmap = remember(item.imagen) {
                                decodeBase64ToImageBitmap(item.imagen ?: "")
                            }
                            Image(
                                bitmap = imageBitmap ?: ImageBitmap(1, 1),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(Modifier.height(8.dp))

                            // Porque el modo oscuro no cambia automaticamente el color aqui
                            if (darkMode){
                                Text(
                                    text = item.nombre ?: "Sin nombre",
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }else{
                                Text(
                                    text = item.nombre ?: "Sin nombre",
                                    color = Color.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))


        //Muestra la primera fila de chunkedWithoutStock
        SectionRow(title = "Sin Stock") {
            val row = chunkedWithoutStock.firstOrNull() ?: List(7) { null }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { item ->
                    if (item == null) {
                        EmptyItemPlaceholder()
                    } else {
                        Column(
                            modifier = Modifier
                                .width(100.dp)
                                .clickable { selectedItem = item }
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val imageBitmap = remember(item.imagen) {
                                decodeBase64ToImageBitmap(item.imagen ?: "")
                            }
                            Image(
                                bitmap = imageBitmap ?: ImageBitmap(1, 1),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )

                            Spacer(Modifier.height(8.dp))

                            // Porque el modo oscuro no cambia automaticamente el color aqui
                            if (darkMode){
                                Text(
                                    text = item.nombre ?: "Sin nombre",
                                    color = Color.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }else{
                                Text(
                                    text = item.nombre ?: "Sin nombre",
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }

                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        //Muestra la primera fila de chunkedRandom
        SectionRow(title = "Recomendados") {
            val row = chunkedRandom.firstOrNull() ?: List(7) { null }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { item ->
                    if (item == null) {
                        EmptyItemPlaceholder()
                    } else {
                        Column(
                            modifier = Modifier
                                .width(100.dp)
                                .clickable { selectedItem = item }
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val imageBitmap = remember(item.imagen) {
                                decodeBase64ToImageBitmap(item.imagen ?: "")
                            }
                            Image(
                                bitmap = imageBitmap ?: ImageBitmap(1, 1),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )

                            Spacer(Modifier.height(8.dp))

                            // Porque el modo oscuro no cambia automaticamente el color aqui
                            if (darkMode){
                                Text(
                                    text = item.nombre ?: "Sin nombre",
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }else{
                                Text(
                                    text = item.nombre ?: "Sin nombre",
                                    color = Color.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Llama a DetalleDialog
    selectedItem?.let { item ->
        DetalleDialog(
            item = item,
            onDismiss = { selectedItem = null }
        )
    }
}

// Para poner el Ver todos a la derecha del nombre de cada fila
@Composable
fun SectionRow(title: String, content: @Composable RowScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "Ver todos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {

                }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

// Si no tiene items ocupa el espacio
@Composable
fun EmptyItemPlaceholder() {
    Column(
        modifier = Modifier
            .width(100.dp)
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .size(100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "—",
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}
