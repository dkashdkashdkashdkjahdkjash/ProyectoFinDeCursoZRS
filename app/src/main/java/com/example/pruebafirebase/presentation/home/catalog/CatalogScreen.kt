package com.example.pruebafirebase.presentation.home.catalog

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pruebafirebase.sharedPreferences.SharedPreferenceManager
import com.example.pruebafirebase.model.CosasQueVender
import com.example.pruebafirebase.model.haveDiscount
import com.example.pruebafirebase.model.haveStock
import com.example.pruebafirebase.ui.utils.methods.DetalleDialog
import com.example.pruebafirebase.ui.utils.methods.DetalleDialogManager
import com.example.pruebafirebase.ui.utils.methods.decodeBase64ToImageBitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.Normalizer

//Pantalla catalogo
@Composable
fun CatalogoScreen() {
    val db = FirebaseFirestore.getInstance()
    val bienesRef = db.collection("Bienes")
    db.collection("Favoritos")
    var items by remember { mutableStateOf<List<CosasQueVender>>(emptyList()) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var selectedItem by remember { mutableStateOf<CosasQueVender?>(null) }
    val context = LocalContext.current
    val sharedPreferenceManager = remember { SharedPreferenceManager(context) }
    val darkMode by rememberSaveable { mutableStateOf(sharedPreferenceManager.darkMode) }
    var filtro by remember { mutableStateOf(FiltroCatalogo.NONE) }
    var userRole by remember { mutableStateOf<String?>(null) }

    //Lee los objetos de la base de datos y los asigna a items
    LaunchedEffect(Unit) {
        bienesRef.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            items = snapshot?.documents?.mapNotNull { doc ->
                val item = doc.toObject(CosasQueVender::class.java)
                item?.copy(id = doc.id)
            } ?: emptyList()
        }
    }

    //Asigna el rol del usuario a userRole usando la funcion getUserRole()
    LaunchedEffect(Unit) {
        userRole = getUserRole()
    }

    //Normaliza el texto (ignora tildes, mayusculas, etc.)
    fun normalize(text: String): String {
        val temp = Normalizer.normalize(text, Normalizer.Form.NFD)
        return Regex("\\p{InCombiningDiacriticalMarks}+").replace(temp, "")
    }

    //Filtra el nombre de los items por el filtro normalizado
    val itemsFiltradosPorBusqueda = items.filter {
        normalize(it.nombre ?: "").contains(
            normalize(searchQuery.text),
            ignoreCase = true
        )
    }

    //Filtra por si tienen descuento o si no tienen stock (compatible con el otro filtro)
    val filteredItems = when (filtro) {
        FiltroCatalogo.DESCUENTO -> itemsFiltradosPorBusqueda.filter { it.haveDiscount() }
        FiltroCatalogo.SIN_STOCK -> itemsFiltradosPorBusqueda.filter { !it.haveStock() }
        else -> itemsFiltradosPorBusqueda
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        //Filtro por nombre
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar...") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            //Filtro por si tiene descuento
            FilterStyledButton(
                texto = "Con descuento",
                activo = filtro == FiltroCatalogo.DESCUENTO,
                onClick = {
                    filtro =
                        if (filtro == FiltroCatalogo.DESCUENTO) FiltroCatalogo.NONE else FiltroCatalogo.DESCUENTO
                }
            )
            //Filtro por si no tiene stock
            FilterStyledButton(
                texto = "Sin stock",
                activo = filtro == FiltroCatalogo.SIN_STOCK,
                onClick = {
                    filtro =
                        if (filtro == FiltroCatalogo.SIN_STOCK) FiltroCatalogo.NONE else FiltroCatalogo.SIN_STOCK
                }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        //Coger los items y ponerlos de 3 en 3
        val chunkedItems = filteredItems.chunked(3)

        //Mostrar filas de 3 items
        chunkedItems.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                //Mostrar cada item de cada fila
                row.forEach { item ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedItem = item }
                            .padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        //La imagen de la base de datos que es un string de base64 que usamos
                        //la funcion decodeBase64ToImageBitmap para pasarlo a imagenBitmap
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
                        //Si tiene darkMode hacer que el texto sea blanco
                        if (darkMode) {
                            Text(
                                text = item.nombre ?: "Sin nombre",
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                        } else {
                            //Si no pues negro
                            Text(
                                text = item.nombre ?: "Sin nombre",
                                color = Color.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                //Para cuando no hay suficientes items en una fila que no se centren
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }

    //Muestra un DetalleDialog diferente dependiendo de si es admin o no
    if (userRole == "admin") {
        selectedItem?.let { item ->
            DetalleDialogManager(
                item = item,
                onDismiss = { selectedItem = null }
            )
        }
    } else {
        selectedItem?.let { item ->
            DetalleDialog(
                item = item,
                onDismiss = { selectedItem = null }
            )
        }
    }

}

//Para el filtrado por botones
enum class FiltroCatalogo {
    NONE, DESCUENTO, SIN_STOCK
}


//Con estilo
@Composable
fun FilterStyledButton(
    texto: String,
    activo: Boolean,
    onClick: () -> Unit
) {
    if (activo) {
        // Botón activo -> rojo (como descuentos)
        Button(
            onClick = onClick,
            modifier = Modifier
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD9534F),
                contentColor = Color.White
            )
        ) {
            Text(texto, fontSize = 15.sp)
        }

    } else {
        // Botón inactivo -> outlined
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(texto, fontSize = 15.sp)
        }
    }
}

//Coge el rol del usuario de la base de datos
suspend fun getUserRole(): String? {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null
    val db = FirebaseFirestore.getInstance()
    val snap = db.collection("Usuarios").document(uid)
        .get()
        .await()
    return snap.getString("rol") // devuelve "admin", "cliente" o null
}