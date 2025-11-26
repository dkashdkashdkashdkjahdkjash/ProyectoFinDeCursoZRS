package com.example.pruebafirebase.Presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.pruebafirebase.R

@Composable
fun MyAlertDialog(
    onDismissRequest: () -> Unit,
    nombre: String,
    descripcion: String,
    painter: Int,
    imageDescription: String
) {

    Dialog(
        onDismissRequest = { onDismissRequest() }
    ) {
        Card(
            modifier = Modifier
                .wrapContentSize(Alignment.Center)
                .background(Color.Gray),
            shape = RoundedCornerShape(16.dp)
        ) {

            Column {
                //Boton de salir
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = {
                        onDismissRequest()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_door_back_24),
                            "ExitItemDialog",
                            Modifier
                                .size(40.dp)
                                .padding(top = 10.dp, end = 10.dp)

                        )
                    }
                }

                //Titulo
                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        nombre,
                        modifier = Modifier.padding(25.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp
                    )
                }

                //Imagen
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    AsyncImage(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape),
                        model = painter,
                        contentDescription = imageDescription
                    )
                }

                //Descripcion
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(descripcion)
                }
            }

            //QR CODE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    modifier = Modifier
                        .size(65.dp)
                        .padding(bottom = 10.dp),
                    onClick = {

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
}

