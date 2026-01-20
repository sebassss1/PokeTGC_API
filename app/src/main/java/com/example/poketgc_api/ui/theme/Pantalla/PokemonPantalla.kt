package com.example.poketgc_api.ui.theme.Pantalla

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.poketgc_api.Data.PokemonCard

@Composable
fun PokemonPantalla(
    pokemonCard: PokemonCard,
    isAdded: Boolean = false,
    onToggleList: (PokemonCard) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context = LocalContext.current)
                    .data("${pokemonCard.imagen}/high.png")
                    .crossfade(true)
                    .build(),
                contentDescription = pokemonCard.nombre,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pokemonCard.nombre ?: "Nombre desconocido",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                IconButton(onClick = { onToggleList(pokemonCard) }) {
                    Icon(
                        imageVector = if (isAdded) Icons.Default.Delete else Icons.Default.Add,
                        contentDescription = if (isAdded) "Quitar" else "Añadir",
                        tint = if (isAdded) Color.Red else Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}