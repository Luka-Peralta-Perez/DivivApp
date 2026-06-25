package com.example.divivapp.ui.checkout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.divivapp.data.PedidoConNombre

private val ColorTeal      = Color(0xFF29B6C5)
private val ColorTealDark  = Color(0xFF0097A7)
private val ColorOrange    = Color(0xFFF5A623)
private val ColorGreen     = Color(0xFF2ECC71)
private val ColorGreenDark = Color(0xFF27AE60)
private val ColorBg        = Color(0xFFF0F4F8)
private val ColorText      = Color(0xFF1A2332)
private val ColorTextSub   = Color(0xFF6B7A8D)
private val ColorBorder    = Color(0xFFE2E8F0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onNavigateBack: () -> Unit,
    onMesaCerrada: () -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.mesaCerrada) {
        if (uiState.mesaCerrada) onMesaCerrada()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Checkout",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Division de cuenta",
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
                    containerColor = ColorGreen,
                    titleContentColor = Color.White
                )
            )
        },
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

        val todosPageron = uiState.comensalesPagados == uiState.totalComensales &&
                uiState.totalComensales > 0

        LazyColumn(
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(innerPadding)
        ) {
            item {
                ResumenGeneralCard(uiState = uiState)
            }

            items(uiState.resumenes, key = { it.comensal.id }) { resumen ->
                ComensalCheckoutCard(
                    resumen = resumen,
                    onPagar = { viewModel.abrirModalPago(resumen) }
                )
            }

            if (todosPageron) {
                item {
                    AnimatedVisibility(visible = true, enter = fadeIn()) {
                        Button(
                            onClick = viewModel::cerrarMesa,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ColorGreenDark
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = "Cerrar Mesa — Todos pagaron",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.comensalEnPago != null) {
        ModalPago(
            resumen = uiState.comensalEnPago!!,
            metodoPagoSeleccionado = uiState.metodoPagoSeleccionado,
            onMetodoPagoClick = viewModel::seleccionarMetodoPago,
            onConfirmar = viewModel::confirmarPago,
            onDismiss = viewModel::cerrarModal
        )
    }
}

@Composable
private fun ResumenGeneralCard(uiState: CheckoutUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(listOf(ColorTeal, ColorGreen))
            )
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Resumen de Mesa",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatBox(
                    label = "Total general",
                    valor = "$${uiState.totalGeneral.toLong().toLocaleString()}"
                )
                StatBox(
                    label = "Pendiente",
                    valor = "$${uiState.totalPendiente.toLong().toLocaleString()}"
                )
                StatBox(
                    label = "Pagaron",
                    valor = "${uiState.comensalesPagados}/${uiState.totalComensales}"
                )
            }
        }
    }
}

@Composable
private fun StatBox(label: String, valor: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = valor,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.75f)
        )
    }
}

@Composable
private fun ComensalCheckoutCard(
    resumen: ResumenComensal,
    onPagar: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (resumen.pagado) Color(0xFFF0FFF4) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(
                                if (resumen.pagado) Color(0xFFD4EDDA) else Color(0xFFE0F7FA)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = resumen.comensal.nombre.first().uppercase(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (resumen.pagado) ColorGreenDark else ColorTealDark
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = resumen.comensal.nombre,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ColorText
                        )
                        Text(
                            text = "${resumen.pedidos.size} plato(s)",
                            fontSize = 11.sp,
                            color = ColorTextSub
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (resumen.pagado) ColorGreen else Color(0xFFFFF3E0))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (resumen.pagado) "Pago" else "Pendiente",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (resumen.pagado) Color.White else ColorOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (resumen.pedidos.isEmpty()) {
                Text(
                    text = "Sin pedidos cargados",
                    fontSize = 13.sp,
                    color = ColorTextSub,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            } else {
                resumen.pedidos.forEach { pedido ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = pedido.nombre_plato,
                            fontSize = 13.sp,
                            color = ColorText,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "$${pedido.precio_fijado.toLong().toLocaleString()}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorTextSub
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(ColorBorder)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SUBTOTAL",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ColorTextSub
                    )
                    Text(
                        text = "$${resumen.subtotal.toLong().toLocaleString()}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = ColorText
                    )
                }

                if (!resumen.pagado) {
                    Button(
                        onClick = onPagar,
                        colors = ButtonDefaults.buttonColors(containerColor = ColorGreen),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Cobrar",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                } else {
                    Text(
                        text = "Listo",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ColorGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun ModalPago(
    resumen: ResumenComensal,
    metodoPagoSeleccionado: String,
    onMetodoPagoClick: (String) -> Unit,
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        title = {
            Column {
                Text(
                    text = "Cobrar a ${resumen.comensal.nombre}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = ColorText
                )
                Text(
                    text = "$${resumen.subtotal.toLong().toLocaleString()}",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = ColorGreenDark
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "METODO DE PAGO",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ColorTextSub,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                METODOS_PAGO.forEach { metodo ->
                    val seleccionado = metodo == metodoPagoSeleccionado
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                2.dp,
                                if (seleccionado) ColorGreen else ColorBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .background(if (seleccionado) Color(0xFFF0FFF4) else Color.White)
                            .clickable { onMetodoPagoClick(metodo) }
                            .padding(11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = obtenerEmojiMetodo(metodo),
                            fontSize = 16.sp,
                            modifier = Modifier.width(28.dp)
                        )
                        Text(
                            text = metodo,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ColorText
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(containerColor = ColorGreen),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Confirmar pago",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = ColorTextSub)
            }
        }
    )
}

private fun obtenerEmojiMetodo(metodo: String): String = when (metodo) {
    "Efectivo"           -> "\uD83D\uDCB5"
    "Transferencia"      -> "\uD83D\uDCF1"
    "Tarjeta debito"     -> "\uD83D\uDCB3"
    "Tarjeta credito"    -> "\uD83D\uDCB3"
    else                 -> "\uD83D\uDCB0"
}

private fun Long.toLocaleString(): String {
    return String.format("%,d", this).replace(",", ".")
}
