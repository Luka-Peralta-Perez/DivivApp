package com.example.divivapp.ui.edicion

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divivapp.data.Comensal
import com.example.divivapp.data.PedidoConNombre
import com.example.divivapp.repository.MesasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConsumosComensal(
    val comensal: Comensal,
    val pedidos: List<PedidoConNombre>
)

data class EdicionUiState(
    val consumos: List<ConsumosComensal> = emptyList(),
    val pedidoAReasignar: PedidoConNombre? = null,
    val comensalActualNombre: String = "",
    val comensales: List<Comensal> = emptyList(),
    val comensalDestinoId: String? = null,
    val isLoading: Boolean = true,
    val mensajeToast: String? = null
)

@HiltViewModel
class EdicionViewModel @Inject constructor(
    private val repository: MesasRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val mesaId: String = checkNotNull(savedStateHandle["mesaId"])

    private val _uiState = MutableStateFlow(EdicionUiState())
    val uiState: StateFlow<EdicionUiState> = _uiState.asStateFlow()

    init {
        observarConsumosAgrupados()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observarConsumosAgrupados() {
        viewModelScope.launch {
            repository.getComensalesFlow(mesaId)
                .flatMapLatest { comensales ->
                    if (comensales.isEmpty()) {
                        flowOf(emptyList<ConsumosComensal>() to comensales)
                    } else {
                        val flowsPorComensal = comensales.map { comensal ->
                            repository.getPedidosConNombres(comensal.id)
                                .map { pedidos -> ConsumosComensal(comensal, pedidos) }
                        }
                        combine(flowsPorComensal) { array ->
                            array.toList() to comensales
                        }
                    }
                }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false) }
                }
                .collect { (consumos, comensales) ->
                    _uiState.update {
                        it.copy(
                            consumos = consumos,
                            comensales = comensales,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun abrirSheetReasignacion(pedido: PedidoConNombre, comensalActualNombre: String) {
        _uiState.update {
            it.copy(
                pedidoAReasignar = pedido,
                comensalActualNombre = comensalActualNombre,
                comensalDestinoId = it.comensales
                    .firstOrNull { c -> c.id != pedido.comensal_id }?.id
            )
        }
    }

    fun cerrarSheet() {
        _uiState.update {
            it.copy(pedidoAReasignar = null, comensalDestinoId = null)
        }
    }

    fun seleccionarComensalDestino(comensalId: String) {
        _uiState.update { it.copy(comensalDestinoId = comensalId) }
    }

    fun confirmarReasignacion() {
        val pedido = _uiState.value.pedidoAReasignar ?: return
        val nuevoComensalId = _uiState.value.comensalDestinoId ?: return
        val nuevoNombre = _uiState.value.comensales
            .find { it.id == nuevoComensalId }?.nombre ?: "comensal"

        viewModelScope.launch {
            try {
                repository.reasignarPedido(pedido.pedido_id, nuevoComensalId)
                cerrarSheet()
                _uiState.update {
                    it.copy(mensajeToast = "${pedido.nombre_plato} reasignado a $nuevoNombre")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(mensajeToast = "Error al reasignar el pedido") }
            }
        }
    }

    fun limpiarToast() {
        _uiState.update { it.copy(mensajeToast = null) }
    }
}
