package com.example.pruebafirebase

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.modeloPrueba.PantallaInicio
import com.example.pruebafirebase.Presentation.home.HomeClientScreen
import com.example.pruebafirebase.Presentation.home.HomeManagerScreen
import com.example.pruebafirebase.Presentation.home.HomeScreen
import com.example.pruebafirebase.Presentation.initial.InitialScreen
import com.example.pruebafirebase.Presentation.initial.InitialScreen2
import com.example.pruebafirebase.Presentation.login.LoginScreen
import com.example.pruebafirebase.Presentation.signUp.SignUpScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavigationWrapper(
    navHostController: NavHostController,
    auth: FirebaseAuth
) {
    NavHost(navController = navHostController, startDestination = "pantallaInicioPrueba") {
        composable("initial") {
            InitialScreen(
                navigateToLogin = { navHostController.navigate("logIn") },
                navigateToSignUp = { navHostController.navigate("signUp") }
            )
        }
        composable("login") {
            LoginScreen(
                auth,
                navigateToHome = { navHostController.navigate("homeClient") },
                navigateToInitial = { navHostController.navigate("initial") })
        }
        composable("signUp") {
            SignUpScreen(auth)
        }
        composable("home"){
            HomeScreen()
        }
        composable("homeManager") {
            HomeManagerScreen(auth) { navHostController.navigate("initial2") }
        }
        composable("homeClient") {
            HomeClientScreen(auth) { navHostController.navigate("initial2") }
        }
        composable("initial2") {
            InitialScreen2(
                auth,
                navigateToHomeManager = { navHostController.navigate("homeManager") },
                navigateToHomeClient = { navHostController.navigate("homeClient") })
        }
        composable("pantallaInicioPrueba"){
            PantallaInicio(
                auth,
                navigateToHomeManager = { navHostController.navigate("homeManager") },
                navigateToHomeClient = { navHostController.navigate("homeClient") })
        }
    }
}