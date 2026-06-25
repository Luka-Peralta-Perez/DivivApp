package com.example.divivapp.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divivapp.data.Mesa
import com.example.divivapp.repository.MesasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val mesas: List<Mesa> = emptyList(),
    val mostrarDialogo: Boolean = false,
    val inputNumeroMesa: String = "",
    val inputNombresComensales: String = "",
    val errorValidacion: String? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: MesasRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observarMesas()
    }

    private fun observarMesas() {
        viewModelScope.launch {
            repository.getMesasFlow().collect { mesas ->
                _uiState.update { it.copy(mesas = mesas) }
            }
        }
    }


    fun abrirDialogoNuevaMesa() {
        _uiState.update { it.copy(mostrarDialogo = true, errorValidacion = null) }
    }

    fun cerrarDialogo() {
        _uiState.update {
            it.copy(
                mostrarDialogo = false,
                inputNumeroMesa = "",
                inputNombresComensales = "",
                errorValidacion = null
            )
        }
    }

    fun actualizarNumeroMesa(valor: String) {
        _uiState.update { it.copy(inputNumeroMesa = valor) }
    }

    fun actualizarNombresComensales(valor: String) {
        _uiState.update { it.copy(inputNombresComensales = valor) }
    }

    fun crearMesa() {
        val numero = _uiState.value.inputNumeroMesa.toIntOrNull()
        val nombresRaw = _uiState.value.inputNombresComensales

        if (numero == null || numero <= 0) {
            _uiState.update { it.copy(errorValidacion = "Ingresa un numero de mesa valido.") }
            return
        }

        val mesaExistente = _uiState.value.mesas.find { it.numero == numero && it.estado != "finalizada" }
        if (mesaExistente != null) {
            _uiState.update { it.copy(errorValidacion = "La mesa $numero ya esta abierta.") }
            return
        }

        val nombres = nombresRaw.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (nombres.isEmpty()) {
            _uiState.update { it.copy(errorValidacion = "Ingresa al menos un nombre de comensal.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.crearMesaConComensales(
                    numero = numero,
                    nombresComensales = nombres
                )
                cerrarDialogo()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorValidacion = "Error al crear la mesa: ${e.localizedMessage}")
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
