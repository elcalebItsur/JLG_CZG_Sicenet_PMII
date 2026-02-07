package com.example.jlg_czg_sicenet.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color

// Colores (paleta)
private val DarkBlue = Color(0xFF1B396A)
private val CoolGray = Color(0xFF807E82)
private val DarkText = Color(0xFF000000)
private val LightBackground = Color(0xFFF5F5F5)

@Composable
fun LoginScreen(
    loginUiState: LoginUiState,
    matricula: String,
    contrasenia: String,
    onMatriculaChange: (String) -> Unit,
    onContraseniaChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onLoginSuccess: (String) -> Unit,
    onResetForm: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (loginUiState) {
        is LoginUiState.Idle, is LoginUiState.Loading -> {
            LoginFormScreen(
                matricula = matricula,
                contrasenia = contrasenia,
                onMatriculaChange = onMatriculaChange,
                onContraseniaChange = onContraseniaChange,
                onLoginClick = onLoginClick,
                isLoading = loginUiState is LoginUiState.Loading,
                modifier = modifier
            )
        }
        is LoginUiState.Success -> {
            onLoginSuccess(loginUiState.matricula)
        }
        is LoginUiState.Error -> {
            LoginErrorScreen(
                error = loginUiState.message,
                onRetryClick = onResetForm,
                modifier = modifier
            )
        }
    }
}

@Composable
fun LoginFormScreen(
    matricula: String,
    contrasenia: String,
    onMatriculaChange: (String) -> Unit,
    onContraseniaChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val passwordVisible = remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SICENET",
            fontSize = 32.sp,
            modifier = Modifier.padding(bottom = 32.dp),
            color = DarkBlue,
            fontWeight = FontWeight.Bold
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = matricula,
                    onValueChange = onMatriculaChange,
                    label = { Text("Matrícula") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = contrasenia,
                    onValueChange = onContraseniaChange,
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    visualTransformation = if (passwordVisible.value) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        val image = if (passwordVisible.value) {
                            Icons.Filled.Visibility
                        } else {
                            Icons.Filled.VisibilityOff
                        }
                        IconButton(onClick = {
                            passwordVisible.value = !passwordVisible.value
                        }) {
                            Icon(image, contentDescription = null)
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onLoginClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        enabled = !isLoading,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = DarkBlue)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.height(24.dp))
                        } else {
                            Text("Ingresar", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoginErrorScreen(
    error: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFEBEE)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Error",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(onClick = onRetryClick) {
                    Text("Reintentar")
                }
            }
        }
    }
}
