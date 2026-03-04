package com.example.jlg_czg_sicenet.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.jlg_czg_sicenet.JLGSICENETApplication
import com.example.jlg_czg_sicenet.data.SNRepository
import com.example.jlg_czg_sicenet.ui.screens.*
import kotlinx.coroutines.launch

@Composable
fun JLGSICENETApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "login"

    //val isLoginScreen = currentRoute == "login"
    val showDrawer = currentRoute != "login" && currentRoute != "startup"

    val context = LocalContext.current
    val app = context.applicationContext as JLGSICENETApplication
    val repository = app.container.snRepository

    val matriculaParam = currentRoute.split("/").lastOrNull() ?: ""

    if (showDrawer) {

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
                            repository.clearSession()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) }
                    )
                }
            }
        ) {
            AppNavHost(navController, repository)
        }
    } else {
        AppNavHost(navController, repository)
    }

}

@Composable
fun AppNavHost(
    navController: NavHostController,
    repository: SNRepository
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    NavHost(
        navController = navController,
        startDestination = "startup"
    ) {
        composable("startup") {
            StartupScreen(repository, navController)
        }
        composable("login") {
            LoginFlow(navController)
        }
        composable("profile/{matricula}") { backStackEntry ->
            val m = backStackEntry.arguments?.getString("matricula") ?: ""
            val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)
            ProfileScreen(
                profileUiState = profileViewModel.profileUiState,
                onLogoutClick = { scope.launch { drawerState.open() } },
                onLoadProfile = { profileViewModel.loadProfile(it) },
                matricula = m
            )
        }

        composable("carga/{matricula}") { backStackEntry ->
            val academicViewModel: AcademicViewModel = viewModel(factory = AcademicViewModel.Factory)
            val m = backStackEntry.arguments?.getString("matricula") ?: ""
            AcademicDataScreen("Carga Académica", m, "CARGA", academicViewModel) {
                scope.launch { drawerState.open() }
            }
        }

        composable("kardex/{matricula}") { backStackEntry ->
            val academicViewModel: AcademicViewModel = viewModel(factory = AcademicViewModel.Factory)
            val m = backStackEntry.arguments?.getString("matricula") ?: ""
            AcademicDataScreen("Kardex", m, "KARDEX", academicViewModel) {
                scope.launch { drawerState.open() }
            }
        }

        composable("unidades/{matricula}") { backStackEntry ->
            val academicViewModel: AcademicViewModel = viewModel(factory = AcademicViewModel.Factory)
            val m = backStackEntry.arguments?.getString("matricula") ?: ""
            AcademicDataScreen("Calificaciones Unidad", m, "UNIDADES", academicViewModel) {
                scope.launch { drawerState.open() }
            }
        }

        composable("final/{matricula}") { backStackEntry ->
            val academicViewModel: AcademicViewModel = viewModel(factory = AcademicViewModel.Factory)
            val m = backStackEntry.arguments?.getString("matricula") ?: ""
            AcademicDataScreen("Calificación Final", m, "FINAL", academicViewModel) {
                scope.launch { drawerState.open() }
            }
        }
    }
}

@Composable
fun StartupScreen(
    repository: SNRepository,
    navController: NavHostController
) {

    LaunchedEffect(Unit) {
        val saved = repository.isSessionSaved()
        Log.d("SESSION_DEBUG", "isSessionSaved = $saved")

        if (saved) {
            val valid = repository.validateSession()
            Log.d("SESSION_DEBUG", "validateSession = $valid")
            if (valid) {

                val matricula = repository.getSavedMatricula()
                Log.d("SESSION_DEBUG", "matricula = $matricula")

                /*
                navController.navigate("profile/$matricula") {
                    popUpTo("login") { inclusive = true }
                }
                 */
                navController.navigate("profile/$matricula") {
                    popUpTo("startup") { inclusive = true }
                }

            } else {
                navController.navigate("login") {
                    popUpTo("startup") { inclusive = true }
                }
            }

        } else {
            navController.navigate("login") {
                popUpTo("startup") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun LoginFlow(navController: NavHostController) {
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
                popUpTo("startup") { inclusive = true }
            }
        },
        onResetForm = {
            loginViewModel.resetState()
            loginViewModel.updateMatricula("")
            loginViewModel.updateContrasenia("")
        }
    )
}

@Composable
fun MainFlowWrapper(
    navController: NavHostController,
    matricula: String
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    MainFlow(
        navController = navController,
        drawerState = drawerState,
        scope = scope,
        currentRoute = "profile/$matricula"
    )
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

    }
}
