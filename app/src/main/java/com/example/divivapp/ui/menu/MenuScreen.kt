package com.example.divivapp.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.divivapp.data.Comensal
import com.example.divivapp.data.MenuItem

private val ColorTeal      = Color(0xFF29B6C5)
private val ColorTealDark  = Color(0xFF0097A7)
private val ColorOrange    = Color(0xFFF5A623)
private val ColorGreen     = Color(0xFF2ECC71)
private val ColorBg        = Color(0xFFF0F4F8)
private val ColorText      = Color(0xFF1A2332)
private val ColorTextSub   = Color(0xFF6B7A8D)
private val ColorBorder    = Color(0xFFE2E8F0)
private val GradientBrush  = Brush.horizontalGradient(listOf(ColorTeal, ColorOrange))

private val CATEGORIAS = listOf("Todos", "Entradas", "Platos", "Bebidas", "Postres")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetalle: (String) -> Unit,
    onNavigateToEdicion: () -> Unit,
    onNavigateToCheckout: () -> Unit,
    viewModel: MenuViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(uiState.mensajeToast) {
        uiState.mensajeToast?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            viewModel.limpiarToast()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Menu de Platos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Toca un plato para asignarlo",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al Dashboard",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToEdicion) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar consumo",
                            tint = Color.White
                        )
                    }
                    TextButton(onClick = onNavigateToCheckout) {
                        Text(
                            text = "Cuenta",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorTeal,
                    titleContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ColorBg
    ) { innerPadding ->

        MenuContent(
            uiState = uiState,
            innerPadding = innerPadding,
            onBusquedaChange = viewModel::actualizarBusqueda,
            onCategoriaClick = viewModel::seleccionarCategoria,
            onItemClick = viewModel::abrirSheetAsignacion
        )
    }

    if (uiState.itemSeleccionado != null) {
        ModalBottomSheet(
            onDismissRequest = viewModel::cerrarSheet,
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            SheetAsignacion(
                item = uiState.itemSeleccionado!!,
                comensales = uiState.comensales,
                comensalSeleccionadoId = uiState.comensalSeleccionadoId,
                onComensalClick = viewModel::seleccionarComensal,
                onConfirmar = viewModel::confirmarAsignacion,
                onCancelar = viewModel::cerrarSheet,
                onVerDetalle = {
                    viewModel.cerrarSheet()
                    onNavigateToDetalle(uiState.itemSeleccionado!!.id)
                }
            )
        }
    }
}

@Composable
private fun MenuContent(
    uiState: MenuUiState,
    innerPadding: PaddingValues,
    onBusquedaChange: (String) -> Unit,
    onCategoriaClick: (String) -> Unit,
    onItemClick: (MenuItem) -> Unit
) {
    val itemsFiltrados = if (uiState.categoriaActiva == "Todos") {
        uiState.items
    } else {
        uiState.items.filter { inferirCategoria(it.nombre) == uiState.categoriaActiva }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        OutlinedTextField(
            value = uiState.busqueda,
            onValueChange = onBusquedaChange,
            placeholder = { Text("Buscar plato...", color = ColorTextSub) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = ColorTextSub)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ColorTeal,
                unfocusedBorderColor = ColorBorder,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            items(CATEGORIAS) { categoria ->
                CategoriaChip(
                    label = categoria,
                    activa = categoria == uiState.categoriaActiva,
                    onClick = { onCategoriaClick(categoria) }
                )
            }
        }

        if (itemsFiltrados.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No se encontraron platos.\nIntenta con otra busqueda.",
                    color = ColorTextSub,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(itemsFiltrados, key = { it.id }) { item ->
                    MenuItemCard(item = item, onClick = { onItemClick(item) })
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun MenuItemCard(item: MenuItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFE0F7FA), Color(0xFFFFF8E1))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (item.image_url.isNotBlank()) {
                    GlideImage(
                        model = item.image_url,
                        contentDescription = item.nombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Text(text = obtenerEmoji(item.nombre), fontSize = 28.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ColorText
                )
                Text(
                    text = inferirCategoria(item.nombre),
                    fontSize = 11.sp,
                    color = ColorTextSub,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = "$${item.precio.toLong().toLocaleString()}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ColorTealDark,
                    modifier = Modifier.padding(top = 4.dp)
                )

                if (!item.health_labels.isNullOrBlank()) {
                    val labels = item.health_labels
                        .removeSurrounding("[", "]")
                        .split(",")
                        .map { it.trim().removeSurrounding("\"") }
                        .filter { it.isNotEmpty() }
                        .take(3)

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        labels.forEach { label ->
                            HealthLabelChip(label = label)
                        }
                    }
                }
            }

            Text(text = ">", fontSize = 20.sp, color = ColorBorder)
        }
    }
}

@Composable
private fun CategoriaChip(label: String, activa: Boolean, onClick: () -> Unit) {
    val modifier = if (activa) {
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(GradientBrush)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    } else {
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(2.dp, ColorBorder, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    }
    Box(modifier = modifier) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (activa) Color.White else ColorTextSub
        )
    }
}

@Composable
private fun HealthLabelChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFE0F7FA))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = ColorTealDark
        )
    }
}

@Composable
private fun SheetAsignacion(
    item: MenuItem,
    comensales: List<Comensal>,
    comensalSeleccionadoId: String?,
    onComensalClick: (String) -> Unit,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    onVerDetalle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.nombre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ColorText
                )
                Text(
                    text = "$${item.precio.toLong().toLocaleString()}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = ColorTealDark,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            TextButton(onClick = onVerDetalle) {
                Text(
                    text = "Ver detalle",
                    fontSize = 12.sp,
                    color = ColorTeal,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ASIGNAR A",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = ColorTextSub,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        comensales.forEach { comensal ->
            val seleccionado = comensal.id == comensalSeleccionadoId
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        2.dp,
                        if (seleccionado) ColorTeal else ColorBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .background(if (seleccionado) Color(0xFFE0F7FA) else Color.White)
                    .clickable { onComensalClick(comensal.id) }
                    .padding(11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color(0xFFE0F7FA)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = comensal.nombre.first().uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTealDark
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = comensal.nombre,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ColorText
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onCancelar,
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(2.dp, ColorBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancelar", color = ColorTextSub, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onConfirmar,
                enabled = comensalSeleccionadoId != null,
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorTeal),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Asignar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}


private fun obtenerEmoji(nombre: String): String {
    val n = nombre.lowercase()
    return when {
        n.contains("bife") || n.contains("carne") || n.contains("asado") -> "\uD83E\uDD69"
        n.contains("pollo") -> "\uD83C\uDF57"
        n.contains("ensalada") -> "\uD83E\uDD57"
        n.contains("pasta") -> "\uD83C\uDF5D"
        n.contains("pizza") -> "\uD83C\uDF55"
        n.contains("salmon") || n.contains("pescado") -> "\uD83D\uDC1F"
        n.contains("vino") -> "\uD83C\uDF77"
        n.contains("cerveza") -> "\uD83C\uDF7A"
        n.contains("agua") -> "\uD83D\uDCA7"
        n.contains("jugo") || n.contains("gaseosa") -> "\uD83E\uDD64"
        n.contains("cafe") || n.contains("expreso") -> "\u2615"
        n.contains("helado") || n.contains("postre") -> "\uD83C\uDF68"
        n.contains("tarta") || n.contains("torta") -> "\uD83C\uDF70"
        n.contains("sopa") -> "\uD83C\uDF72"
        n.contains("empanada") -> "\uD83E\uFAD4"
        else -> "\uD83C\uDF7D\uFE0F"
    }
}

fun inferirCategoria(nombre: String): String {
    val n = nombre.lowercase()
    return when {
        n.contains("vino") || n.contains("cerveza") || n.contains("agua") ||
        n.contains("jugo") || n.contains("gaseosa") || n.contains("cafe") ||
        n.contains("expreso") -> "Bebidas"
        n.contains("helado") || n.contains("postre") || n.contains("tarta") ||
        n.contains("torta") || n.contains("flan") -> "Postres"
        n.contains("ensalada") || n.contains("sopa") || n.contains("empanada") ||
        n.contains("tabla") || n.contains("provoleta") -> "Entradas"
        else -> "Platos"
    }
}

private fun Long.toLocaleString(): String {
    return String.format("%,d", this).replace(",", ".")
}
