package com.example.jlg_czg_sicenet.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.jlg_czg_sicenet.JLGSICENETApplication
import com.example.jlg_czg_sicenet.data.SNRepository
import com.example.jlg_czg_sicenet.model.ProfileStudent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface ProfileUiState {
    object Idle : ProfileUiState
    object Loading : ProfileUiState
    data class Success(val profile: ProfileStudent) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

class ProfileViewModel(private val snRepository: SNRepository) : ViewModel() {

    // Estado de red (sincronización activa)
    private val _syncState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val syncState: StateFlow<ProfileUiState> = _syncState

    // Cache de flows por matrícula para evitar recreación en recomposición
    private val _flowsCache = mutableMapOf<String, StateFlow<ProfileStudent?>>()

    private fun normalize(m: String) = m.trim().uppercase()

    /**
     * Retorna un StateFlow que observa Room en tiempo real.
     * Se actualiza automáticamente cuando el Worker guarda datos.
     */
    fun getProfileFlow(matricula: String): StateFlow<ProfileStudent?> {
        val m = normalize(matricula)
        return _flowsCache.getOrPut(m) {
            snRepository.getProfileFlow(m)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        }
    }

    /**
     * Dispara una sincronización con la red.
     * Si hay internet: obtiene datos frescos y los guarda en Room.
     * Si no hay internet: Room ya tiene los datos del Worker anterior.
     */
    fun loadProfile(matricula: String) {
        val m = normalize(matricula)
        if (m.isEmpty() || m.contains("{") || m.contains("}")) {
            _syncState.value = ProfileUiState.Error("Matrícula inválida: $m")
            return
        }
        if (_syncState.value is ProfileUiState.Loading) return // evitar duplicados

        viewModelScope.launch {
            _syncState.value = ProfileUiState.Loading
            try {
                val profile = withContext(Dispatchers.IO) {
                    snRepository.profile(m)
                }
                // Si la respuesta tiene nombre, significa que la red funcionó y guardó en Room
                if (profile.nombre.isNotEmpty() || profile.carrera.isNotEmpty()) {
                    Log.d("ProfileViewModel", "Perfil cargado de red para $m")
                    _syncState.value = ProfileUiState.Success(profile)
                } else {
                    // La red respondió vacío, usamos lo que tenga Room (el Flow lo maneja)
                    Log.d("ProfileViewModel", "Respuesta vacía de red para $m, usando Room")
                    _syncState.value = ProfileUiState.Idle
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error de red para $m: ${e.message}")
                // No mostramos error si Room tiene datos (el Flow lo resuelve)
                _syncState.value = ProfileUiState.Idle
            }
        }
    }

    fun logout() {
        snRepository.logout()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as JLGSICENETApplication)
                ProfileViewModel(snRepository = application.container.snRepository)
            }
        }
    }
}
