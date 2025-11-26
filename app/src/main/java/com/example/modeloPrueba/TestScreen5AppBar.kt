package com.example.modeloPrueba

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.text.Normalizer

// --- Esquemas de colores claros y oscuros ---
private val LightColors = lightColorScheme(
    primary = Color(0xFF6200EE),
    onPrimary = Color.White,
    secondary = Color(0xFF03DAC6),
    background = Color(0xFFF2F2F2),
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFFBB86FC),
    onPrimary = Color.Black,
    secondary = Color(0xFF03DAC6),
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White
)

@Composable
fun TestScreen5AppBar() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedIndex by remember { mutableIntStateOf(1) }
    var darkMode by remember { mutableStateOf(false) }

    val drawerItems = listOf(
        "Opciones" to Icons.Default.Settings,
        "Salir" to Icons.AutoMirrored.Filled.ExitToApp
    )

    val bottomBarItems = listOf(
        "Catalogo" to Icons.Default.Store,
        "Inicio" to Icons.Default.Home,
        "Favoritos" to Icons.Default.Favorite,
        "Lista" to Icons.Default.ShoppingCart
    )

    // --- MaterialTheme dinámico según darkMode ---
    MaterialTheme(
        colorScheme = if (darkMode) DarkColors else LightColors
    ) {
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
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Mi Usuario",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // --- Switch de Modo Oscuro (ahora primero) ---
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
                            )
                        )
                    }
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                    )

                    Spacer(Modifier.height(16.dp))

                    // --- Drawer Items después del switch ---
                    drawerItems.forEach { (label, icon) ->
                        DrawerItem(label, icon) {
                            coroutineScope.launch {
                                drawerState.close()
                                snackbarHostState.showSnackbar("$label seleccionada")
                            }
                        }
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                        )
                    }
                }
            },
            scrimColor = Color.Black.copy(alpha = 0.4f)
        )
        {
            val context = LocalContext.current
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
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "QRScanner")
                    }
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
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
                        // --- Contenido dinámico con animación ---
                        AnimatedContent(
                            targetState = selectedIndex,
                            transitionSpec = {
                                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> -width } + fadeOut())
                            }
                        ) { targetIndex ->
                            when (targetIndex) {
                                0 -> CatalogoScreen()
                                1 -> InicioScreen()
                                2 -> FavoritosScreen()
                                3 -> ListaScreen()
                            }
                        }
                    }
                }
            )
        }
    }
}











@Composable
fun CatalogoScreen(snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }) {
    val catalogItems = listOf(
        "Árbol", "Niño", "Canción", "Perro", "Elefante", "Índice",
        "Libro", "Sol", "Montaña", "Río", "Mariposa", "Computadora",
        "Coche", "Casa", "Flor", "Gato", "Estrella", "Lápiz", "Ventana", "Música"
    )
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    fun normalize(text: String): String {
        val temp = Normalizer.normalize(text, Normalizer.Form.NFD)
        return Regex("\\p{InCombiningDiacriticalMarks}+").replace(temp, "")
    }

    val filteredItems = catalogItems.filter {
        normalize(it).contains(
            normalize(searchQuery.text),
            ignoreCase = true
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Barra de búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Grid usando chunked
        val chunkedItems = filteredItems.chunked(3)
        chunkedItems.forEachIndexed { rowIndex, rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Si es la última fila y tiene menos de 3 elementos
                val isLastRow = rowIndex == chunkedItems.lastIndex
                if (isLastRow && rowItems.size < 3) {
                    rowItems.forEach { item ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(Color(0xFF42A5F5))
                                .clickable {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("$item seleccionado")
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = item, color = Color.White)
                        }
                    }
                    // Rellenar espacios restantes con Spacers
                    repeat(3 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                } else {
                    // Filas normales con 3 elementos
                    rowItems.forEach { item ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(Color(0xFF42A5F5))
                                .clickable {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("$item seleccionado")
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = item, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}




@Composable
fun InicioScreen() {
    // Lista de ejemplo con tildes
    val items = listOf(
        "Árbol",
        "Niño",
        "Canción",
        "Perro",
        "Elefante",
        "Índice",
        "Libro",
        "Sol",
        "Montaña",
        "Río",
        "Mariposa",
        "Computadora",
        "Coche",
        "Casa",
        "Flor",
        "Gato",
        "Estrella",
        "Lápiz",
        "Ventana",
        "Música"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // --- Primera sección ---
        SectionRow(title = "Recomendados") {
            items.forEach { item ->
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xFF42A5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(item, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Segunda sección ---
        SectionRow(title = "Nuevos") {
            items.forEach { item ->
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xFF42A5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(item, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Tercera sección ---
        SectionRow(title = "Populares") {
            items.forEach { item ->
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xFF42A5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(item, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SectionRow(title: String, content: @Composable RowScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "Ver todos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    // Acción al hacer clic en "Ver todos"
                    // Por ejemplo, navegar a otra pantalla
                }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}


@Composable
fun FavoritosScreen() {
    var favoritos by remember { mutableStateOf(listOf("Favorito 1", "Favorito 2", "Favorito 3")) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedFavorito by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Lista Favoritos",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(16.dp))

        favoritos.forEachIndexed { index, favorito ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        selectedFavorito = favorito
                        showDialog = true
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red)
                Spacer(Modifier.width(16.dp))
                Text(
                    favorito,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = {
                    favoritos = favoritos.toMutableList().also { it.removeAt(index) }
                }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Favorito seleccionado") },
            text = { Text("Has seleccionado: $selectedFavorito") },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Aceptar")
                }
            }
        )
    }
}


@Composable
fun ListaScreen() {
    // Lista de elementos con su estado de checkbox y cantidad
    var listaItems by remember {
        mutableStateOf(
            listOf(
                ListaItem("Item 1", false, 1),
                ListaItem("Item 2", false, 1),
                ListaItem("Item 3", false, 1),
                ListaItem("Item 4", false, 1),
                ListaItem("Item 5", false, 1)
            )
        )
    }

    // Estado del diálogo
    var showDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<ListaItem?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Pantalla Lista",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(16.dp))

        listaItems.forEachIndexed { index, item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        selectedItem = item
                        showDialog = true
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Checkbox
                Checkbox(
                    checked = item.checked,
                    onCheckedChange = { checked ->
                        listaItems = listaItems.toMutableList().also {
                            it[index] = it[index].copy(checked = checked)
                        }
                        selectedItem = listaItems[index]
                        showDialog = true
                    }
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Texto del item
                Text(
                    item.text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )

                // Contador de cantidad
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        if (item.quantity > 1) {
                            listaItems = listaItems.toMutableList().also {
                                it[index] = it[index].copy(quantity = it[index].quantity - 1)
                            }
                        }
                    }) {
                        Icon(Icons.Default.Remove, contentDescription = "Disminuir")
                    }
                    Text(item.quantity.toString(), style = MaterialTheme.typography.bodyLarge)
                    IconButton(onClick = {
                        listaItems = listaItems.toMutableList().also {
                            it[index] = it[index].copy(quantity = it[index].quantity + 1)
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Aumentar")
                    }
                }

                // Botón de eliminar
                IconButton(onClick = {
                    listaItems = listaItems.toMutableList().also { it.removeAt(index) }
                }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    // AlertDialog al hacer clic en un item
    if (showDialog && selectedItem != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Elemento seleccionado") },
            text = { Text("Has seleccionado: ${selectedItem!!.text}\nCantidad: ${selectedItem!!.quantity}") },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Aceptar")
                }
            }
        )
    }
}

// Clase de datos con checkbox y cantidad
data class ListaItem(
    val text: String,
    val checked: Boolean,
    val quantity: Int
)



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