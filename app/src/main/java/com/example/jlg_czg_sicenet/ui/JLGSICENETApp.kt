package com.example.jlg_czg_sicenet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.jlg_czg_sicenet.ui.screens.*
import kotlinx.coroutines.launch

@Composable
fun JLGSICENETApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // El Drawer solo debe mostrarse si NO estamos en la pantalla de login
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "login"
    val isLoginScreen = currentRoute == "login"

    if (isLoginScreen) {
        LoginFlow(navController)
    } else {
        MainFlow(navController, drawerState, scope, currentRoute)
    }
}

@Composable
fun LoginFlow(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
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
                    navController.navigate("main/$matricula") {
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
        // Ruta para ir al main
        composable("main/{matricula}") { backStackEntry ->
            val matricula = backStackEntry.arguments?.getString("matricula") ?: ""
            // Esta ruta es solo un puente
        }
    }
}

@Composable
fun MainFlow(
    navController: NavHostController,
    drawerState: DrawerState,
    scope: kotlinx.coroutines.CoroutineScope,
    currentRoute: String
) {
    val matriculaParam = currentRoute.split("/").lastOrNull() ?: ""
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Sicenet Menu", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Mi Perfil") },
                    selected = currentRoute.startsWith("profile"),
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("profile/$matriculaParam") {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Carga Académica") },
                    selected = currentRoute.startsWith("carga"),
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("carga/$matriculaParam") {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Kardex") },
                    selected = currentRoute.startsWith("kardex"),
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("kardex/$matriculaParam") {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.List, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Calificaciones Unidad") },
                    selected = currentRoute.startsWith("unidades"),
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("unidades/$matriculaParam") {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Calificación Final") },
                    selected = currentRoute.startsWith("final"),
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("final/$matriculaParam") {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) }
                )
                Spacer(modifier = Modifier.weight(1f))
                NavigationDrawerItem(
                    label = { Text("Cerrar Sesión") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) }
                )
            }
        }
    ) {
        val academicViewModel: AcademicViewModel = viewModel(factory = AcademicViewModel.Factory)
        
        NavHost(navController = navController, startDestination = "profile/$matriculaParam") {
            composable("profile/{matricula}") { backStackEntry ->
                val m = backStackEntry.arguments?.getString("matricula") ?: ""
                val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)
                ProfileScreen(
                    profileUiState = profileViewModel.profileUiState,
                    onLogoutClick = { scope.launch { drawerState.open() } }, // Cambiado para abrir menu
                    onLoadProfile = { profileViewModel.loadProfile(it) },
                    matricula = m
                )
            }
            
            composable("carga/{matricula}") { backStackEntry ->
                val m = backStackEntry.arguments?.getString("matricula") ?: ""
                AcademicDataScreen("Carga Académica", m, "CARGA", academicViewModel) {
                    scope.launch { drawerState.open() }
                }
            }
            
            composable("kardex/{matricula}") { backStackEntry ->
                val m = backStackEntry.arguments?.getString("matricula") ?: ""
                AcademicDataScreen("Kardex", m, "KARDEX", academicViewModel) {
                    scope.launch { drawerState.open() }
                }
            }
            
            composable("unidades/{matricula}") { backStackEntry ->
                val m = backStackEntry.arguments?.getString("matricula") ?: ""
                AcademicDataScreen("Calificaciones Unidad", m, "UNIDADES", academicViewModel) {
                    scope.launch { drawerState.open() }
                }
            }
            
            composable("final/{matricula}") { backStackEntry ->
                val m = backStackEntry.arguments?.getString("matricula") ?: ""
                AcademicDataScreen("Calificación Final", m, "FINAL", academicViewModel) {
                    scope.launch { drawerState.open() }
                }
            }
            
            composable("login") {
                // Se maneja afuera
            }
        }
    }
}
