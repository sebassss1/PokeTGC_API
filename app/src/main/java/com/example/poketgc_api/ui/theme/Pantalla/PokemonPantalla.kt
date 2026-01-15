package com.example.poketgc_api.ui.theme.Pantalla

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.poketgc_api.Data.PokemonCard
import com.example.poketgc_api.Data.PokemonRepository
import com.example.poketgc_api.Data.RetrofitInstance
import com.example.poketgc_api.PokemonViewModel
import com.example.poketgc_api.PokemonViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonPantalla(
    viewModel: PokemonViewModel = viewModel(
        factory = PokemonViewModelFactory(PokemonRepository(RetrofitInstance.api))
    )
) {
    val pokemonCards by viewModel.pokemonCards.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cartas Pokemon") },
                actions = {
                    IconButton(onClick = { /* TODO: Implement filter */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(paddingValues),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(pokemonCards) { pokemonCard ->
                PokemonCardItem(pokemonCard = pokemonCard)
            }
        }
    }
}

@Composable
fun PokemonCardItem(pokemonCard: PokemonCard) {
    Card {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            AsyncImage(
                model = pokemonCard.image?.let { "${it}_hires.png" },
                contentDescription = pokemonCard.name,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Fit
            )
            Text(text = pokemonCard.name, style = MaterialTheme.typography.titleMedium)
        }
    }
}
