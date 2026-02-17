package com.example.jlg_czg_sicenet.ui

import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.jlg_czg_sicenet.ui.screens.LoginScreen
import com.example.jlg_czg_sicenet.ui.screens.LoginUiState
import com.example.jlg_czg_sicenet.ui.screens.LoginViewModel
import com.example.jlg_czg_sicenet.ui.screens.ProfileScreen
import com.example.jlg_czg_sicenet.ui.screens.ProfileUiState
import com.example.jlg_czg_sicenet.ui.screens.ProfileViewModel

@Composable
fun JLGSICENETApp() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
            
            LoginScreen(
                loginUiState = loginViewModel.loginUiState,
                matricula = loginViewModel.matricula,
                contrasenia = loginViewModel.contrasenia,
                onMatriculaChange = { loginViewModel.updateMatricula(it) },
                onContraseniaChange = { loginViewModel.updateContrasenia(it) },
                onLoginClick = { loginViewModel.login() },
                onLoginSuccess = { matricula ->
                    navController.navigate("profile/$matricula") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onResetForm = {
                    loginViewModel.resetState()
                    loginViewModel.updateMatricula("")
                    loginViewModel.updateContrasenia("")
                }
            )
        }
        
        composable("profile/{matricula}") { backStackEntry ->
            val matricula = backStackEntry.arguments?.getString("matricula") ?: ""
            val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)
            
            ProfileScreen(
                profileUiState = profileViewModel.profileUiState,
                onLogoutClick = {
                    profileViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onLoadProfile = { profileViewModel.loadProfile(it) },
                matricula = matricula,
                onNavigateToAcademicLoad = { navController.navigate("academic_load/$matricula") },
                onNavigateToKardex = { navController.navigate("kardex/$matricula") },
                onNavigateToGradesByUnit = { navController.navigate("grades_by_unit/$matricula") },
                onNavigateToFinalGrades = { navController.navigate("final_grades/$matricula") }
            )
        }

        composable("academic_load/{matricula}") { backStackEntry ->
            val matricula = backStackEntry.arguments?.getString("matricula") ?: ""
            AcademicLoadScreen(
                navController = navController,
                matricula = matricula
            )
        }

        composable("kardex/{matricula}") { backStackEntry ->
            val matricula = backStackEntry.arguments?.getString("matricula") ?: ""
            KardexScreen(
                navController = navController,
                matricula = matricula
            )
        }

        composable("grades_by_unit/{matricula}") { backStackEntry ->
            val matricula = backStackEntry.arguments?.getString("matricula") ?: ""
            GradesByUnitScreen(
                navController = navController,
                matricula = matricula
            )
        }

        composable("final_grades/{matricula}") { backStackEntry ->
            val matricula = backStackEntry.arguments?.getString("matricula") ?: ""
            FinalGradesScreen(
                navController = navController,
                matricula = matricula
            )
        }
    }
}
