package com.example.divivapp.ui.admin

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.divivapp.data.MenuItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Administrador - Menu", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A2332))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.abrirModalCrear() },
                containerColor = Color(0xFFF1C40F),
                contentColor = Color.Black,
                shape = RoundedCornerShape(50.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Plato")
            }
        },
        containerColor = Color(0xFFF0F4F8)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Platos Registrados (${uiState.menuItems.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A2332),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(uiState.menuItems, key = { it.id }) { item ->
                    AdminMenuItemCard(
                        item = item,
                        onEdit = { viewModel.abrirModalEditar(item) },
                        onDelete = { viewModel.eliminarPlato(item) }
                    )
                }
            }
        }
    }

    if (uiState.isModalOpen) {
        AdminMenuDialog(
            uiState = uiState,
            onDismiss = { viewModel.cerrarModal() },
            onConfirm = { viewModel.guardarPlato() },
            onNombreChange = viewModel::updateInputNombre,
            onCategoriaChange = viewModel::updateInputCategoria,
            onIngredientesChange = viewModel::updateInputIngredientes,
            onPrecioChange = viewModel::updateInputPrecio,
            onImageSelected = viewModel::updateSelectedImageUri
        )
    }
}

@Composable
fun AdminMenuItemCard(
    item: MenuItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1A2332)
                )
                Text(
                    text = "${item.categoria} • $${item.precio}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF29B6C5))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFE74C3C))
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AdminMenuDialog(
    uiState: AdminUiState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onNombreChange: (String) -> Unit,
    onCategoriaChange: (String) -> Unit,
    onIngredientesChange: (String) -> Unit,
    onPrecioChange: (String) -> Unit,
    onImageSelected: (android.net.Uri?) -> Unit
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> onImageSelected(uri) }
    )

    AlertDialog(
        onDismissRequest = {
            if (!uiState.isUploading) onDismiss()
        },
        title = { Text(if (uiState.itemEnEdicion == null) "Nuevo Plato" else "Editar Plato") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = uiState.inputNombre,
                    onValueChange = onNombreChange,
                    label = { Text("Nombre") },
                    singleLine = true,
                    enabled = !uiState.isUploading
                )
                TextField(
                    value = uiState.inputCategoria,
                    onValueChange = onCategoriaChange,
                    label = { Text("Categoría") },
                    singleLine = true,
                    enabled = !uiState.isUploading
                )
                TextField(
                    value = uiState.inputPrecio,
                    onValueChange = onPrecioChange,
                    label = { Text("Precio ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    enabled = !uiState.isUploading
                )
                TextField(
                    value = uiState.inputIngredientes,
                    onValueChange = onIngredientesChange,
                    label = { Text("Ingredientes (separados por coma)") },
                    enabled = !uiState.isUploading
                )
                
                TextButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = !uiState.isUploading
                ) {
                    Text("Seleccionar Foto de Galería", color = Color(0xFF29B6C5))
                }

                val imageToLoad = uiState.selectedImageUri ?: uiState.inputImageUrl
                if (imageToLoad.toString().isNotEmpty()) {
                    GlideImage(
                        model = imageToLoad,
                        contentDescription = "Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                if (uiState.isUploading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                        Text("Guardando y subiendo foto...")
                    }
                }

                uiState.errorValidacion?.let { msg ->
                    Text(msg, color = Color.Red, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !uiState.isUploading
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold, color = Color(0xFF29B6C5))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !uiState.isUploading
            ) {
                Text("Cancelar", color = Color.Gray)
            }
        },
        containerColor = Color.White
    )
}
