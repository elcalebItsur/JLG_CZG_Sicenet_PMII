package com.example.jlg_czg_sicenet.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.work.WorkInfo
import com.example.jlg_czg_sicenet.ui.theme.SicenetColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicLoadScreen(
    navController: NavController,
    matricula: String,
    viewModel: AcademicLoadViewModel = viewModel(factory = AcademicLoadViewModel.Factory)
) {
    val uiState = viewModel.academicLoadUiState
    val syncStatus by viewModel.syncStatus.collectAsState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadAcademicLoad(matricula)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Carga Académica") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.syncAcademicLoad(matricula) },
                        enabled = syncStatus?.state != WorkInfo.State.RUNNING
                    ) {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = "Sync",
                            tint = if (syncStatus?.state == WorkInfo.State.RUNNING)
                                Color.Gray else SicenetColors.Primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SicenetColors.Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is AcademicLoadUiState.Idle -> {
                    // Show nothing initially
                }
                is AcademicLoadUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = SicenetColors.Primary
                    )
                }
                is AcademicLoadUiState.Success -> {
                    AcademicLoadContent(
                        data = uiState.data,
                        lastUpdate = uiState.lastUpdate
                    )
                }
                is AcademicLoadUiState.Error -> {
                    ErrorContent(
                        message = uiState.message,
                        onRetry = { viewModel.loadAcademicLoad(matricula) }
                    )
                }
            }

            if (syncStatus?.state == WorkInfo.State.RUNNING) {
                SyncOverlay()
            }
        }
    }
}

@Composable
private fun AcademicLoadContent(data: String, lastUpdate: String?) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = SicenetColors.Secondary
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Carga Académica",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    lastUpdate?.let {
                        Text(
                            text = "Última actualización: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Datos de Carga Académica",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SicenetColors.Primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = data,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Red,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = SicenetColors.Primary
            )
        ) {
            Text("Reintentar", color = Color.White)
        }
    }
}

@Composable
private fun SyncOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = SicenetColors.Primary.copy(alpha = 0.9f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.width(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sincronizando datos...",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}