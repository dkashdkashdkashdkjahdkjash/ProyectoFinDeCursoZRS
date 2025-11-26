package com.example.pruebafirebase.Presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.pruebafirebase.R
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeManagerScreen(auth: FirebaseAuth, navigateToHome: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
    ) {

        Row(modifier = Modifier
            .padding(start = 10.dp)
            .fillMaxWidth())
        {
            IconButton(onClick = {
                navigateToHome()
            })
            {
                Icon(painter = painterResource(R.drawable.baseline_door_back_24),"GoingBack", tint = Color.White)
            }
        }

        Text("Manager", color = Color.White)

    }
}