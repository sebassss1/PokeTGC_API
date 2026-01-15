package com.example.poketgc_api.ui.theme.Pantalla

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.poketgc_api.Data.PokemonData

@Composable
fun PokemonPantalla(pokemonData: PokemonData) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context = LocalContext.current)
                    .data(pokemonData.imagen?.toString())
                    .crossfade(true)
                    .build(),
                contentDescription = pokemonData.nombre,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(text = pokemonData.nombre ?: "Nombre desconocido", modifier = Modifier.padding(top = 8.dp))
        }
    }
}