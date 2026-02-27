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
import com.example.jlg_czg_sicenet.data.local.AcademicDataEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AcademicUiState {
    object Idle : AcademicUiState
    object Loading : AcademicUiState
    data class Success(val data: String) : AcademicUiState
    data class Error(val message: String) : AcademicUiState
}

class AcademicViewModel(private val snRepository: SNRepository) : ViewModel() {

    var cargaUiState: AcademicUiState by mutableStateOf(AcademicUiState.Idle)
        private set

    var kardexUiState: AcademicUiState by mutableStateOf(AcademicUiState.Idle)
        private set

    var unidadesUiState: AcademicUiState by mutableStateOf(AcademicUiState.Idle)
        private set

    var finalesUiState: AcademicUiState by mutableStateOf(AcademicUiState.Idle)
        private set

    fun getAcademicDataFlow(matricula: String, type: String): StateFlow<AcademicDataEntity?> {
        return snRepository.getAcademicDataFlow(matricula, type)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    fun loadCarga(matricula: String) {
        viewModelScope.launch {
            cargaUiState = AcademicUiState.Loading
            try {
                val res = snRepository.getCargaAcademica(matricula)
                cargaUiState = AcademicUiState.Success(res)
            } catch (e: Exception) {
                cargaUiState = AcademicUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun loadKardex(matricula: String) {
        viewModelScope.launch {
            kardexUiState = AcademicUiState.Loading
            try {
                val res = snRepository.getKardex(matricula)
                kardexUiState = AcademicUiState.Success(res)
            } catch (e: Exception) {
                kardexUiState = AcademicUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun loadUnidades(matricula: String) {
        viewModelScope.launch {
            unidadesUiState = AcademicUiState.Loading
            try {
                val res = snRepository.getUnidades(matricula)
                unidadesUiState = AcademicUiState.Success(res)
            } catch (e: Exception) {
                unidadesUiState = AcademicUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun loadFinales(matricula: String) {
        viewModelScope.launch {
            finalesUiState = AcademicUiState.Loading
            try {
                val res = snRepository.getFinales(matricula)
                finalesUiState = AcademicUiState.Success(res)
            } catch (e: Exception) {
                finalesUiState = AcademicUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as JLGSICENETApplication)
                AcademicViewModel(snRepository = application.container.snRepository)
            }
        }
    }
}
