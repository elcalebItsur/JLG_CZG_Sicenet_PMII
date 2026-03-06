import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.jlg_czg_sicenet.JLGSICENETApplication
import com.example.jlg_czg_sicenet.data.SNRepository

class StartupViewModel( // es para iniciar la sesión
    private val repository: SNRepository
) : ViewModel() {

    suspend fun checkSession(): StartupResult { // devuelve si hay una sesión activa o no

        if (repository.isSessionSaved()) { // si hay una sesión activa

            val valid = repository.validateSession() // valida la sesión

            return if (valid) { // si la sesión es válida
                val matricula = repository.getSavedMatricula() // obtiene la matrícula
                StartupResult.Authenticated(matricula ?: "") // devuelve la matrícula
            } else { // si la sesión no es válida
                StartupResult.NotAuthenticated // devuelve que no hay sesión
            }

        } else { // si no hay una sesión activa
            return StartupResult.NotAuthenticated // devuelve que no hay sesión
        }
    }
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory { // Factory para crear el ViewModel
            initializer { // Inicializador del ViewModel
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                            as JLGSICENETApplication) // obtiene la aplicación

                StartupViewModel( // crea el ViewModel
                    repository = application.container.snRepository // usa el repositorio de la aplicación y lo pasa al ViewModel
                )
            }
        }
    }
}

sealed class StartupResult {
    data class Authenticated(val matricula: String) : StartupResult()
    object NotAuthenticated : StartupResult()
}