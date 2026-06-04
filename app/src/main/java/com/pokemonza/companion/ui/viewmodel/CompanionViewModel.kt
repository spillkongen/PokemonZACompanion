package com.pokemonza.companion.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pokemonza.companion.data.model.FashionItem
import com.pokemonza.companion.data.model.GuideEntry
import com.pokemonza.companion.data.model.MissionEntry
import com.pokemonza.companion.data.model.PokemonEntry
import com.pokemonza.companion.data.model.TabLoadState
import com.pokemonza.companion.data.network.FashionRepository
import com.pokemonza.companion.data.repository.GuideRepository
import com.pokemonza.companion.data.network.MissionRepository
import com.pokemonza.companion.data.repository.PokemonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CompanionViewModel(application: Application) : AndroidViewModel(application) {

    private val pokemonRepo = PokemonRepository(application)
    private val fashionRepo = FashionRepository(application)
    private val missionRepo = MissionRepository(application)
    private val guideRepo = GuideRepository()

    private val _pokemonState = MutableStateFlow(TabLoadState<PokemonEntry>())
    val pokemonState: StateFlow<TabLoadState<PokemonEntry>> = _pokemonState.asStateFlow()

    private val _fashionState = MutableStateFlow(TabLoadState<FashionItem>())
    val fashionState: StateFlow<TabLoadState<FashionItem>> = _fashionState.asStateFlow()

    private val _missionState = MutableStateFlow(TabLoadState<MissionEntry>())
    val missionState: StateFlow<TabLoadState<MissionEntry>> = _missionState.asStateFlow()

    private val _guideState = MutableStateFlow(TabLoadState<GuideEntry>())
    val guideState: StateFlow<TabLoadState<GuideEntry>> = _guideState.asStateFlow()

    private val _pokemonDetail = MutableStateFlow<String?>(null)
    val pokemonDetail: StateFlow<String?> = _pokemonDetail.asStateFlow()

    private val _missionDetail = MutableStateFlow<String?>(null)
    val missionDetail: StateFlow<String?> = _missionDetail.asStateFlow()

    private var pokemonLoaded = false
    private var fashionLoaded = false
    private var missionsLoaded = false
    private var guidesLoaded = false

    fun loadPokemon(forceRefresh: Boolean = false) {
        if (pokemonLoaded && !forceRefresh) return
        viewModelScope.launch {
            _pokemonState.value = _pokemonState.value.copy(isLoading = true, error = null)
            try {
                if (forceRefresh) pokemonRepo.clearCache()
                val data = pokemonRepo.fetchPokemon()
                _pokemonState.value = TabLoadState(data = data, lastUpdated = System.currentTimeMillis())
                pokemonLoaded = true
            } catch (e: Exception) {
                _pokemonState.value = _pokemonState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun loadFashion(forceRefresh: Boolean = false) {
        if (fashionLoaded && !forceRefresh) return
        viewModelScope.launch {
            _fashionState.value = _fashionState.value.copy(isLoading = true, error = null)
            try {
                if (forceRefresh) fashionRepo.clearCache()
                val data = fashionRepo.fetchFashion()
                _fashionState.value = TabLoadState(data = data, lastUpdated = System.currentTimeMillis())
                fashionLoaded = true
            } catch (e: Exception) {
                _fashionState.value = _fashionState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun loadMissions(forceRefresh: Boolean = false) {
        if (missionsLoaded && !forceRefresh) return
        viewModelScope.launch {
            _missionState.value = _missionState.value.copy(isLoading = true, error = null)
            try {
                if (forceRefresh) missionRepo.clearCache()
                val data = missionRepo.fetchMissions()
                _missionState.value = TabLoadState(data = data, lastUpdated = System.currentTimeMillis())
                missionsLoaded = true
            } catch (e: Exception) {
                _missionState.value = _missionState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun loadGuides(forceRefresh: Boolean = false) {
        if (guidesLoaded && !forceRefresh) return
        viewModelScope.launch {
            _guideState.value = _guideState.value.copy(isLoading = true, error = null)
            try {
                val data = guideRepo.fetchGuides()
                _guideState.value = TabLoadState(data = data, lastUpdated = System.currentTimeMillis())
                guidesLoaded = true
            } catch (e: Exception) {
                _guideState.value = _guideState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun loadPokemonDetail(entry: PokemonEntry) {
        _pokemonDetail.value = pokemonRepo.fetchPokemonDetail(entry)
    }

    fun clearPokemonDetail() {
        _pokemonDetail.value = null
    }

    fun loadMissionDetail(mission: MissionEntry) {
        viewModelScope.launch {
            _missionDetail.value = "Loading guide..."
            _missionDetail.value = missionRepo.fetchMissionGuide(mission)
        }
    }

    fun clearMissionDetail() {
        _missionDetail.value = null
    }
}
