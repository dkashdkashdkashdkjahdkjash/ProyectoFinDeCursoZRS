package com.example.pruebafirebase.presentation.home.options

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import kotlinx.coroutines.tasks.await
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.example.pruebafirebase.sharedPreferences.SharedPreferenceManager
import com.example.pruebafirebase.ui.utils.DarkColors
import com.example.pruebafirebase.ui.utils.LightColors

//Pantalla para cambiar de contraseña
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsScreen(
    onBack: () -> Unit = {}
) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val sharedPreferenceManager = remember { SharedPreferenceManager(context) }
    val darkMode by rememberSaveable { mutableStateOf(sharedPreferenceManager.darkMode) }

    //Cambia de colores dependiendo de darkMode
    MaterialTheme(
        colorScheme = if (darkMode) DarkColors else LightColors
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Cambiar contraseña") },
                    //Boton para ir atras
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Contraseña actual
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Contraseña actual") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Contraseña nueva
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Nueva contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Confirmar contraseña nueva
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirmar nueva contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                //Boton para actualizar contraseña
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (user == null) {
                                snackbarHostState.showSnackbar("Usuario no encontrado")
                                return@launch
                            }
                            if (currentPassword.isBlank() || newPassword.isBlank()) {
                                snackbarHostState.showSnackbar("Completa todos los campos")
                                return@launch
                            }
                            if (newPassword != confirmPassword) {
                                snackbarHostState.showSnackbar("Las contraseñas no coinciden")
                                return@launch
                            }
                            if (newPassword.length < 6) {
                                snackbarHostState.showSnackbar("La nueva contraseña debe tener al menos 6 caracteres")
                                return@launch
                            }
                            loading = true

                            // Reautenticación
                            val credential = EmailAuthProvider.getCredential(
                                user.email ?: "",
                                currentPassword
                            )
                            try {
                                user.reauthenticate(credential).await()

                                // Actualizar la contraseña
                                user.updatePassword(newPassword).await()

                                snackbarHostState.showSnackbar("Contraseña actualizada correctamente")
                                currentPassword = ""
                                newPassword = ""
                                confirmPassword = ""
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Error: ${e.message}")
                            } finally {
                                loading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Cambiar contraseña")
                    }
                }

            }
        }
    }
}