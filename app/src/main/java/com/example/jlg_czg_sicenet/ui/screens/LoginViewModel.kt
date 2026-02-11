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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    data class Success(val matricula: String) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel(private val snRepository: SNRepository) : ViewModel() {
    
    var loginUiState: LoginUiState by mutableStateOf(LoginUiState.Idle)
        private set
    
    var matricula: String by mutableStateOf("")
        private set
    
    var contrasenia: String by mutableStateOf("")
        private set
    
    fun updateMatricula(newValue: String) {
        matricula = newValue
    }
    
    fun updateContrasenia(newValue: String) {
        contrasenia = newValue
    }

    fun resetState() {
        loginUiState = LoginUiState.Idle
    }
    
    fun login() {
        if (matricula.isBlank() || contrasenia.isBlank()) {
            loginUiState = LoginUiState.Error("Por favor ingresa matrícula y contraseña")
            return
        }
        
        viewModelScope.launch(Dispatchers.IO) {
            loginUiState = LoginUiState.Loading
            loginUiState = try {
                val success = snRepository.acceso(matricula, contrasenia)
                if (success) {
                    LoginUiState.Success(matricula)
                } else {
                    LoginUiState.Error("Matrícula o contraseña incorrecta")
                }
            } catch (e: Exception) {
                LoginUiState.Error("Error de conexión: ${e.message}")
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as JLGSICENETApplication)
                LoginViewModel(snRepository = application.container.snRepository)
            }
        }
    }
}
