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
import androidx.work.WorkInfo
import com.example.jlg_czg_sicenet.JLGSICENETApplication
import com.example.jlg_czg_sicenet.data.local.LocalRepository
import com.example.jlg_czg_sicenet.workers.SyncAcademicDataWorker
import com.example.jlg_czg_sicenet.workers.SyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface KardexUiState {
    object Idle : KardexUiState
    object Loading : KardexUiState
    data class Success(val data: String, val lastUpdate: String?) : KardexUiState
    data class Error(val message: String) : KardexUiState
}

class KardexViewModel(
    private val localRepository: LocalRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    var kardexUiState: KardexUiState by mutableStateOf(KardexUiState.Idle)
        private set

    private val _syncStatus = MutableStateFlow<WorkInfo?>(null)
    val syncStatus: StateFlow<WorkInfo?> = _syncStatus.asStateFlow()

    fun loadKardex(matricula: String) {
        viewModelScope.launch(Dispatchers.IO) {
            kardexUiState = KardexUiState.Loading

            try {
                val localData = localRepository.getKardex(matricula)
                val lastUpdate = localRepository.getKardexLastUpdate(matricula)

                if (!localData.isNullOrEmpty()) {
                    val formattedDate = lastUpdate?.let { formatDate(it) }
                    kardexUiState = KardexUiState.Success(localData, formattedDate)
                } else {
                    syncKardex(matricula)
                }
            } catch (e: Exception) {
                kardexUiState = KardexUiState.Error("Error loading data: ${e.message}")
            }
        }
    }

    fun syncKardex(matricula: String) {
        viewModelScope.launch(Dispatchers.IO) {
            kardexUiState = KardexUiState.Loading

            try {
                syncManager.scheduleAcademicDataSync(matricula, SyncAcademicDataWorker.DATA_TYPE_KARDEX)

                syncManager.getSyncStatus(matricula, SyncAcademicDataWorker.DATA_TYPE_KARDEX)
                    .collect { workInfo ->
                        _syncStatus.value = workInfo

                        when (workInfo?.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                loadKardex(matricula)
                            }
                            WorkInfo.State.FAILED -> {
                                kardexUiState = KardexUiState.Error("Sync failed")
                            }
                            else -> {}
                        }
                    }
            } catch (e: Exception) {
                kardexUiState = KardexUiState.Error("Error syncing data: ${e.message}")
            }
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as JLGSICENETApplication)
                KardexViewModel(
                    localRepository = application.container.localRepository,
                    syncManager = application.container.syncManager
                )
            }
        }
    }
}