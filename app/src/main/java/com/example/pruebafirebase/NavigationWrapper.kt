package com.example.pruebafirebase

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pruebafirebase.presentation.initial.InitialScreen
import com.example.pruebafirebase.presentation.signUp.PantallaRegistro
import com.example.pruebafirebase.presentation.home.add.AddScreen
import com.example.pruebafirebase.presentation.home.options.OptionsScreen
import com.example.pruebafirebase.presentation.home.HomeManagerScreen
import com.example.pruebafirebase.presentation.home.PantallaHome
import com.google.firebase.auth.FirebaseAuth

// Para manejar los navegadores
@Composable
fun NavigationWrapper(
    navHostController: NavHostController,
    auth: FirebaseAuth
) {
    NavHost(navController = navHostController, startDestination = "initialScreen") {
        composable("initialScreen") {
            InitialScreen(
                auth,
                navigateToHomeManager = { navHostController.navigate("homeScreenAdmin") },
                navigateToHomeClient = { navHostController.navigate("homeScreen") },
                navigateToRegister = {navHostController.navigate("registerScreen")})
        }
        composable("registerScreen"){
            PantallaRegistro(auth, navigateBack = {navHostController.popBackStack()})
        }
        composable("homeScreen"){
            PantallaHome(navigateToInitialScreen = { navHostController.navigate("initialScreen") }, navigateToOptions = {navHostController.navigate("optionsScreen")})
        }
        composable("homeScreenAdmin"){
            HomeManagerScreen(navigateToInitialScreen = { navHostController.navigate("initialScreen") }, navigateToOptions = {navHostController.navigate("optionsScreen")})
        }
        composable("addScreen"){
            AddScreen()
        }
        composable("optionsScreen"){
            OptionsScreen(onBack = {navHostController.popBackStack()})
        }
    }
}