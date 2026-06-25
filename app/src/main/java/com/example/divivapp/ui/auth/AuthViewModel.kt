package com.example.divivapp.ui.auth

import android.content.Context
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class LoginUiState {
    object Idle    : LoginUiState()   // Estado inicial, sin ninguna accion en curso
    object Loading : LoginUiState()   // Esperando respuesta de Firebase o Google
    data class Success(val displayName: String) : LoginUiState() // Login exitoso
    data class Error(val message: String)       : LoginUiState() // Algo salio mal
}

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()


    fun hasActiveSession(): Boolean = auth.currentUser != null

    fun getCurrentUserName(): String = auth.currentUser?.displayName ?: "Camarero"

    fun isAdmin(): Boolean {
        return auth.currentUser?.email == "lukaperalta03@gmail.com"
    }


    fun signInWithGoogle(context: Context) {
        _uiState.value = LoginUiState.Loading

        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) // false = muestra todas las cuentas Google
            .setServerClientId(WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)          // El usuario elige manualmente
            .build()

        val credentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewModelScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = credentialRequest,
                    context = context
                )
                authenticateWithFirebase(result.credential)

            } catch (e: NoCredentialException) {
                _uiState.value = LoginUiState.Error(
                    "No hay cuentas de Google disponibles en este dispositivo."
                )
            } catch (e: GetCredentialCancellationException) {
                _uiState.value = LoginUiState.Idle
            } catch (e: GetCredentialException) {
                _uiState.value = LoginUiState.Error(
                    "Error al obtener credenciales: ${e.localizedMessage}"
                )
            }
        }
    }


    private suspend fun authenticateWithFirebase(credential: Credential) {
        try {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken

            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

            val authResult = auth.signInWithCredential(firebaseCredential).await()
            val user = authResult.user

            if (user != null) {
                _uiState.value = LoginUiState.Success(
                    displayName = user.displayName ?: "Camarero"
                )
            } else {
                _uiState.value = LoginUiState.Error("No se pudo obtener el usuario de Firebase.")
            }

        } catch (e: Exception) {
            _uiState.value = LoginUiState.Error(
                "Error de autenticacion con Firebase: ${e.localizedMessage}"
            )
        }
    }


    fun signOut() {
        auth.signOut()
        _uiState.value = LoginUiState.Idle
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }

    companion object {
        const val WEB_CLIENT_ID = "657615653102-tnb725t5ncc6bv8i6slf0mbjjk1vlt62.apps.googleusercontent.com"
    }
}