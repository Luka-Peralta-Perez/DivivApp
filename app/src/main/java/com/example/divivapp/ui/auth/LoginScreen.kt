package com.example.divivapp.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

private val ColorTeal      = Color(0xFF29B6C5)
private val ColorTealDark  = Color(0xFF0097A7)
private val ColorOrange    = Color(0xFFF5A623)
private val ColorBg        = Color(0xFFF0F4F8)
private val ColorText      = Color(0xFF1A2332)
private val ColorTextSub   = Color(0xFF6B7A8D)

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = uiState) {
        when (val state = uiState) {
            is LoginUiState.Success -> {
                onLoginSuccess()
            }
            is LoginUiState.Error -> {
                snackbarHostState.showSnackbar(message = state.message)
                authViewModel.resetState()
            }
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE0F7FA),
                        ColorBg,
                        Color(0xFFFFF8E1)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = com.example.divivapp.R.drawable.logo_divivapp),
                contentDescription = "Logo DivivApp",
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(22.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row {
                Text(
                    text = "Diviv",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ColorTealDark
                )
                Text(
                    text = "App",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ColorOrange
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "División inteligente de cuentas",
                color = ColorTextSub,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(56.dp))

            val isLoading = uiState is LoginUiState.Loading

            Button(
                onClick = {
                    authViewModel.signInWithGoogle(context)
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .border(
                        width = 1.5.dp,
                        color = if (isLoading) Color.Transparent else Color(0xFFDDE3EA),
                        shape = RoundedCornerShape(12.dp)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    disabledContainerColor = Color(0xFFF5F5F5)
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                )
            ) {
                AnimatedVisibility(visible = isLoading, enter = fadeIn(), exit = fadeOut()) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = ColorTeal,
                        strokeWidth = 2.5.dp
                    )
                }
                AnimatedVisibility(visible = !isLoading, enter = fadeIn(), exit = fadeOut()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GoogleColoredG()
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Continuar con Google",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ColorText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFDDE3EA))
                Text(
                    text = "  Acceso seguro via Firebase  ",
                    fontSize = 11.sp,
                    color = ColorTextSub
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFDDE3EA))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Al iniciar sesión aceptas los Términos de Servicio\ny la Política de Privacidad de DivivApp.",
                fontSize = 11.sp,
                color = ColorTextSub,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { snackbarData ->
            Snackbar(
                snackbarData = snackbarData,
                containerColor = Color(0xFF1A2332),
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun GoogleColoredG() {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(1.dp, Color(0xFFDDE3EA), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "G",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4285F4)
        )
    }
}