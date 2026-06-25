package com.example.divivapp.ui.detalle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divivapp.BuildConfig
import com.example.divivapp.data.MenuItem
import com.example.divivapp.repository.MesasRepository
import com.google.ai.client.generativeai.GenerativeModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetalleUiState(
    val item: MenuItem? = null,
    val descripcionIa: String? = null,   // Respuesta generada por Gemini
    val iaLoading: Boolean = false,      // true mientras Gemini responde
    val iaError: Boolean = false,        // true si Gemini fallo o no esta configurado
    val iaErrorMessage: String? = null,  // Mensaje real de la excepcion de Gemini
    val isLoading: Boolean = true
)

@HiltViewModel
class DetalleViewModel @Inject constructor(
    private val repository: MesasRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val itemId: String = checkNotNull(savedStateHandle["menuItemId"])

    private val _uiState = MutableStateFlow(DetalleUiState())
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    init {
        cargarItem()
    }

    private fun cargarItem() {
        viewModelScope.launch {
            val item = repository.getMenuItemById(itemId)
            _uiState.update { it.copy(item = item, isLoading = false) }
            if (item != null) {
                generarDescripcionIa(item.nombre)
            }
        }
    }

    private fun generarDescripcionIa(nombrePlato: String) {
        if (BuildConfig.GEMINI_API_KEY.isBlank()) {
            _uiState.update { it.copy(iaError = true, iaErrorMessage = "La API Key está vacía en BuildConfig.") }
            return
        }

        _uiState.update { it.copy(iaLoading = true) }

        viewModelScope.launch {
            try {
                val model = GenerativeModel(
                    modelName = "gemini-pro",
                    apiKey = BuildConfig.GEMINI_API_KEY
                )
                val prompt = """
                    Eres el asistente de un restaurante argentino. 
                    Describe brevemente el plato "$nombrePlato" en exactamente 2 oraciones: 
                    menciona sus ingredientes principales, su preparacion tipica y una sugerencia 
                    de maridaje o acompanamiento. Usa un tono calido y apetitoso. 
                    Responde en espanol rioplatense.
                """.trimIndent()

                val respuesta = model.generateContent(prompt)
                _uiState.update {
                    it.copy(
                        descripcionIa = respuesta.text,
                        iaLoading = false
                    )
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Error desconocido"
                val mensajeAmigable = if (errorMsg.contains("MissingFieldException") || errorMsg.contains("NOT_FOUND")) {
                    "Modelo no disponible en tu region o API Key incorrecta. Revisa tu consola de Google AI Studio."
                } else {
                    errorMsg
                }
                _uiState.update { it.copy(iaLoading = false, iaError = true, iaErrorMessage = mensajeAmigable) }
            }
        }
    }
}
