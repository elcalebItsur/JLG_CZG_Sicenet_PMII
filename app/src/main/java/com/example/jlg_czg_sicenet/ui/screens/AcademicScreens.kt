package com.example.jlg_czg_sicenet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

private val DarkBlue = Color(0xFF1B396A)
private val LightBackground = Color(0xFFF5F5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicDataScreen(
    title: String,
    matricula: String,
    dataType: String,
    viewModel: AcademicViewModel,
    onMenuClick: () -> Unit
) {
    val localData by viewModel.getAcademicDataFlow(matricula, dataType).collectAsState()
    
    // Al cargar la pantalla, si no hay datos locales, disparamos la carga
    LaunchedEffect(key1 = dataType) {
        if (localData == null) {
            when (dataType) {
                "CARGA" -> viewModel.loadCarga(matricula)
                "KARDEX" -> viewModel.loadKardex(matricula)
                "UNIDADES" -> viewModel.loadUnidades(matricula)
                "FINAL" -> viewModel.loadFinales(matricula)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        when (dataType) {
                            "CARGA" -> viewModel.loadCarga(matricula)
                            "KARDEX" -> viewModel.loadKardex(matricula)
                            "UNIDADES" -> viewModel.loadUnidades(matricula)
                            "FINAL" -> viewModel.loadFinales(matricula)
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBlue)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(LightBackground)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            localData?.let { data ->
                val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(data.lastUpdated))
                Text(
                    text = "Última actualización: $date",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Resultado del Servidor:",
                            fontWeight = FontWeight.Bold,
                            color = DarkBlue,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = data.data,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                }
            } ?: run {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Text("Cargando datos...", modifier = Modifier.padding(top = 16.dp))
                }
            }
        }
    }
}
