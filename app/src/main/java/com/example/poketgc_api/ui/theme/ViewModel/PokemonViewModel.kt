package com.example.poketgc_api.ui.theme.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.poketgc_api.Datos.Data.PokeCardData
import com.example.poketgc_api.Datos.Data.PokemonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PokemonViewModel(private val repository: PokemonRepository) : ViewModel() {

    private val _pokeCardsData = MutableStateFlow<List<PokeCardData>>(emptyList())
    val pokeCardsData: StateFlow<List<PokeCardData>> = _pokeCardsData

    init {
        viewModelScope.launch {
            _pokeCardsData.value = repository.getAllCards()
        }
    }
}

class PokemonViewModelFactory(private val repository: PokemonRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PokemonViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PokemonViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}