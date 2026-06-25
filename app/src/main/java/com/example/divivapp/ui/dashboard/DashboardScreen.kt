package com.example.divivapp.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.divivapp.data.Mesa
import com.example.divivapp.ui.auth.AuthViewModel

@Composable
fun DashboardScreenReal(
    authViewModel: AuthViewModel,
    onNavigateToMenu: (String) -> Unit,
    onNavigateToCheckout: (String) -> Unit,
    onNavigateToEdicion: (String) -> Unit,
    onNavigateToAdmin: () -> Unit,
    onLogout: () -> Unit,
    dashboardViewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()

    DashboardContent(
        uiState = uiState,
        isAdmin = authViewModel.isAdmin(),
        onNuevaMesaClick = dashboardViewModel::abrirDialogoNuevaMesa,
        onDialogDismiss = dashboardViewModel::cerrarDialogo,
        onNumeroMesaChange = dashboardViewModel::actualizarNumeroMesa,
        onNombresChange = dashboardViewModel::actualizarNombresComensales,
        onCrearMesaConfirm = dashboardViewModel::crearMesa,
        onMesaClick = onNavigateToMenu,
        onCheckoutClick = onNavigateToCheckout,
        onEdicionClick = onNavigateToEdicion,
        onAdminClick = onNavigateToAdmin,
        onLogoutClick = {
            authViewModel.signOut()
            onLogout()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    isAdmin: Boolean,
    onNuevaMesaClick: () -> Unit,
    onDialogDismiss: () -> Unit,
    onNumeroMesaChange: (String) -> Unit,
    onNombresChange: (String) -> Unit,
    onCrearMesaConfirm: () -> Unit,
    onMesaClick: (String) -> Unit,
    onCheckoutClick: (String) -> Unit,
    onEdicionClick: (String) -> Unit,
    onAdminClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("DivivApp", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Salon Principal", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                },
                actions = {
                    if (isAdmin) {
                        TextButton(onClick = onAdminClick) {
                            Text("Admin", color = Color(0xFFF1C40F), fontWeight = FontWeight.Bold)
                        }
                    }
                    TextButton(onClick = onLogoutClick) {
                        Text("Salir", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF29B6C5),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNuevaMesaClick,
                containerColor = Color(0xFF29B6C5),
                contentColor = Color.White,
                shape = RoundedCornerShape(50.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Mesa")
            }
        },
        containerColor = Color(0xFFF0F4F8)
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF29B6C5))
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Mesas Activas",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A2332),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (uiState.mesas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No hay mesas activas.\nToca el boton + para abrir una.",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.mesas, key = { it.id }) { mesa ->
                        MesaCard(
                            mesa = mesa,
                            onVerMenuClick = { onMesaClick(mesa.id) },
                            onCheckoutClick = { onCheckoutClick(mesa.id) },
                            onEdicionClick = { onEdicionClick(mesa.id) }
                        )
                    }
                }
            }
        }
    }

    if (uiState.mostrarDialogo) {
        AlertDialog(
            onDismissRequest = onDialogDismiss,
            title = { Text("Abrir Nueva Mesa", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextField(
                        value = uiState.inputNumeroMesa,
                        onValueChange = onNumeroMesaChange,
                        label = { Text("Numero de Mesa") },
                        placeholder = { Text("Ej: 5") },
                        isError = uiState.errorValidacion != null
                    )
                    TextField(
                        value = uiState.inputNombresComensales,
                        onValueChange = onNombresChange,
                        label = { Text("Nombres de comensales") },
                        placeholder = { Text("Ana, Carlos, Maria, Pedro") },
                        supportingText = { Text("Separados por coma", fontSize = 11.sp) },
                        isError = uiState.errorValidacion != null
                    )
                    uiState.errorValidacion?.let { mensaje ->
                        Text(text = mensaje, color = Color.Red, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onCrearMesaConfirm) {
                    Text("Crear Mesa", color = Color(0xFF29B6C5), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDialogDismiss) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
private fun MesaCard(
    mesa: Mesa,
    onVerMenuClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    onEdicionClick: () -> Unit
) {
    val colorEstado = when (mesa.estado) {
        "abierta"    -> Color(0xFF2ECC71)
        "cerrando"   -> Color(0xFFF1C40F)
        else         -> Color.Gray
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mesa ${mesa.numero}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF1A2332)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colorEstado)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        mesa.estado.replaceFirstChar { it.uppercase() },
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onVerMenuClick,
                    modifier = Modifier.weight(1.5f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF29B6C5)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 8.dp, vertical = 8.dp
                    )
                ) {
                    Text(
                        "Ver Menu",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                OutlinedButton(
                    onClick = onEdicionClick,
                    modifier = Modifier.weight(1.2f),
                    colors = ButtonDefaults.outlinedButtonColors(),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp, Color(0xFFF5A623)
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 8.dp, vertical = 8.dp
                    )
                ) {
                    Text(
                        "Editar",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF5A623)
                    )
                }

                OutlinedButton(
                    onClick = onCheckoutClick,
                    modifier = Modifier.weight(1.2f),
                    colors = ButtonDefaults.outlinedButtonColors(),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp, Color(0xFF2ECC71)
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 8.dp, vertical = 8.dp
                    )
                ) {
                    Text(
                        "Checkout",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF27AE60)
                    )
                }
            }
        }
    }
}
