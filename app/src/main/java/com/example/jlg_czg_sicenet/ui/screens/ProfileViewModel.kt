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
            profileUiState = try {
                val profile = withContext(Dispatchers.IO) {
                    snRepository.profile(matricula)
                }
                ProfileUiState.Success(profile)
            } catch (e: Exception) {
                ProfileUiState.Error("Error cargando perfil: ${e.message}")
            }
        }
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
