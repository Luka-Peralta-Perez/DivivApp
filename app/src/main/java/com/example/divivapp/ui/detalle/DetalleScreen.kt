package com.example.divivapp.ui.detalle

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.divivapp.data.MenuItem

private val ColorTeal      = Color(0xFF29B6C5)
private val ColorTealDark  = Color(0xFF0097A7)
private val ColorOrange    = Color(0xFFF5A623)
private val ColorBg        = Color(0xFFF0F4F8)
private val ColorText      = Color(0xFF1A2332)
private val ColorTextSub   = Color(0xFF6B7A8D)
private val ColorBorder    = Color(0xFFE2E8F0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(
    onNavigateBack: () -> Unit,
    viewModel: DetalleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.item?.nombre ?: "Detalle",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
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
                    containerColor = ColorTeal,
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

        val item = uiState.item
        if (item == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Plato no encontrado.", color = ColorTextSub)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFE0F7FA), Color(0xFFFFF8E1))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (item.image_url.isNotBlank()) {
                    @OptIn(ExperimentalGlideComposeApi::class)
                    GlideImage(
                        model = item.image_url,
                        contentDescription = item.nombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                } else {
                    Text(text = obtenerEmoji(item.nombre), fontSize = 72.sp)
                }
            }

            Column(modifier = Modifier.padding(18.dp)) {

                Text(
                    text = item.nombre,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ColorText
                )
                Text(
                    text = "$${item.precio.toLong().toLocaleString()}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = ColorTealDark,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                val labels = item.health_labels
                    ?.removeSurrounding("[", "]")
                    ?.split(",")
                    ?.map { it.trim().removeSurrounding("\"") }
                    ?.filter { it.isNotEmpty() }
                    ?: emptyList()

                if (labels.isNotEmpty()) {
                    SectionTitle("Informacion nutricional")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp).fillMaxWidth()
                    ) {
                        items(labels) { label ->
                            val isAllergy = label.startsWith("Alergia: ")
                            LabelChip(
                                label = label.replace("Alergia: ", "⚠️ "),
                                isWarning = isAllergy
                            )
                        }
                    }
                }

                SectionTitle("Valores por porcion (por 100g)")
                Spacer(modifier = Modifier.height(8.dp))
                NutricionRow(item)

                Spacer(modifier = Modifier.height(20.dp))

                SectionTitle("Descripcion del plato")
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    ColorTeal.copy(alpha = 0.07f),
                                    ColorOrange.copy(alpha = 0.07f)
                                )
                            )
                        )
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "Gemini IA",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ColorText
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(ColorTeal, ColorOrange)
                                        )
                                    )
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "IA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }

                        when {
                            uiState.iaLoading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = ColorTeal,
                                    strokeWidth = 2.dp
                                )
                            }
                            uiState.iaError -> {
                                Text(
                                    text = "Error de Gemini:\n${uiState.iaErrorMessage ?: "Desconocido"}\n\nRevisa si tu API Key es válida y si Gemini está disponible en tu región.",
                                    fontSize = 13.sp,
                                    color = Color.Red,
                                    lineHeight = 20.sp
                                )
                            }
                            uiState.descripcionIa != null -> {
                                AnimatedVisibility(visible = true, enter = fadeIn()) {
                                    Text(
                                        text = uiState.descripcionIa!!,
                                        fontSize = 13.sp,
                                        color = ColorTextSub,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorTeal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Volver al menu",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionTitle(texto: String) {
    Text(
        text = texto.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        color = ColorTextSub,
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun LabelChip(label: String, isWarning: Boolean = false) {
    val bgColor = if (isWarning) Color(0xFFFFEBEE) else Color(0xFFE0F7FA)
    val textColor = if (isWarning) Color(0xFFC62828) else ColorTealDark

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
private fun NutricionRow(item: MenuItem) {
    val cal = if (item.calorias == null || item.calorias < 0) "N/A" else "${item.calorias.toInt()}kcal"
    val prot = if (item.proteinas == null || item.proteinas < 0) "N/A" else "${item.proteinas.toInt()}g"
    val carb = if (item.carbohidratos == null || item.carbohidratos < 0) "N/A" else "${item.carbohidratos.toInt()}g"
    val gras = if (item.grasas == null || item.grasas < 0) "N/A" else "${item.grasas.toInt()}g"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        NutricionBox(label = "Calorias", valor = cal, modifier = Modifier.weight(1f))
        NutricionBox(label = "Proteina", valor = prot, modifier = Modifier.weight(1f))
        NutricionBox(label = "Carbos", valor = carb, modifier = Modifier.weight(1f))
        NutricionBox(label = "Grasa", valor = gras, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun NutricionBox(label: String, valor: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = valor,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ColorText,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = ColorTextSub,
            textAlign = TextAlign.Center
        )
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
        n.contains("cafe") -> "\u2615"
        n.contains("helado") || n.contains("postre") -> "\uD83C\uDF68"
        n.contains("tarta") || n.contains("torta") -> "\uD83C\uDF70"
        n.contains("sopa") -> "\uD83C\uDF72"
        n.contains("empanada") -> "\uD83E\uFAD4"
        else -> "\uD83C\uDF7D\uFE0F"
    }
}

private fun Long.toLocaleString(): String {
    return String.format("%,d", this).replace(",", ".")
}
