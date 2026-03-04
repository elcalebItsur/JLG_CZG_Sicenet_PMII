import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.jlg_czg_sicenet.JLGSICENETApplication
import com.example.jlg_czg_sicenet.data.SNRepository

class StartupViewModel(
    private val repository: SNRepository
) : ViewModel() {

    suspend fun checkSession(): StartupResult {

        if (repository.isSessionSaved()) {

            val valid = repository.validateSession()

            return if (valid) {
                val matricula = repository.getSavedMatricula()
                StartupResult.Authenticated(matricula ?: "")
            } else {
                StartupResult.NotAuthenticated
            }

        } else {
            return StartupResult.NotAuthenticated
        }
    }
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                            as JLGSICENETApplication)

                StartupViewModel(
                    repository = application.container.snRepository
                )
            }
        }
    }
}

sealed class StartupResult {
    data class Authenticated(val matricula: String) : StartupResult()
    object NotAuthenticated : StartupResult()
}