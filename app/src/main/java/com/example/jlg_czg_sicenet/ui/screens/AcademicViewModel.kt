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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    // Timestamps de última actualización (se actualizan al finalizar cada carga)
    private val _lastFetchedCarga = MutableStateFlow<Long?>(null)
    val lastFetchedCarga: StateFlow<Long?> = _lastFetchedCarga.asStateFlow()

    private val _lastFetchedKardex = MutableStateFlow<Long?>(null)
    val lastFetchedKardex: StateFlow<Long?> = _lastFetchedKardex.asStateFlow()

    private val _lastFetchedUnidades = MutableStateFlow<Long?>(null)
    val lastFetchedUnidades: StateFlow<Long?> = _lastFetchedUnidades.asStateFlow()

    private val _lastFetchedFinales = MutableStateFlow<Long?>(null)
    val lastFetchedFinales: StateFlow<Long?> = _lastFetchedFinales.asStateFlow()

    private val _flowsCache = mutableMapOf<String, StateFlow<Any?>>()

    private fun normalize(m: String) = m.trim().uppercase()

    // Funciones para obtener los estados de los datos académicos

    @Suppress("UNCHECKED_CAST")
    fun getCargaFlow(matricula: String): StateFlow<List<MateriaCarga>> {
        val m = normalize(matricula)
        return _flowsCache.getOrPut("CARGA_$m") {
            snRepository.getCargaFlow(m)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        } as StateFlow<List<MateriaCarga>>
    }

    // Funciones para obtener los estados de los datos académicos
    @Suppress("UNCHECKED_CAST")
    fun getKardexFlow(matricula: String): StateFlow<KardexModel?> {
        val m = normalize(matricula)
        return _flowsCache.getOrPut("KARDEX_$m") {
            snRepository.getKardexFlow(m)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        } as StateFlow<KardexModel?>
    }

    // Funciones para obtener los estados de los datos académicos
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

    private fun isInvalid(m: String) = m.isEmpty() || m.contains("{") || m.contains("}")

    // Funciones para cargar los datos de la API y actualizar los estados
    fun loadCarga(matricula: String) {
        val m = normalize(matricula)
        if (isInvalid(m)) return
        viewModelScope.launch { // Esto se dispara en LaunchedEffect al entrar a la pantalla, y también cuando el usuario toca el botón de recargar
            if (cargaUiState is AcademicUiState.Loading) return@launch
            cargaUiState = AcademicUiState.Loading
            // Llamada a la API y actualización del estado
            try {
                snRepository.getCargaAcademica(m) // API -> guarda carga en ROOM
                cargaUiState = AcademicUiState.Success(Unit) // Carga exitosa
            } catch (e: Exception) {
                Log.e("AcademicViewModel", "Error cargando carga ($m): ${e.message}")
                cargaUiState = AcademicUiState.Error(e.message ?: "Error desconocido")
            } finally {
                _lastFetchedCarga.value = System.currentTimeMillis()
            }
        }
    }

    fun loadKardex(matricula: String) {
        val m = normalize(matricula)
        if (isInvalid(m)) return
        viewModelScope.launch {
            if (kardexUiState is AcademicUiState.Loading) return@launch
            kardexUiState = AcademicUiState.Loading
            try {
                snRepository.getKardex(m)
                kardexUiState = AcademicUiState.Success(Unit)
            } catch (e: Exception) {
                Log.e("AcademicViewModel", "Error cargando kardex ($m): ${e.message}")
                kardexUiState = AcademicUiState.Error(e.message ?: "Error desconocido")
            } finally {
                _lastFetchedKardex.value = System.currentTimeMillis()
            }
        }
    }

    fun loadUnidades(matricula: String) {
        val m = normalize(matricula)
        if (isInvalid(m)) return
        viewModelScope.launch {
            if (unidadesUiState is AcademicUiState.Loading) return@launch
            unidadesUiState = AcademicUiState.Loading
            try {
                snRepository.getUnidades(m)
                unidadesUiState = AcademicUiState.Success(Unit)
            } catch (e: Exception) {
                Log.e("AcademicViewModel", "Error cargando unidades ($m): ${e.message}")
                unidadesUiState = AcademicUiState.Error(e.message ?: "Error desconocido")
            } finally {
                _lastFetchedUnidades.value = System.currentTimeMillis()
            }
        }
    }

    fun loadFinales(matricula: String) {
        val m = normalize(matricula)
        if (isInvalid(m)) return
        viewModelScope.launch {
            if (finalesUiState is AcademicUiState.Loading) return@launch
            finalesUiState = AcademicUiState.Loading
            try {
                snRepository.getFinales(m)
                finalesUiState = AcademicUiState.Success(Unit)
            } catch (e: Exception) {
                Log.e("AcademicViewModel", "Error cargando finales ($m): ${e.message}")
                finalesUiState = AcademicUiState.Error(e.message ?: "Error desconocido")
            } finally {
                _lastFetchedFinales.value = System.currentTimeMillis()
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
