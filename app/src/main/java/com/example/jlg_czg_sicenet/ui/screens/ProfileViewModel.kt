package com.example.jlg_czg_sicenet.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface ProfileUiState {
    object Idle : ProfileUiState
    object Loading : ProfileUiState
    data class Success(val profile: ProfileStudent) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

class ProfileViewModel(private val snRepository: SNRepository) : ViewModel() {
    
    var profileUiState: ProfileUiState by mutableStateOf(ProfileUiState.Idle)
        private set

    fun loadProfile(matricula: String) {
        viewModelScope.launch {
            profileUiState = ProfileUiState.Loading
            try {
                // Primero mostrar cualquier perfil local guardado (si existe)
                val local = withContext(Dispatchers.IO) {
                    snRepository.getProfileFlow(matricula).first()
                }
                if (local != null) {
                    profileUiState = ProfileUiState.Success(local)
                }

                // Luego intentar actualizar desde la red y persistir el resultado
                val remote = withContext(Dispatchers.IO) {
                    snRepository.profile(matricula)
                }
                profileUiState = ProfileUiState.Success(remote)
            } catch (e: Exception) {
                // Si no había local, mostrar error; si había local, mantenerlo
                if (profileUiState is ProfileUiState.Success) {
                    // ya mostramos local
                } else {
                    profileUiState = ProfileUiState.Error("Error cargando perfil: ${e.message}")
                }
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
