package com.example.foggoff.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foggoff.data.UnlockedHexRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UnlockedHexRepository()

    private val _hexCount = MutableStateFlow(0)
    val hexCount: StateFlow<Int> = _hexCount.asStateFlow()

    private val _unlockedCountries = MutableStateFlow<List<String>>(emptyList())
    val unlockedCountries: StateFlow<List<String>> = _unlockedCountries.asStateFlow()

    private val _unlockedCountryKm = MutableStateFlow<Map<String, Double>>(emptyMap())
    val unlockedCountryKm: StateFlow<Map<String, Double>> = _unlockedCountryKm.asStateFlow()

    init {
        viewModelScope.launch {
            val ids = repository.loadUnlockedH3Ids()
            _hexCount.value = ids.size
            _unlockedCountries.value = repository.loadUnlockedCountryCodes()
            _unlockedCountryKm.value = repository.loadUnlockedCountryKmByCode()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val ids = repository.loadUnlockedH3Ids()
            _hexCount.value = ids.size
            _unlockedCountries.value = repository.loadUnlockedCountryCodes()
            _unlockedCountryKm.value = repository.loadUnlockedCountryKmByCode()
        }
    }
}
