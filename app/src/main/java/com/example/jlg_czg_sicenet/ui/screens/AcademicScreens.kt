package com.example.jlg_czg_sicenet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jlg_czg_sicenet.model.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicDataScreen(
    title: String,
    matricula: String,
    type: String,
    viewModel: AcademicViewModel,
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit
) {
    val academicEntity by viewModel.getAcademicEntityFlow(matricula, type).collectAsState()
    
    // Observar el estado de carga del ViewModel según el tipo
    val currentUiState = when (type) {
        "CARGA" -> viewModel.cargaUiState
        "KARDEX" -> viewModel.kardexUiState
        "UNIDADES" -> viewModel.unidadesUiState
        "FINAL" -> viewModel.finalesUiState
        else -> AcademicUiState.Idle
    }

    // Cargar datos automáticamente al entrar
    LaunchedEffect(matricula, type) {
        if (currentUiState is AcademicUiState.Idle) {
            when (type) {
                "CARGA" -> viewModel.loadCarga(matricula)
                "KARDEX" -> viewModel.loadKardex(matricula)
                "UNIDADES" -> viewModel.loadUnidades(matricula)
                "FINAL" -> viewModel.loadFinales(matricula)
            }
        }
    }

    val lastUpdated = academicEntity?.lastUpdated?.let {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(it))
    } ?: "Nunca"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(text = "Última actualización: $lastUpdated", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                when (type) {
                    "CARGA" -> viewModel.loadCarga(matricula)
                    "KARDEX" -> viewModel.loadKardex(matricula)
                    "UNIDADES" -> viewModel.loadUnidades(matricula)
                    "FINAL" -> viewModel.loadFinales(matricula)
                }
            }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
            }
        }
    ) { paddingValues ->
        Box(modifier = modifier.padding(paddingValues)) {
            // Contenido principal
            when (type) {
                "CARGA" -> CargaContent(viewModel, matricula)
                "KARDEX" -> KardexContent(viewModel, matricula)
                "UNIDADES" -> UnidadesContent(viewModel, matricula)
                "FINAL" -> FinalesContent(viewModel, matricula)
            }

            // Overlay de carga
            if (currentUiState is AcademicUiState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Mostrar error si ocurre
            if (currentUiState is AcademicUiState.Error) {
                SnackbarHost(
                    hostState = remember { SnackbarHostState() },
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Snackbar { Text(currentUiState.message) }
                }
            }
        }
    }
}

@Composable
fun CargaContent(viewModel: AcademicViewModel, matricula: String) {
    val items by viewModel.getCargaFlow(matricula).collectAsState(initial = emptyList())
    val academicEntity by viewModel.getAcademicEntityFlow(matricula, "CARGA").collectAsState(initial = null)

    if (items.isEmpty()) {
        if (viewModel.cargaUiState !is AcademicUiState.Loading) {
            // If parsing produced empty list but there is raw data in DB, show it for debugging
            val raw = academicEntity?.data
            if (!raw.isNullOrEmpty()) {
                Card(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Datos crudos guardados (JSON):", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = raw.take(2000), style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Si ves aquí el JSON, cópialo y pégalo en la conversación para que lo revise.")
                    }
                }
            } else {
                EmptyState("No hay carga académica disponible. Pulsa refrescar si crees que es un error.")
            }
        }
    } else {
        CargaTableContent(items = items)
    }
}

@Composable
fun CargaTableContent(items: List<MateriaCarga>) {
    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Row(modifier = Modifier.padding(8.dp)) {
                Text(text = "Materia", modifier = Modifier.weight(3f), fontWeight = FontWeight.Bold)
                Text(text = "Grupo", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text(text = "Cdt", modifier = Modifier.weight(0.6f), fontWeight = FontWeight.Bold)
                Text(text = "Horario", modifier = Modifier.weight(3f), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(contentPadding = PaddingValues(4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(items) { materia ->
                CargaTableRow(materia)
            }
        }
    }
}

@Composable
fun CargaTableRow(materia: MateriaCarga) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = materia.Materia, modifier = Modifier.weight(3f), maxLines = 2)
            Text(text = materia.Grupo, modifier = Modifier.weight(1f))
            Text(text = materia.CreditosMateria.toString(), modifier = Modifier.weight(0.6f))

            // Mostrar horario en columnas L-V para evitar concatenaciones ambiguas
            Column(modifier = Modifier.weight(3f)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "L", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text(text = "M", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text(text = "Mi", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text(text = "J", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text(text = "V", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(text = materia.Lunes?.takeIf { it.isNotBlank() } ?: "-", modifier = Modifier.weight(1f), fontSize = 12.sp)
                    Text(text = materia.Martes?.takeIf { it.isNotBlank() } ?: "-", modifier = Modifier.weight(1f), fontSize = 12.sp)
                    Text(text = materia.Miercoles?.takeIf { it.isNotBlank() } ?: "-", modifier = Modifier.weight(1f), fontSize = 12.sp)
                    Text(text = materia.Jueves?.takeIf { it.isNotBlank() } ?: "-", modifier = Modifier.weight(1f), fontSize = 12.sp)
                    Text(text = materia.Viernes?.takeIf { it.isNotBlank() } ?: "-", modifier = Modifier.weight(1f), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun MateriaCargaCard(materia: MateriaCarga) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = materia.Materia, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(text = materia.Docente, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoTag(label = "Grupo", value = materia.Grupo, icon = Icons.Default.Groups)
                InfoTag(label = "Créditos", value = materia.CreditosMateria.toString(), icon = Icons.Default.Star)
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            
            ScheduleSection(materia)
            
            if (materia.Observaciones.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Obs: ${materia.Observaciones}", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
            }
        }
    }
}

@Composable
fun ScheduleSection(materia: MateriaCarga) {
    val schedules = listOf(
        "L" to materia.Lunes,
        "M" to materia.Martes,
        "Mi" to materia.Miercoles,
        "J" to materia.Jueves,
        "V" to materia.Viernes,
        "S" to materia.Sabado
    ).filter { it.second?.isNotEmpty() == true }

    if (schedules.isNotEmpty()) {
        Column {
            schedules.forEach { (day, time) ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                    Box(modifier = Modifier.size(24.dp).background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                        Text(text = day, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = time ?: "", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun KardexContent(viewModel: AcademicViewModel, matricula: String) {
    val kardexModel by viewModel.getKardexFlow(matricula).collectAsState(initial = null)
    
    if (kardexModel == null || kardexModel?.lstKardex?.isEmpty() == true) {
        if (viewModel.kardexUiState !is AcademicUiState.Loading) {
            EmptyState("No hay información en el kardex")
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                KardexSummaryCard(kardexModel!!.Promedio)
            }
            items(kardexModel!!.lstKardex) { item ->
                KardexItemRow(item)
            }
        }
    }
}

@Composable
fun KardexSummaryCard(summary: SummaryKardex) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(text = "Promedio General", style = MaterialTheme.typography.labelMedium)
                Text(text = summary.PromedioGral.toString(), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Avance: ${summary.AvanceCdts}%", fontWeight = FontWeight.Bold)
                Text(text = "${summary.CdtsAcum} / ${summary.CdtsPlan} Créditos", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun KardexItemRow(item: KardexItem) {
    val isAproved = item.Calif >= 70
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(45.dp).background(if (isAproved) Color(0xFFE8F5E9) else Color(0xFFFFEBEE), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Text(text = item.Calif.toString(), fontWeight = FontWeight.Bold, color = if (isAproved) Color(0xFF2E7D32) else Color(0xFFC62828), fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.Materia, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(text = "${item.P1} ${item.A1} • ${item.Acred}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Text(text = "${item.Cdts} cdt", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Light)
        }
    }
}

@Composable
fun UnidadesContent(viewModel: AcademicViewModel, matricula: String) {
    val items by viewModel.getUnidadesFlow(matricula).collectAsState(initial = emptyList())
    
    if (items.isEmpty()) {
        if (viewModel.unidadesUiState !is AcademicUiState.Loading) {
            EmptyState("No hay calificaciones registradas")
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(items) { item ->
                UnidadesCard(item)
            }
        }
    }
}

@Composable
fun UnidadesCard(item: CalificacionUnidad) {
    val units = listOf(item.C1, item.C2, item.C3, item.C4, item.C5, item.C6, item.C7, item.C8, item.C9, item.C10, item.C11, item.C12, item.C13)
        .filterNotNull()
        .filter { it != "null" && it.isNotEmpty() }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = item.Materia, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = "Grupo: ${item.Grupo}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                units.forEachIndexed { index, calif ->
                    val score = calif.toIntOrNull() ?: 0
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(text = "U${index + 1}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Box(modifier = Modifier.padding(top = 2.dp).fillMaxWidth().height(30.dp).background(if (score >= 70) Color(0xFF4CAF50) else Color(0xFFF44336), RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                            Text(text = calif, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FinalesContent(viewModel: AcademicViewModel, matricula: String) {
    val items by viewModel.getFinalesFlow(matricula).collectAsState(initial = emptyList())
    
    if (items.isEmpty()) {
        if (viewModel.finalesUiState !is AcademicUiState.Loading) {
            EmptyState("No hay calificaciones finales")
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items) { item ->
                FinalGradeRow(item)
            }
        }
    }
}

@Composable
fun FinalGradeRow(item: CalificacionFinal) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.materia, fontWeight = FontWeight.Bold)
                Text(text = "${item.grupo} • ${item.acred}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text(text = item.calif.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = if (item.calif >= 70) Color(0xFF2E7D32) else Color(0xFFC62828))
        }
    }
}

@Composable
fun InfoTag(label: String, value: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "$label: ", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = message, color = Color.Gray, modifier = Modifier.padding(horizontal = 32.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
