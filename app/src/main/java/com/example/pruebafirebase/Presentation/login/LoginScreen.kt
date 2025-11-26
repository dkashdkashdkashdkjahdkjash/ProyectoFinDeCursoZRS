package com.example.pruebafirebase.Presentation.login

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pruebafirebase.R
import com.google.firebase.auth.FirebaseAuth


@Composable
fun LoginScreen(auth: FirebaseAuth, navigateToHome: () -> Unit={},navigateToInitial: () -> Unit={}) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Row() {
            IconButton(onClick = {navigateToInitial()})
            {
                Icon(painter = painterResource(R.drawable.ic_back_24),"")
            }
//            IconButton(onClick = {
//                navigateToHome()
//            }) {
//                Icon(
//                    painter = painterResource(id = R.drawable.ic_back_24),
//                    contentDescription = "",
//                    tint = Color.White,
//                    modifier = Modifier
//                        .padding(vertical = 24.dp)
//                        .size(24.dp)
//                )
//            }

//            Spacer(modifier = Modifier.weight(1f))
        }

        Text("Email", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 40.sp)
        TextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.DarkGray
            )
        )

        Spacer(Modifier.height(48.dp))

        Text("Password", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 40.sp)
        TextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.DarkGray
            )
        )
        Spacer(Modifier.height(48.dp))
        Button(onClick = {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //navegar
                    navigateToHome()
                    Log.i("aris", "LOGIN OK")
                } else {
                    //error
                    Log.i("aris", "LOGIN KO")
                }
            }
        }) {
            Text("Login")
        }
    }
}
