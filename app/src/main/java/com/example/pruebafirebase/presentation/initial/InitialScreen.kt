package com.example.pruebafirebase.presentation.initial

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pruebafirebase.sharedPreferences.SharedPreferenceManager
import com.example.pruebafirebase.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Pantalla inicial al abrir la app
@Composable
fun InitialScreen(
    auth: FirebaseAuth,
    navigateToHomeManager: () -> Unit = {},
    navigateToHomeClient: () -> Unit = {},
    navigateToRegister: () -> Unit = {}
) {
    val context = LocalContext.current
    val sharedPreferenceManager = remember { SharedPreferenceManager(context) }
    var email by rememberSaveable { mutableStateOf(sharedPreferenceManager.email) }
    var password by rememberSaveable { mutableStateOf(sharedPreferenceManager.password) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var checked by rememberSaveable { mutableStateOf(sharedPreferenceManager.checked) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    )
    {
        // Nombre de la app
        Text(
            "BAZAR ORIENTAL ONLINE",
            Modifier.padding(top = 100.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 38.sp,
            textAlign = TextAlign.Center,
            lineHeight = 40.sp
        )
        // Email
        Text(
            "Email:",
            modifier = Modifier
                .padding(start = 20.dp, top = 25.dp, end = 20.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Start,
            color = Color.White,
            fontSize = 30.sp
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
            modifier = Modifier
                .padding(start = 20.dp, top = 15.dp, end = 20.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Start,
            color = Color.White,
            fontSize = 30.sp
        )
        TextField(
            value = password,
            onValueChange = { password = it },
            singleLine = true,
            placeholder = { Text("Contraseña") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                // Alternar visibilidad de la contraseña
                IconButton(onClick = { passwordVisible = !passwordVisible })
                {
                    if (passwordVisible) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_visibility_24),
                            "Show password"
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.baseline_visibility_off_24),
                            "Hide password"
                        )
                    }
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
        Row(
            modifier = Modifier.padding(start = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox Recuérdame
            Checkbox(
                checked = checked,
                onCheckedChange = { checked = it }
            ).also {
                if (!checked) {
                    sharedPreferenceManager.email = ""
                    sharedPreferenceManager.password = ""
                    sharedPreferenceManager.checked = false
                }
            }
            Text("Recuérdame", color = Color.White, fontSize = 20.sp)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Bottom entrar o logearse
            Button(
                modifier = Modifier.padding(top = 10.dp),
                onClick = {
                    if (email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(
                            context,
                            "No puedes dejar los campos vacios",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // Intenta entrar con los datos introducidos
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Si tienes marcado checked entonces los guarda en sharedPreferenceManager
                                    if (checked) {
                                        sharedPreferenceManager.email = email
                                        sharedPreferenceManager.password = password
                                        sharedPreferenceManager.checked = checked
                                    }

                                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                                    val db = FirebaseFirestore.getInstance()

                                    // Mira si la cuenta es cliente o manager
                                    db.collection("Usuarios").document(uid).get()
                                        .addOnSuccessListener { doc ->
                                            if (doc.exists()) {
                                                val rol = doc.getString("rol")
                                                when (rol) {
                                                    "admin" -> {
                                                        navigateToHomeManager()
                                                    }

                                                    "cliente" -> {
                                                        navigateToHomeClient()
                                                    }
                                                }
                                            }
                                        }
                                    //Si te equivocas de contraseña o email
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Email o contraseña incorrecta",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                }
            ) {
                Text("Entrar")
            }
        }
        Text(
            "- - - - - - -O- - - - - - -",
            color = Color.White,
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth()
        )
        Column(
            modifier = Modifier
                .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Boton registrarse que te envia a otra pantalla
            Button(
                modifier = Modifier.padding(top = 10.dp),
                onClick = { navigateToRegister() }
            ) {
                Text("Registrarse")
            }
        }
        Text(
            "- - - - - - -O- - - - - - -",
            color = Color.White,
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth()
        )
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Entrar como invitado. No necesita una cuenta pero tiene mas restricciones
            Button(
                modifier = Modifier.padding(top = 10.dp),
                onClick = {
                    auth.signOut()
                    navigateToHomeClient()
                }
            ) {
                Text("Entrar como invitado")
            }
        }
    }
}
