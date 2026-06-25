package com.example.divivapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.divivapp.ui.auth.AuthViewModel
import com.example.divivapp.ui.auth.LoginScreen
import com.example.divivapp.ui.checkout.CheckoutScreen
import com.example.divivapp.ui.dashboard.DashboardScreenReal
import com.example.divivapp.ui.detalle.DetalleScreen
import com.example.divivapp.ui.edicion.EdicionScreen
import com.example.divivapp.ui.menu.MenuScreen
import kotlinx.coroutines.delay

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.divivapp.ui.admin.AdminScreen

@Composable
fun DivivAppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") {
            SplashScreenReal(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate("dashboard") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("dashboard") {
            DashboardScreenReal(
                authViewModel = authViewModel,
                onNavigateToMenu = { mesaId ->
                    navController.navigate("menu/$mesaId") {
                        launchSingleTop = true
                    }
                },
                onNavigateToCheckout = { mesaId ->
                    navController.navigate("checkout/$mesaId") {
                        launchSingleTop = true
                    }
                },
                onNavigateToEdicion = { mesaId ->
                    navController.navigate("edicion/$mesaId") {
                        launchSingleTop = true
                    }
                },
                onNavigateToAdmin = {
                    navController.navigate("admin") {
                        launchSingleTop = true
                    }
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }

        composable("admin") {
            AdminScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("menu/{mesaId}") { backStackEntry ->
            val mesaId = backStackEntry.arguments?.getString("mesaId") ?: ""
            MenuScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetalle = { itemId ->
                    navController.navigate("detalle/$itemId")
                },
                onNavigateToEdicion = {
                    navController.navigate("edicion/$mesaId")
                },
                onNavigateToCheckout = {
                    navController.navigate("checkout/$mesaId")
                }
            )
        }

        composable("detalle/{menuItemId}") {
            DetalleScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("edicion/{mesaId}") {
            EdicionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("checkout/{mesaId}") {
            CheckoutScreen(
                onNavigateBack = { navController.popBackStack() },
                onMesaCerrada = {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = false }
                    }
                }
            )
        }
    }
}

@Composable
fun SplashScreenReal(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    LaunchedEffect(key1 = true) {
        delay(1500) // Un pequeño retraso para que se luzca el logo
        if (authViewModel.hasActiveSession()) {
            onNavigateToDashboard()
        } else {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF29B6C5)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = com.example.divivapp.R.drawable.logo_divivapp),
                contentDescription = "Logo DivivApp",
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "DivivApp",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
            Text(
                text = "División inteligente de cuentas",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun PantallaTemporal(titulo: String) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(text = titulo, color = Color.Black, fontSize = 24.sp)
    }
}
