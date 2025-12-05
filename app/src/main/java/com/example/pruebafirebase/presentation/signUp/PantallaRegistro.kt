package com.example.pruebafirebase.presentation.signUp

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pruebafirebase.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Pantalla de registro de nueva cuenta
@Composable
fun PantallaRegistro(
    auth: FirebaseAuth,
    navigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var passwordVisible2 by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Registrarse en letras grandes
        Text(
            "REGISTRARSE",
            Modifier.padding(start = 20.dp, top = 100.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 38.sp,
            textAlign = TextAlign.Center
        )

        // Email
        Text(
            "Email:",
            modifier = Modifier.padding(start = 20.dp, top = 40.dp),
            color = Color.White,
            fontSize = 28.sp
        )
        TextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 5.dp, end = 20.dp),
            singleLine = true,
            placeholder = { Text("ejemplo@gmail.com") },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.Gray
            )
        )

        // Contraseña
        Text(
            "Contraseña:",
            modifier = Modifier.padding(start = 20.dp, top = 20.dp),
            color = Color.White,
            fontSize = 28.sp
        )
        TextField(
            value = password,
            onValueChange = { password = it },
            singleLine = true,
            placeholder = { Text("Contraseña") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                // Alternar visibilidad de la contraseña
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(
                            if (passwordVisible) R.drawable.baseline_visibility_24
                            else R.drawable.baseline_visibility_off_24
                        ),
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 5.dp, end = 20.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.Gray
            )
        )

        // Confirmar contraseña
        Text(
            "Confirmar contraseña:",
            modifier = Modifier.padding(start = 20.dp, top = 20.dp),
            color = Color.White,
            fontSize = 28.sp
        )
        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            singleLine = true,
            placeholder = { Text("Confirmar contraseña") },
            visualTransformation = if (passwordVisible2) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                // Alternar visibilidad de la contraseña
                IconButton(onClick = { passwordVisible2 = !passwordVisible2 }) {
                    Icon(
                        painter = painterResource(
                            if (passwordVisible2) R.drawable.baseline_visibility_24
                            else R.drawable.baseline_visibility_off_24
                        ),
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 5.dp, end = 20.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.Gray
            )
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Boton para registrarse
            Button(
                modifier = Modifier.padding(top = 30.dp),
                onClick = {
                    // Que no puedan estar vacios
                    if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                        Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT)
                            .show()
                        return@Button
                    }
                    // Que las dos contraseñas sean iguales
                    if (password != confirmPassword) {
                        Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT)
                            .show()
                        return@Button
                    }
                    // Intentar crear usuario
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener { result ->
                            val uid = result.user?.uid ?: return@addOnSuccessListener
                            // Ademas de crear la cuenta crear una entrada en la coleccion Usuarios con el rol de cliente
                            val userData = mapOf(
                                "email" to email,
                                "uid" to uid,
                                "rol" to "cliente",
                                "createdAt" to System.currentTimeMillis()
                            )
                            db.collection("Usuarios").document(uid)
                                .set(userData)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        context,
                                        "Registrado correctamente",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navigateBack()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        context,
                                        "Error al guardar datos",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG)
                                .show()
                        }
                }
            ) {
                Text("Registrarse")
            }

            // Boton volver
            Button(
                modifier = Modifier.padding(top = 10.dp),
                onClick = { navigateBack() }
            ) {
                Text("Volver")
            }
        }
    }
}
