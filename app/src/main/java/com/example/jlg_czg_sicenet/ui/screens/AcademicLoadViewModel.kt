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

sealed interface AcademicLoadUiState {
    object Idle : AcademicLoadUiState
    object Loading : AcademicLoadUiState
    data class Success(val data: String, val lastUpdate: String?) : AcademicLoadUiState
    data class Error(val message: String) : AcademicLoadUiState
}

class AcademicLoadViewModel(
    private val localRepository: LocalRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    var academicLoadUiState: AcademicLoadUiState by mutableStateOf(AcademicLoadUiState.Idle)
        private set

    private val _syncStatus = MutableStateFlow<WorkInfo?>(null)
    val syncStatus: StateFlow<WorkInfo?> = _syncStatus.asStateFlow()

    fun loadAcademicLoad(matricula: String) {
        viewModelScope.launch(Dispatchers.IO) {
            academicLoadUiState = AcademicLoadUiState.Loading

            try {
                val localData = localRepository.getAcademicLoad(matricula)
                val lastUpdate = localRepository.getAcademicLoadLastUpdate(matricula)

                if (!localData.isNullOrEmpty()) {
                    val formattedDate = lastUpdate?.let { formatDate(it) }
                    academicLoadUiState = AcademicLoadUiState.Success(localData, formattedDate)
                } else {
                    syncAcademicLoad(matricula)
                }
            } catch (e: Exception) {
                academicLoadUiState = AcademicLoadUiState.Error("Error loading data: ${e.message}")
            }
        }
    }

    fun syncAcademicLoad(matricula: String) {
        viewModelScope.launch(Dispatchers.IO) {
            academicLoadUiState = AcademicLoadUiState.Loading

            try {
                syncManager.scheduleAcademicDataSync(matricula, SyncAcademicDataWorker.DATA_TYPE_ACADEMIC_LOAD)

                syncManager.getSyncStatus(matricula, SyncAcademicDataWorker.DATA_TYPE_ACADEMIC_LOAD)
                    .collect { workInfo ->
                        _syncStatus.value = workInfo

                        when (workInfo?.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                loadAcademicLoad(matricula)
                            }
                            WorkInfo.State.FAILED -> {
                                academicLoadUiState = AcademicLoadUiState.Error("Sync failed")
                            }
                            else -> {}
                        }
                    }
            } catch (e: Exception) {
                academicLoadUiState = AcademicLoadUiState.Error("Error syncing data: ${e.message}")
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
                AcademicLoadViewModel(
                    localRepository = application.container.localRepository,
                    syncManager = application.container.syncManager
                )
            }
        }
    }
}