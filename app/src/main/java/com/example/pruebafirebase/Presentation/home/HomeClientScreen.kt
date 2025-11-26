package com.example.pruebafirebase.Presentation.home

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pruebafirebase.CustomCaptureActivity
import com.example.pruebafirebase.Presentation.MyAlertDialog
import com.example.pruebafirebase.R
import com.google.firebase.auth.FirebaseAuth
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeClientScreen(auth: FirebaseAuth, navigateToHome: () -> Unit = {}) {

    var search by rememberSaveable { mutableStateOf("") }
    var resultScan by rememberSaveable { mutableStateOf("") }
    val scanLauncher =
        rememberLauncherForActivityResult(
            contract = ScanContract(),
            onResult = { result ->
                resultScan = result.contents ?: "SINRESULTADO"
            }
        )
    var openDialogState = remember { mutableStateOf(false) }


    var showAlertMessage = remember { mutableStateOf(false) }

    if (showAlertMessage.value) {
        MyAlertDialog(
            onDismissRequest = { showAlertMessage.value = false },
            "Cosa",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. In ut turpis tellus. Nullam ut tincidunt felis. Fusce nec eleifend libero. Cras vulputate accumsan lectus, et efficitur nisi aliquet quis. Curabitur auctor ac ipsum sollicitudin consequat. Mauris varius at neque vitae mattis. Donec quis maximus ipsum. Maecenas scelerisque tincidunt augue vitae tincidunt. Sed eget nulla sed tortor lobortis finibus in eu neque. Curabitur rhoncus nec augue at tincidunt. Donec mollis quam sed tortor ornare pharetra. Nunc eu blandit nunc, in pretium felis. Donec laoreet non mauris in facilisis.",
            R.drawable.google,
            "ItemDialog"
        )
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
    ) {

        Row(
            modifier = Modifier
                .padding(start = 10.dp, top = 20.dp)
                .fillMaxWidth()
        )
        {
            IconButton(onClick = {
                navigateToHome()
            })
            {
                Icon(
                    painter = painterResource(R.drawable.baseline_door_back_24),
                    "GoingBack",
                    tint = Color.White
                )
            }
        }

        //Buscador
        TextField(
            value = search,
            onValueChange = { search = it },
            singleLine = true,
            placeholder = { Text("Busqueda") },
            trailingIcon = {
                IconButton(onClick = {

                }) {
                    Icon(painter = painterResource(R.drawable.baseline_search_24), "search")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 25.dp, start = 50.dp, end = 50.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.Gray
            )
        )

        //Primera Fila
        Row(
            modifier = Modifier
                .padding(start = 25.dp, top = 30.dp, end = 25.dp)
                .fillMaxWidth()
        ) {
            Text(
                "Cosas",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Ver mas",
                color = Color.Blue,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                style = TextStyle(textDecoration = TextDecoration.Underline),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier
                .padding(start = 25.dp, top = 25.dp, end = 25.dp)
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .border(border = BorderStroke(2.dp, Color.White))
        ) {
            repeat(10) {
                Text("Item $it", modifier = Modifier.padding(7.dp), color = Color.White)
            }
        }

        //Segunda Fila
        Row(
            modifier = Modifier
                .padding(start = 25.dp, top = 30.dp, end = 25.dp)
                .fillMaxWidth()
        ) {
            Text(
                "Cosas",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Ver mas",
                color = Color.Blue,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                style = TextStyle(textDecoration = TextDecoration.Underline),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier
                .padding(start = 25.dp, top = 25.dp, end = 25.dp)
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .border(border = BorderStroke(2.dp, Color.White))
        ) {
            repeat(10) {
                Text("Item $it", modifier = Modifier.padding(7.dp), color = Color.White)
            }
        }

        Text(resultScan, color = Color.Blue)
        Row(
            modifier = Modifier
                .padding(top = 250.dp)
                .fillMaxSize()
                .background(Color.LightGray),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            //Boton de añadir objeto
            IconButton(
                modifier = Modifier
                    .size(65.dp),
                onClick = {
                    showAlertMessage.value = true
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_add_24),
                    "AddNewItem",
                    tint = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(100))
                        .size(65.dp)
                        .background(color = Color.DarkGray)
                )
            }


            //Boton para escanear QR
            IconButton(
                modifier = Modifier
                    .size(65.dp),
                onClick = {
                    scanLauncher.launch(
                        ScanOptions()
                            .setPrompt("Escanear codigo QR")
                            .setCaptureActivity(CustomCaptureActivity::class.java)
                            .setOrientationLocked(false)
                    )
                    Log.i("escanerCosa", resultScan)
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_qr_code_scanner_24),
                    "QRScan",
                    tint = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(100))
                        .size(65.dp)
                        .background(color = Color.DarkGray)
                )
            }
        }
    }
}

