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

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    // SwipeRefresh indicator should only be active when the user pulls to refresh.
    private val _userRefreshing = MutableStateFlow(false)
    val userRefreshing: StateFlow<Boolean> = _userRefreshing.asStateFlow()

    private val _hexCount = MutableStateFlow(0)
    val hexCount: StateFlow<Int> = _hexCount.asStateFlow()

    private val _unlockedCountries = MutableStateFlow<List<String>>(emptyList())
    val unlockedCountries: StateFlow<List<String>> = _unlockedCountries.asStateFlow()

    private val _unlockedCountryKm = MutableStateFlow<Map<String, Double>>(emptyMap())
    val unlockedCountryKm: StateFlow<Map<String, Double>> = _unlockedCountryKm.asStateFlow()

    init {
        viewModelScope.launch {
            _loading.value = true
            try {
                val ids = repository.loadUnlockedH3Ids()
                _hexCount.value = ids.size
                _unlockedCountries.value = repository.loadUnlockedCountryCodes()
                _unlockedCountryKm.value = repository.loadUnlockedCountryKmByCode()
            } finally {
                _loading.value = false
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _userRefreshing.value = true
            try {
                val ids = repository.loadUnlockedH3Ids()
                _hexCount.value = ids.size
                _unlockedCountries.value = repository.loadUnlockedCountryCodes()
                _unlockedCountryKm.value = repository.loadUnlockedCountryKmByCode()
            } finally {
                _userRefreshing.value = false
            }
        }
    }
}
