package com.example.poketgc_api

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.poketgc_api.Data.PokemonCard
import com.example.poketgc_api.Data.TcgDexApi
import com.example.poketgc_api.ui.theme.Pantalla.PokemonPantalla
import com.example.poketgc_api.ui.theme.PokeTGC_APITheme
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Modelo para las listas personalizadas
data class UserPokemonList(
    val name: String,
    val cards: SnapshotStateList<PokemonCard> = mutableStateListOf()
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            PokeTGC_APITheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PokemonApp(
                        isDarkMode = isDarkMode,
                        onDarkModeToggle = { isDarkMode = it }
                    )
                }
            }
        }
    }
}

enum class Screen {
    Home, MyLists, ViewList, EditList
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonApp(isDarkMode: Boolean, onDarkModeToggle: (Boolean) -> Unit) {
    var cards by remember { mutableStateOf<List<PokemonCard>>(emptyList()) }
    
    // Lista de todas las listas del usuario
    val userLists = remember { mutableStateListOf<UserPokemonList>() }
    // Lista que se está viendo o editando actualmente
    var activeList by remember { mutableStateOf<UserPokemonList?>(null) }
    
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isAscending by remember { mutableStateOf(true) }
    var currentScreen by remember { mutableStateOf(Screen.Home) }

    // Estados para búsqueda y filtrado
    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedRarity by remember { mutableStateOf("Todas") }
    
    // Estado para el diálogo de nueva lista
    var showNewListDialog by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val retrofit = remember {
        Retrofit.Builder()
            .baseUrl("https://api.tcgdex.net/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api = remember { retrofit.create(TcgDexApi::class.java) }

    LaunchedEffect(Unit) {
        try {
            cards = api.getAllCard().take(200)
            isLoading = false
        } catch (e: Exception) {
            errorMessage = e.message
            isLoading = false
        }
    }

    val filteredAndSortedCards = remember(cards, isAscending, searchQuery, selectedRarity) {
        cards.filter { card ->
            val matchesSearch = searchQuery.isEmpty() || (card.nombre?.contains(searchQuery, ignoreCase = true) ?: false)
            val matchesRarity = selectedRarity == "Todas" || card.rarity == selectedRarity
            val isNotUnown = card.nombre != "Unown" && !card.imagen.isNullOrEmpty()
            matchesSearch && matchesRarity && isNotUnown
        }.let { list ->
            if (isAscending) list.sortedBy { it.localId }
            else list.sortedByDescending { it.localId }
        }
    }

    val rarities = remember(cards) {
        listOf("Todas") + cards.mapNotNull { it.rarity }.distinct().sorted()
    }

    // Diálogo para filtrar por rareza
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Filtrar por Rareza") },
            text = {
                Column {
                    rarities.forEach { rarity ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { selectedRarity = rarity }.padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = selectedRarity == rarity, onClick = { selectedRarity = rarity })
                            Text(text = rarity, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showFilterDialog = false }) { Text("Aceptar") } }
        )
    }

    // Diálogo para crear una nueva lista
    if (showNewListDialog) {
        AlertDialog(
            onDismissRequest = { showNewListDialog = false },
            title = { Text("Nueva Lista") },
            text = {
                TextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    placeholder = { Text("Nombre de la lista") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newListName.isNotBlank()) {
                            val newList = UserPokemonList(newListName)
                            userLists.add(newList)
                            activeList = newList
                            newListName = ""
                            showNewListDialog = false
                            currentScreen = Screen.EditList
                        }
                    }
                ) { Text("Crear") }
            },
            dismissButton = { TextButton(onClick = { showNewListDialog = false }) { Text("Cancelar") } }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("PokeTGC Menu", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Inicio") },
                    selected = currentScreen == Screen.Home,
                    onClick = { currentScreen = Screen.Home; scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    modifier = Modifier.padding(8.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Mis Listas (${userLists.size})") },
                    selected = currentScreen == Screen.MyLists,
                    onClick = { currentScreen = Screen.MyLists; scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    modifier = Modifier.padding(8.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode, contentDescription = null)
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
                            when (currentScreen) {
                                Screen.ViewList -> currentScreen = Screen.MyLists
                                Screen.EditList -> currentScreen = Screen.ViewList
                                else -> scope.launch { drawerState.open() }
                            }
                        }) {
                            Icon(
                                imageVector = if (currentScreen == Screen.ViewList || currentScreen == Screen.EditList) 
                                    Icons.Default.ArrowBack else Icons.Default.Menu,
                                contentDescription = null, tint = Color.White
                            )
                        }
                    },
                    title = { 
                        when (currentScreen) {
                            Screen.EditList -> TextField(
                                value = searchQuery, onValueChange = { searchQuery = it },
                                placeholder = { Text("Buscar para añadir...", color = Color.White.copy(alpha = 0.7f)) },
                                singleLine = true, colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                                    cursorColor = Color.White, focusedTextColor = Color.White, unfocusedTextColor = Color.White
                                ), modifier = Modifier.fillMaxWidth()
                            )
                            Screen.ViewList -> Text(activeList?.name ?: "Lista", color = Color.White)
                            else -> Text("PokeTGC API", color = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF4CAF50)),
                    actions = {
                        if (currentScreen == Screen.Home || currentScreen == Screen.EditList) {
                            IconButton(onClick = { showFilterDialog = true }) { Icon(Icons.Default.FilterList, null, tint = Color.White) }
                            IconButton(onClick = { isAscending = !isAscending }) {
                                Icon(if (isAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward, null, tint = Color.White)
                            }
                        } else if (currentScreen == Screen.ViewList) {
                            IconButton(onClick = { currentScreen = Screen.EditList }) {
                                Icon(Icons.Default.Add, "Añadir cartas", tint = Color.White)
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                if (currentScreen == Screen.MyLists) {
                    ExtendedFloatingActionButton(
                        onClick = { showNewListDialog = true },
                        icon = { Icon(Icons.Default.Create, null) },
                        text = { Text("Nueva Lista") },
                        containerColor = Color(0xFF4CAF50), contentColor = Color.White
                    )
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                when (currentScreen) {
                    Screen.Home -> {
                        if (isLoading) CircularProgressIndicator(Modifier.align(Alignment.Center), color = Color(0xFF4CAF50))
                        else LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(8.dp)) {
                            items(filteredAndSortedCards) { card ->
                                PokemonPantalla(pokemonCard = card, isAdded = false, onToggleList = {})
                            }
                        }
                    }
                    Screen.MyLists -> {
                        if (userLists.isEmpty()) Box(Modifier.fillMaxSize(), Alignment.Center) { Text("No tienes listas.") }
                        else LazyColumn(Modifier.fillMaxSize().padding(8.dp)) {
                            items(userLists) { list ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { 
                                        activeList = list
                                        currentScreen = Screen.ViewList
                                    }
                                ) {
                                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Folder, null, tint = Color(0xFF4CAF50))
                                        Spacer(Modifier.width(16.dp))
                                        Column {
                                            Text(list.name, style = MaterialTheme.typography.titleMedium)
                                            Text("${list.cards.size} cartas", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Spacer(Modifier.weight(1f))
                                        IconButton(onClick = { userLists.remove(list) }) { Icon(Icons.Default.Delete, null, tint = Color.Gray) }
                                    }
                                }
                            }
                        }
                    }
                    Screen.ViewList -> {
                        activeList?.let { list ->
                            if (list.cards.isEmpty()) Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Lista vacía. Pulsa + para añadir.") }
                            else LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(8.dp)) {
                                items(list.cards) { card ->
                                    PokemonPantalla(pokemonCard = card, isAdded = true, onToggleList = { list.cards.remove(it) })
                                }
                            }
                        }
                    }
                    Screen.EditList -> {
                        activeList?.let { list ->
                            LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(8.dp)) {
                                items(filteredAndSortedCards) { card ->
                                    val inList = list.cards.any { it.id == card.id }
                                    PokemonPantalla(
                                        pokemonCard = card,
                                        isAdded = inList,
                                        onToggleList = { selected ->
                                            if (inList) list.cards.removeAll { it.id == selected.id }
                                            else list.cards.add(selected)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
