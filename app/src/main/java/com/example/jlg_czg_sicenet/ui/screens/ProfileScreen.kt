package com.example.jlg_czg_sicenet.ui.screens

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jlg_czg_sicenet.model.ProfileStudent

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
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Información Personal
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Información Personal", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Divider()
                    ProfileInfoRow(label = "Matrícula", value = profile.matricula)
                    ProfileInfoRow(label = "Nombre", value = profile.nombre)
                }
            }

            // Información Académica
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Información Académica", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Divider()
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
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.4f))
        Text(text = value, modifier = Modifier.weight(0.6f))
    }
}

@Composable
private fun ProfileInfoRowExpandible(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold)
        Text(text = value, modifier = Modifier.fillMaxWidth())
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
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Error", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(error, modifier = Modifier.padding(vertical = 16.dp))
        Button(onClick = onBackClick) {
            Text("Atrás")
        }
    }
}


