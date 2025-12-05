package com.example.pruebafirebase.presentation.home

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.pruebafirebase.sharedPreferences.SharedPreferenceManager
import com.example.pruebafirebase.model.CosasQueVender
import com.example.pruebafirebase.presentation.home.catalog.CatalogoScreen
import com.example.pruebafirebase.presentation.home.favorites.FavoritosScreen
import com.example.pruebafirebase.presentation.home.list.ListaScreen
import com.example.pruebafirebase.presentation.home.start.InicioScreen
import com.example.pruebafirebase.qrscanner.QRScannerActivity
import com.example.pruebafirebase.ui.utils.DarkColors
import com.example.pruebafirebase.ui.utils.LightColors
import com.example.pruebafirebase.ui.utils.methods.DetalleDialog
import com.example.pruebafirebase.ui.utils.methods.isGuest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

// Pantalla home para clientes e invitados
@Composable
fun PantallaHome(navigateToInitialScreen: () -> Unit = {}, navigateToOptions: () -> Unit) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedIndex by remember { mutableIntStateOf(1) }
    var selectedItem by remember { mutableStateOf<CosasQueVender?>(null) }
    var openDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val sharedPreferenceManager = remember { SharedPreferenceManager(context) }
    var darkMode by rememberSaveable { mutableStateOf(sharedPreferenceManager.darkMode) }

    // Que muestre cosas diferentes dependiendo de si es cliente o invitado
    val drawerItems: List<Pair<String, ImageVector>> = if (isGuest()) {
        listOf(
            "Salir" to Icons.AutoMirrored.Filled.ExitToApp
        )
    } else {
        listOf(
            "Opciones" to Icons.Default.Settings,
            "Salir" to Icons.AutoMirrored.Filled.ExitToApp
        )
    }
    val bottomBarItems: List<Pair<String, ImageVector>> = if (isGuest()) {
        listOf(
            "Catalogo" to Icons.Default.Store,
            "Inicio" to Icons.Default.Home
        )
    } else {
        listOf(
            "Catalogo" to Icons.Default.Store,
            "Inicio" to Icons.Default.Home,
            "Favoritos" to Icons.Default.Favorite,
            "Lista" to Icons.Default.ShoppingCart
        )
    }

    // Para el escaner QR en el Floating Action Button
    val qrLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val id = result.data?.getStringExtra("qr_id")

            if (id != null) {
                getCosaById(id) { item ->
                    if (item != null) {
                        selectedItem = item
                        openDialog = true
                    }
                }
            }
        }
    }



    BackHandler {
        // Para que al dar a atras no te mande al inicio, para eso esta el Salir
    }


    // Para el modo oscuro
    MaterialTheme(
        colorScheme = if (darkMode) DarkColors else LightColors
    ) {
        // La SideBar
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(280.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            ),
                            shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    // --- Header ---
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(Modifier.height(8.dp))
                        if (isGuest()) {
                            Text(
                                "Invitado",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.titleMedium
                            )
                        } else {
                            Text(
                                "Cliente",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    // El switch del modo oscuro
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            "Modo oscuro",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(
                            checked = darkMode,
                            onCheckedChange = { darkMode = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.secondary
                            ).also { sharedPreferenceManager.darkMode = darkMode }
                        )
                    }
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(16.dp))

                    //Las opciones del SideBar
                    drawerItems.forEach { (label, icon) ->
                        DrawerItem(label, icon) {
                            coroutineScope.launch {
                                drawerState.close()

                                if (isGuest()) {
                                    when (label) {
                                        "Salir" -> {
                                            FirebaseAuth.getInstance().signOut()
                                            navigateToInitialScreen()
                                        }
                                    }
                                } else {
                                    when (label) {
                                        "Salir" -> {
                                            FirebaseAuth.getInstance().signOut()
                                            navigateToInitialScreen()
                                        }

                                        "Opciones" -> {
                                            navigateToOptions()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            scrimColor = Color.Black.copy(alpha = 0.4f)
        )
        {

            //La AppBar
            Scaffold(
                topBar = {
                    MyAppBar(
                        onMenuClick = { coroutineScope.launch { drawerState.open() } }
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) },
                floatingActionButton = {
                    FloatingActionButton(onClick = {
                        val intent = Intent(context, QRScannerActivity::class.java)
                        qrLauncher.launch(intent)
                    }) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "QRScanner")
                    }
                },
                //El BottomBar
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        // Las imagenes de la BottomBar
                        bottomBarItems.forEachIndexed { index, (label, icon) ->
                            NavigationBarItem(
                                selected = selectedIndex == index,
                                onClick = { selectedIndex = index },
                                icon = {
                                    Icon(
                                        icon,
                                        contentDescription = label,
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                },
                                label = {
                                    Text(
                                        label,
                                        color = if (selectedIndex == index)
                                            MaterialTheme.colorScheme.onPrimary
                                        else
                                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(
                                        alpha = 0.7f
                                    ),
                                    selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                                    unselectedTextColor = MaterialTheme.colorScheme.onPrimary.copy(
                                        alpha = 0.7f
                                    ),
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                )
                            )
                        }
                    }
                },
                content = { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp)
                    ) {
                        // A donde te llevan las opciones de la BottomBar
                        AnimatedContent(
                            targetState = selectedIndex,
                            transitionSpec = {
                                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> -width } + fadeOut())
                            }, label = ""
                        ) { targetIndex ->
                            // Cosas distintas dependiendo de si eres invitado
                            if (isGuest()) {
                                when (targetIndex) {
                                    0 -> CatalogoScreen()
                                    1 -> InicioScreen()
                                }
                            } else {
                                when (targetIndex) {
                                    0 -> CatalogoScreen()
                                    1 -> InicioScreen()
                                    2 -> FavoritosScreen()
                                    3 -> ListaScreen()
                                }
                            }

                        }
                    }
                }
            )

            // Llamar el DetalleDialog
            if (openDialog && selectedItem != null) {
                DetalleDialog(
                    item = selectedItem!!,
                    onDismiss = { openDialog = false }
                )
            }
        }
    }
}

// Estructura de los componentes de la SideBar
@Composable
fun DrawerItem(text: String, icon: ImageVector?, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                indication = LocalIndication.current,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(vertical = 12.dp)
    ) {
        if (icon != null) {
            Icon(
                icon,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
        }
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// La AppBar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppBar(onMenuClick: () -> Unit) {
    TopAppBar(
        title = { Text("Bazar Oriental Online", color = MaterialTheme.colorScheme.onPrimary) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Menú",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    )
}


// funcion que uso en el escaner QR que busca un item por un id
fun getCosaById(id: String, onResult: (CosasQueVender?) -> Unit) {
    FirebaseFirestore.getInstance()
        .collection("Bienes")
        .document(id)
        .get()
        .addOnSuccessListener { doc ->
            val item = doc.toObject(CosasQueVender::class.java)
            onResult(item?.copy(id = doc.id))
        }
        .addOnFailureListener {
            onResult(null)
        }
}

