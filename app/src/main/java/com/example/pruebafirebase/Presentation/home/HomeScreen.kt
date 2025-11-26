package com.example.pruebafirebase.Presentation.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.pruebafirebase.Presentation.model.Artist
import com.example.pruebafirebase.Presentation.model.CosasQueVender
import com.example.pruebafirebase.ui.theme.Black

@Composable
fun HomeScreen(viewmodel: HomeViewmodel = HomeViewmodel()) {

//    val artists: State<List<Artist>> = viewmodel.artist.collectAsState()

    val objetos: State<List<CosasQueVender>> = viewmodel.objetos.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        Text(
            "Popular artists",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            modifier = Modifier.padding(16.dp)
        )



        LazyRow(Modifier.background(Color.Gray)) {
            items(objetos.value) {
                ObjectItem(it)
                it.Nombre?.let { it1 -> Log.i("asdasdasd", it1) }
            }
        }
//        LazyRow(modifier = Modifier.background(Color.Gray)) {
//            items(artists.value) {
//                ArtistItem(it)
//                it.name?.let { it1 -> Log.i("asdasdasd", it1) }
//            }
//        }
    }

}


@Composable
fun ObjectItem(objetos: CosasQueVender) {
    Column {
        AsyncImage(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape),
            model = objetos.Imagen,
            contentDescription = "Objeto imagen"
        )
        Text(text = objetos.Nombre.orEmpty(), color = Color.Black)
        objetos.Nombre?.let { Log.i("Coooooosa", it) }

    }
}

@Composable
fun ArtistItem(artist: Artist) {
    Column {
        AsyncImage(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape),
            model = artist.image,
            contentDescription = "Artist image"
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = artist.name.orEmpty(), color = Color.White)
        artist.name?.let { Log.i("Coooooosa", it) }
    }
}

//fun createArtist(db: FirebaseFirestore) {
//    val random = (1..10000).random()
//    val artist = Artist(name = "Random $random", numberOfSongs = random)
//    db.collection("artists")
//        .add(artist)
//        .addOnSuccessListener {
//            Log.i("Aris", "SUCCESS")
//        }
//        .addOnFailureListener {
//            Log.i("Aris", "FAILURE")
//        }
//        .addOnCompleteListener {
//            Log.i("Aris", "COMPLETE")
//        }
//}