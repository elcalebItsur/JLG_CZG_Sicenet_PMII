package com.example.jlg_czg_sicenet.ui.screens

import android.util.Log
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
import com.example.jlg_czg_sicenet.model.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AcademicUiState<out T> {
    object Idle : AcademicUiState<Nothing>
    object Loading : AcademicUiState<Nothing>
    data class Success<T>(val data: T) : AcademicUiState<T>
    data class Error(val message: String) : AcademicUiState<Nothing>
}

class AcademicViewModel(private val snRepository: SNRepository) : ViewModel() {

    var cargaUiState: AcademicUiState<Unit> by mutableStateOf(AcademicUiState.Idle)
        private set

    var kardexUiState: AcademicUiState<Unit> by mutableStateOf(AcademicUiState.Idle)
        private set

    var unidadesUiState: AcademicUiState<Unit> by mutableStateOf(AcademicUiState.Idle)
        private set

    var finalesUiState: AcademicUiState<Unit> by mutableStateOf(AcademicUiState.Idle)
        private set

    private val _flowsCache = mutableMapOf<String, StateFlow<Any?>>()

    private fun normalize(m: String) = m.trim().uppercase()

    @Suppress("UNCHECKED_CAST")
    fun getCargaFlow(matricula: String): StateFlow<List<MateriaCarga>> {
        val m = normalize(matricula)
        return _flowsCache.getOrPut("CARGA_$m") {
            snRepository.getCargaFlow(m)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        } as StateFlow<List<MateriaCarga>>
    }

    @Suppress("UNCHECKED_CAST")
    fun getKardexFlow(matricula: String): StateFlow<KardexModel?> {
        val m = normalize(matricula)
        return _flowsCache.getOrPut("KARDEX_$m") {
            snRepository.getKardexFlow(m)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        } as StateFlow<KardexModel?>
    }

    @Suppress("UNCHECKED_CAST")
    fun getUnidadesFlow(matricula: String): StateFlow<List<CalificacionUnidad>> {
        val m = normalize(matricula)
        return _flowsCache.getOrPut("UNIDADES_$m") {
            snRepository.getUnidadesFlow(m)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        } as StateFlow<List<CalificacionUnidad>>
    }

    @Suppress("UNCHECKED_CAST")
    fun getFinalesFlow(matricula: String): StateFlow<List<CalificacionFinal>> {
        val m = normalize(matricula)
        return _flowsCache.getOrPut("FINAL_$m") {
            snRepository.getFinalesFlow(m)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        } as StateFlow<List<CalificacionFinal>>
    }

    @Suppress("UNCHECKED_CAST")
    fun getAcademicEntityFlow(matricula: String, type: String): StateFlow<AcademicDataEntity?> {
        val m = normalize(matricula)
        return _flowsCache.getOrPut("${type}_ENTITY_$m") {
            snRepository.getAcademicDataFlow(m, type)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        } as StateFlow<AcademicDataEntity?>
    }

    fun loadCarga(matricula: String) {
        val m = normalize(matricula)
        viewModelScope.launch {
            if (cargaUiState is AcademicUiState.Loading) return@launch
            cargaUiState = AcademicUiState.Loading
            try {
                snRepository.getCargaAcademica(m)
                cargaUiState = AcademicUiState.Success(Unit)
            } catch (e: Exception) {
                Log.e("AcademicViewModel", "Error cargando carga ($m): ${e.message}")
                cargaUiState = AcademicUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun loadKardex(matricula: String) {
        val m = normalize(matricula)
        viewModelScope.launch {
            if (kardexUiState is AcademicUiState.Loading) return@launch
            kardexUiState = AcademicUiState.Loading
            try {
                snRepository.getKardex(m)
                kardexUiState = AcademicUiState.Success(Unit)
            } catch (e: Exception) {
                Log.e("AcademicViewModel", "Error cargando kardex ($m): ${e.message}")
                kardexUiState = AcademicUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun loadUnidades(matricula: String) {
        val m = normalize(matricula)
        viewModelScope.launch {
            if (unidadesUiState is AcademicUiState.Loading) return@launch
            unidadesUiState = AcademicUiState.Loading
            try {
                snRepository.getUnidades(m)
                unidadesUiState = AcademicUiState.Success(Unit)
            } catch (e: Exception) {
                Log.e("AcademicViewModel", "Error cargando unidades ($m): ${e.message}")
                unidadesUiState = AcademicUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun loadFinales(matricula: String) {
        val m = normalize(matricula)
        viewModelScope.launch {
            if (finalesUiState is AcademicUiState.Loading) return@launch
            finalesUiState = AcademicUiState.Loading
            try {
                snRepository.getFinales(m)
                finalesUiState = AcademicUiState.Success(Unit)
            } catch (e: Exception) {
                Log.e("AcademicViewModel", "Error cargando finales ($m): ${e.message}")
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
