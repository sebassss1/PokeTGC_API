package com.example.poketgc_api

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.poketgc_api.Data.*
import com.example.poketgc_api.ui.theme.Pantalla.PokemonPantalla
import com.example.poketgc_api.ui.theme.PokeTGC_APITheme
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.accompanist.permissions.ExperimentalPermissionsApi

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.provider.MediaStore
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.Locale


/**
 * Actividad principal de la aplicación PokeTGC API.
 * Se encarga de inicializar la base de datos, el gestor de preferencias y configurar el tema.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicialización de persistencia: DataStore para ajustes y Room para la base de datos
        val settingsManager = SettingsManager(this)
        val database = AppDatabase.getDatabase(this)
        val dao = database.pokemonDao()

        setContent {
            // Observamos el estado del modo oscuro desde DataStore
            val isDarkMode by settingsManager.isDarkMode.collectAsState(initial = false)
            val scope = rememberCoroutineScope()

            // Aplicamos el tema personalizado (PokeTGC_APITheme) que reacciona al modo oscuro
            PokeTGC_APITheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Lanzamos la aplicación principal pasando los controladores de estado
                    PokemonApp(
                        isDarkMode = isDarkMode,
                        onDarkModeToggle = { enabled ->
                            // Guardamos la preferencia de modo oscuro de forma asíncrona
                            scope.launch { settingsManager.setDarkMode(enabled) }
                        },
                        dao = dao
                    )
                }
            }
        }
    }
}

/**
 * Enumeración para controlar la navegación entre las diferentes pantallas.
 */
enum class Screen {
    Home, MyLists, ViewList, EditList, TakePhoto
}


// Clase para representar la lista en la UI con sus cartas cargadas
class UserListUI(
    val entity: UserListEntity,
    val cards: MutableList<PokemonCard> = mutableStateListOf()
)

fun takePhoto(context: Context, imageCapture: ImageCapture) {
    val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        .format(System.currentTimeMillis())

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PokeTGC")
    }

    val outputOptions = ImageCapture.OutputFileOptions
        .Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
        .build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                Toast.makeText(context, "Foto guardada en galería", Toast.LENGTH_SHORT).show()
            }

            override fun onError(exc: ImageCaptureException) {
                Toast.makeText(context, "Error al guardar la foto", Toast.LENGTH_SHORT).show()
            }
        }
    )
}
//

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TakePhotoScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraPermission = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    )

    if (!cameraPermission.status.isGranted) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Se necesita permiso para usar la cámara")
            Spacer(Modifier.height(16.dp))
            Button(onClick = { cameraPermission.launchPermissionRequest() }) {
                Text("Permitir cámara")
            }
        }
        return
    }

    val imageCapture = remember {
        ImageCapture.Builder().build()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture
                    )
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            onClick = {
                takePhoto(context, imageCapture)
            }
        ) {
            Icon(Icons.Default.Camera, null)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonApp(isDarkMode: Boolean, onDarkModeToggle: (Boolean) -> Unit, dao: PokemonDao) {
    // ESTADOS DE DATOS
    var cards by remember { mutableStateOf<List<PokemonCard>>(emptyList()) } // Cartas descargadas de la API
    val userLists = remember { mutableStateListOf<UserListUI>() } // Listas del usuario cargadas de Room
    var activeList by remember { mutableStateOf<UserListUI?>(null) } // Lista seleccionada actualmente
    
    // ESTADOS DE UI
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isAscending by remember { mutableStateOf(true) }
    var currentScreen by remember { mutableStateOf(Screen.Home) }

    // ESTADOS DE FILTRO Y BÚSQUEDA
    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedRarity by remember { mutableStateOf("Todas") }
    var selectedType by remember { mutableStateOf("Todos") }
    
    var showNewListDialog by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Configuración de Retrofit para conectar con la API de TCGdex
    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl("https://api.tcgdex.net/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val api = remember { retrofit.create(TcgDexApi::class.java) }

    // EFECTO: Carga inicial de cartas desde la API
    LaunchedEffect(Unit) {
        try {
            cards = api.getAllCard().take(200)
            isLoading = false
        } catch (e: Exception) {
            errorMessage = e.message
            isLoading = false
        }
    }

    // EFECTO: Sincronización en tiempo real con la base de datos Room
    LaunchedEffect(Unit) {
        dao.getAllLists().collect { entities ->
            userLists.clear()
            entities.forEach { entity ->
                val uiList = UserListUI(entity)
                userLists.add(uiList)
                // Cargamos las cartas asociadas a cada lista
                launch {
                    dao.getCardsForList(entity.id).collect { cardEntities ->
                        uiList.cards.clear()
                        uiList.cards.addAll(cardEntities.map {
                            PokemonCard(it.cardId, it.localId, it.nombre, it.imagen, it.rarity, it.types?.split(","))
                        })
                    }
                }
            }
        }
    }

    // LÓGICA: Filtrado y ordenación de la lista mostrada
    val filteredAndSortedCards = remember(cards, isAscending, searchQuery, selectedRarity, selectedType) {
        cards.filter { card ->
            val matchesSearch = searchQuery.isEmpty() || (card.nombre?.contains(searchQuery, ignoreCase = true) ?: false)
            val matchesRarity = selectedRarity == "Todas" || card.rarity == selectedRarity
            val matchesType = selectedType == "Todos" || (card.types?.contains(selectedType) ?: false)
            val isNotUnown = card.nombre != "Unown" && !card.imagen.isNullOrEmpty()
            matchesSearch && matchesRarity && matchesType && isNotUnown
        }.let { list ->
            if (isAscending) list.sortedBy { it.localId }
            else list.sortedByDescending { it.localId }
        }
    }

    // Listas dinámicas para los filtros
    val rarities = remember(cards) { listOf("Todas") + cards.mapNotNull { it.rarity }.distinct().sorted() }
    val types = remember(cards) { listOf("Todos") + cards.flatMap { it.types ?: emptyList() }.distinct().sorted() }

    // DIÁLOGO DE FILTROS
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Filtros") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Rareza", style = MaterialTheme.typography.titleSmall)
                    rarities.forEach { rarity ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { selectedRarity = rarity }) {
                            RadioButton(selected = selectedRarity == rarity, onClick = { selectedRarity = rarity })
                            Text(rarity)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Tipo", style = MaterialTheme.typography.titleSmall)
                    types.forEach { type ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { selectedType = type }) {
                            RadioButton(selected = selectedType == type, onClick = { selectedType = type })
                            Text(type)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showFilterDialog = false }) { Text("Aceptar") } }
        )
    }

    // DIÁLOGO: Creación de nueva lista
    if (showNewListDialog) {
        AlertDialog(
            onDismissRequest = { showNewListDialog = false },
            title = { Text("Nueva Lista") },
            text = {
                TextField(value = newListName, onValueChange = { newListName = it }, placeholder = { Text("Nombre de la lista") })
            },
            confirmButton = {
                Button(onClick = {
                    if (newListName.isNotBlank()) {
                        scope.launch {
                            dao.insertList(UserListEntity(name = newListName))
                            newListName = ""
                            showNewListDialog = false
                        }
                    }
                }) { Text("Crear") }
            },
            dismissButton = { TextButton(onClick = { showNewListDialog = false }) { Text("Cancelar") } }
        )
    }

    // MENÚ LATERAL (Navigation Drawer)
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("PokeTGC Menu", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                HorizontalDivider()
                // Navegación a Inicio
                NavigationDrawerItem(
                    label = { Text("Inicio") },
                    selected = currentScreen == Screen.Home,
                    onClick = { currentScreen = Screen.Home; scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Home, null) }
                )
                // Navegación a Mis Listas
                NavigationDrawerItem(
                    label = { Text("Mis Listas (${userLists.size})") },
                    selected = currentScreen == Screen.MyLists,
                    onClick = { currentScreen = Screen.MyLists; scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.List, null) }
                )
                NavigationDrawerItem(
                    label = { Text("Tomar foto") },
                    selected = currentScreen == Screen.TakePhoto,
                    onClick = {
                        currentScreen = Screen.TakePhoto
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.PhotoCamera, null) }
                )

                Spacer(modifier = Modifier.weight(1f))
                // Interruptor de Modo Oscuro
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode, null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Modo Oscuro")
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(checked = isDarkMode, onCheckedChange = onDarkModeToggle)
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { 
                            // Control de retroceso según la pantalla actual
                            when (currentScreen) {
                                Screen.ViewList -> currentScreen = Screen.MyLists
                                Screen.EditList -> currentScreen = Screen.ViewList
                                Screen.TakePhoto -> currentScreen = Screen.Home
                                else -> scope.launch { drawerState.open() }
                            }
                        }) {
                            Icon(
                                if (currentScreen == Screen.ViewList || currentScreen == Screen.EditList || currentScreen == Screen.TakePhoto) Icons.Default.ArrowBack else Icons.Default.Menu,
                                null,
                                tint = Color.White
                            )
                        }
                    },
                    title = { 
                        if (currentScreen == Screen.EditList) {
                            // Barra de búsqueda en modo edición
                            TextField(value = searchQuery, onValueChange = { searchQuery = it }, placeholder = { Text("Buscar...", color = Color.White.copy(alpha = 0.7f)) }, singleLine = true, colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, cursorColor = Color.White, focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                        } else {
                            Text(
                                when (currentScreen) {
                                    Screen.ViewList -> activeList?.entity?.name ?: "Lista"
                                    Screen.TakePhoto -> "Tomar Foto"
                                    else -> "PokeTGC API"
                                }, color = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF4CAF50)),
                    actions = {
                        // Filtros y ordenación disponibles en Inicio y Edición
                        if (currentScreen == Screen.Home || currentScreen == Screen.EditList) {
                            IconButton(onClick = { showFilterDialog = true }) { Icon(Icons.Default.FilterList, null, tint = Color.White) }
                            IconButton(onClick = { isAscending = !isAscending }) { Icon(if (isAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward, null, tint = Color.White) }
                        } else if (currentScreen == Screen.ViewList) {
                            // Botón para entrar en modo edición desde una lista
                            IconButton(onClick = { currentScreen = Screen.EditList }) { Icon(Icons.Default.Add, null, tint = Color.White) }
                        }
                    }
                )
            },
            floatingActionButton = {
                // Botón para crear nueva lista (solo en Mis Listas)
                if (currentScreen == Screen.MyLists) {
                    ExtendedFloatingActionButton(onClick = { showNewListDialog = true }, icon = { Icon(Icons.Default.Create, null) }, text = { Text("Nueva Lista") }, containerColor = Color(0xFF4CAF50), contentColor = Color.White)
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                when (currentScreen) {
                    // PANTALLA: Inicio (Explorar todas las cartas)
                    Screen.Home -> {
                        if (isLoading) CircularProgressIndicator(Modifier.align(Alignment.Center), color = Color(0xFF4CAF50))
                        else LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(8.dp)) {
                            items(filteredAndSortedCards) { card ->
                                PokemonPantalla(pokemonCard = card, isAdded = false, onToggleList = {})
                            }
                        }
                    }
                    // PANTALLA: Gestión de Listas (Vista de carpetas)
                    Screen.MyLists -> {
                        LazyColumn(Modifier.fillMaxSize().padding(8.dp)) {
                            items(userLists) { list ->
                                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { activeList = list; currentScreen = Screen.ViewList }) {
                                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Folder, null, tint = Color(0xFF4CAF50))
                                        Spacer(Modifier.width(16.dp))
                                        Column { Text(list.entity.name); Text("${list.cards.size} cartas", style = MaterialTheme.typography.bodySmall) }
                                        Spacer(Modifier.weight(1f))
                                        // Borrar lista de la base de datos
                                        IconButton(onClick = { scope.launch { dao.deleteList(list.entity) } }) { Icon(Icons.Default.Delete, null, tint = Color.Gray) }
                                    }
                                }
                            }
                        }
                    }
                    // PANTALLA: Visualización de una lista (Contenido guardado)
                    Screen.ViewList -> {
                        activeList?.let { list ->
                            LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(8.dp)) {
                                items(list.cards) { card ->
                                    PokemonPantalla(pokemonCard = card, isAdded = true, onToggleList = { scope.launch { dao.deleteCardFromList(list.entity.id, it.id) } })
                                }
                            }
                        }
                    }
                    // PANTALLA: Edición de lista (Añadir/Quitar cartas de Room)
                    Screen.EditList -> {
                        activeList?.let { list ->
                            LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(8.dp)) {
                                items(filteredAndSortedCards) { card ->
                                    val inList = list.cards.any { it.id == card.id }
                                    PokemonPantalla(pokemonCard = card, isAdded = inList, onToggleList = { selected ->
                                        scope.launch {
                                            if (inList) dao.deleteCardFromList(list.entity.id, selected.id)
                                            else dao.insertCard(ListCardEntity(
                                                listId = list.entity.id, 
                                                cardId = selected.id, 
                                                localId = selected.localId, 
                                                nombre = selected.nombre, 
                                                imagen = selected.imagen, 
                                                rarity = selected.rarity, 
                                                types = selected.types?.joinToString(",")
                                            ))
                                        }
                                    })
                                }
                            }
                        }
                    }
                    Screen.TakePhoto -> {
                        TakePhotoScreen()
                    }
                }
            }
        }
    }
}
