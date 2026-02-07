package com.example.jlg_czg_sicenet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jlg_czg_sicenet.model.ProfileStudent

// Colores personalizados
private val DarkBlue = Color(0xFF1B396A)
private val CoolGray = Color(0xFF807E82)
private val DarkText = Color(0xFF000000)
private val LightBackground = Color(0xFFF5F5F5)

@Composable
fun ProfileScreen(
    profileUiState: ProfileUiState,
    onBackClick: () -> Unit,
    onLoadProfile: (String) -> Unit,
    matricula: String,
    modifier: Modifier = Modifier
) {
    when (profileUiState) {
        is ProfileUiState.Idle -> {
            LaunchedEffect(Unit) {
                onLoadProfile(matricula)
            }
            LoadingScreen(modifier = modifier)
        }
        is ProfileUiState.Loading -> {
            LoadingScreen(modifier = modifier)
        }
        is ProfileUiState.Success -> {
            ProfileDetailScreen(
                profile = profileUiState.profile,
                onBackClick = onBackClick,
                modifier = modifier
            )
        }
        is ProfileUiState.Error -> {
            ErrorScreen(
                error = profileUiState.message,
                onBackClick = onBackClick,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun LoadingScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Text("Cargando perfil...", modifier = Modifier.padding(top = 16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileDetailScreen(
    profile: ProfileStudent,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBlue)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Información Personal
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Información Personal",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkBlue, shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                            .padding(2.dp)
                    ) {}
                    ProfileInfoRow(label = "Matrícula", value = profile.matricula)
                    ProfileInfoRow(label = "Nombre", value = profile.nombre)
                }
            }

            // Información Académica
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Información Académica",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkBlue, shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                            .padding(2.dp)
                    ) {}
                    ProfileInfoRow(label = "Carrera", value = profile.carrera)
                    ProfileInfoRow(label = "Semestre Actual", value = profile.semActual)
                    ProfileInfoRowExpandible(label = "Especialidad", value = profile.especialidad)
                    ProfileInfoRow(label = "Créditos Acumulados", value = profile.cdtsReunidos)
                    ProfileInfoRow(label = "Créditos Actuales", value = profile.cdtsActuales)
                    ProfileInfoRow(label = "Inscrito", value = convertBooleanToText(profile.inscrito))
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            color = CoolGray,
            modifier = Modifier.weight(0.4f),
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = DarkText,
            modifier = Modifier.weight(0.6f),
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun ProfileInfoRowExpandible(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            color = CoolGray,
            fontSize = 14.sp
        )
        Text(
            text = value,
            modifier = Modifier.fillMaxWidth(),
            color = DarkText,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

private fun convertBooleanToText(value: String): String {
    return when (value.lowercase()) {
        "true" -> "Si"
        "false" -> "No"
        else -> value
    }
}

@Composable
private fun ErrorScreen(
    error: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Error",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DarkBlue
        )
        Text(
            error,
            modifier = Modifier.padding(vertical = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = CoolGray
        )
        Button(
            onClick = onBackClick,
            colors = ButtonDefaults.buttonColors(containerColor = DarkBlue)
        ) {
            Text("Atrás", color = Color.White)
        }
    }
}


