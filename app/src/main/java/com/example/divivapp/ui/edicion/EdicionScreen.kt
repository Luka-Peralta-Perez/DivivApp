package com.example.divivapp.ui.edicion

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.divivapp.data.Comensal
import com.example.divivapp.data.PedidoConNombre

private val ColorTeal      = Color(0xFF29B6C5)
private val ColorTealDark  = Color(0xFF0097A7)
private val ColorOrange    = Color(0xFFF5A623)
private val ColorBg        = Color(0xFFF0F4F8)
private val ColorText      = Color(0xFF1A2332)
private val ColorTextSub   = Color(0xFF6B7A8D)
private val ColorBorder    = Color(0xFFE2E8F0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EdicionScreen(
    onNavigateBack: () -> Unit,
    viewModel: EdicionViewModel = hiltViewModel()
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
                            text = "Editar Consumo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Toca un plato para reasignarlo",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorOrange,
                    titleContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ColorBg
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ColorTeal)
            }
            return@Scaffold
        }

        val hayPedidos = uiState.consumos.any { it.pedidos.isNotEmpty() }

        if (!hayPedidos) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay pedidos cargados en esta mesa.\nVe al menu y asigna platos.",
                    color = ColorTextSub,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
            return@Scaffold
        }

        LazyColumn(
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(innerPadding)
        ) {
            uiState.consumos.forEach { consumo ->
                if (consumo.pedidos.isNotEmpty()) {
                    item(key = "header_${consumo.comensal.id}") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(Color(0xFFE0F7FA)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = consumo.comensal.nombre.first().uppercase(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorTealDark
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = consumo.comensal.nombre,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ColorText
                            )
                        }
                    }

                    items(consumo.pedidos, key = { it.pedido_id }) { pedido ->
                        PedidoEditableCard(
                            pedido = pedido,
                            onClick = {
                                viewModel.abrirSheetReasignacion(
                                    pedido = pedido,
                                    comensalActualNombre = consumo.comensal.nombre
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    if (uiState.pedidoAReasignar != null) {
        ModalBottomSheet(
            onDismissRequest = viewModel::cerrarSheet,
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            SheetReasignacion(
                pedido = uiState.pedidoAReasignar!!,
                comensalActualNombre = uiState.comensalActualNombre,
                comensales = uiState.comensales,
                comensalOrigenId = uiState.pedidoAReasignar!!.comensal_id,
                comensalDestinoId = uiState.comensalDestinoId,
                onComensalClick = viewModel::seleccionarComensalDestino,
                onConfirmar = viewModel::confirmarReasignacion,
                onCancelar = viewModel::cerrarSheet
            )
        }
    }
}

@Composable
private fun PedidoEditableCard(pedido: PedidoConNombre, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "\uD83D\uDCCB", fontSize = 20.sp, modifier = Modifier.width(36.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pedido.nombre_plato,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ColorText
            )
            Text(
                text = "$${pedido.precio_fijado.toLong().toLocaleString()}",
                fontSize = 12.sp,
                color = ColorTealDark,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Text(
            text = "Reasignar",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = ColorOrange
        )
    }
}

@Composable
private fun SheetReasignacion(
    pedido: PedidoConNombre,
    comensalActualNombre: String,
    comensales: List<Comensal>,
    comensalOrigenId: String,
    comensalDestinoId: String?,
    onComensalClick: (String) -> Unit,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = pedido.nombre_plato,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ColorText
        )
        Text(
            text = "Actualmente asignado a: $comensalActualNombre",
            fontSize = 13.sp,
            color = ColorTextSub,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        Text(
            text = "REASIGNAR A",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = ColorTextSub
        )

        Spacer(modifier = Modifier.height(8.dp))

        comensales.filter { it.id != comensalOrigenId }.forEach { comensal ->
            val seleccionado = comensal.id == comensalDestinoId
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        2.dp,
                        if (seleccionado) ColorOrange else ColorBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .background(if (seleccionado) Color(0xFFFFF3E0) else Color.White)
                    .clickable { onComensalClick(comensal.id) }
                    .padding(11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color(0xFFFFF3E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = comensal.nombre.first().uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorOrange
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
                enabled = comensalDestinoId != null,
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Reasignar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun Long.toLocaleString(): String {
    return String.format("%,d", this).replace(",", ".")
}
