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

sealed interface FinalGradesUiState {
    object Idle : FinalGradesUiState
    object Loading : FinalGradesUiState
    data class Success(val data: String, val lastUpdate: String?) : FinalGradesUiState
    data class Error(val message: String) : FinalGradesUiState
}

class FinalGradesViewModel(
    private val localRepository: LocalRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    var finalGradesUiState: FinalGradesUiState by mutableStateOf(FinalGradesUiState.Idle)
        private set

    private val _syncStatus = MutableStateFlow<WorkInfo?>(null)
    val syncStatus: StateFlow<WorkInfo?> = _syncStatus.asStateFlow()

    fun loadFinalGrades(matricula: String) {
        viewModelScope.launch(Dispatchers.IO) {
            finalGradesUiState = FinalGradesUiState.Loading

            try {
                val localData = localRepository.getFinalGrades(matricula)
                val lastUpdate = localRepository.getFinalGradesLastUpdate(matricula)

                if (!localData.isNullOrEmpty()) {
                    val formattedDate = lastUpdate?.let { formatDate(it) }
                    finalGradesUiState = FinalGradesUiState.Success(localData, formattedDate)
                } else {
                    syncFinalGrades(matricula)
                }
            } catch (e: Exception) {
                finalGradesUiState = FinalGradesUiState.Error("Error loading data: ${e.message}")
            }
        }
    }

    fun syncFinalGrades(matricula: String) {
        viewModelScope.launch(Dispatchers.IO) {
            finalGradesUiState = FinalGradesUiState.Loading

            try {
                syncManager.scheduleAcademicDataSync(matricula, SyncAcademicDataWorker.DATA_TYPE_FINAL_GRADES)

                syncManager.getSyncStatus(matricula, SyncAcademicDataWorker.DATA_TYPE_FINAL_GRADES)
                    .collect { workInfo ->
                        _syncStatus.value = workInfo

                        when (workInfo?.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                loadFinalGrades(matricula)
                            }
                            WorkInfo.State.FAILED -> {
                                finalGradesUiState = FinalGradesUiState.Error("Sync failed")
                            }
                            else -> {}
                        }
                    }
            } catch (e: Exception) {
                finalGradesUiState = FinalGradesUiState.Error("Error syncing data: ${e.message}")
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
                FinalGradesViewModel(
                    localRepository = application.container.localRepository,
                    syncManager = application.container.syncManager
                )
            }
        }
    }
}