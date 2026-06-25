package com.example.divivapp.ui.checkout

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

data class ResumenComensal(
    val comensal: Comensal,
    val pedidos: List<PedidoConNombre>,
    val subtotal: Double,
    val pagado: Boolean
)

val METODOS_PAGO = listOf("Efectivo", "Transferencia", "Tarjeta debito", "Tarjeta credito")

data class CheckoutUiState(
    val numeroMesa: Int = 0,
    val resumenes: List<ResumenComensal> = emptyList(),
    val totalGeneral: Double = 0.0,
    val totalPendiente: Double = 0.0,
    val comensalesPagados: Int = 0,
    val totalComensales: Int = 0,
    val comensalEnPago: ResumenComensal? = null,
    val metodoPagoSeleccionado: String = "Efectivo",
    val mesaCerrada: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val repository: MesasRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val mesaId: String = checkNotNull(savedStateHandle["mesaId"])
    private var numeroMesaActual: Int = 0

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    init {
        observarMesa()
        observarComensalesYPedidos()
    }

    private fun observarMesa() {
        viewModelScope.launch {
            repository.getMesaFlow(mesaId).collect { mesa ->
                if (mesa != null) {
                    _uiState.update { it.copy(numeroMesa = mesa.numero) }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observarComensalesYPedidos() {
        viewModelScope.launch {
            repository.getComensalesFlow(mesaId)
                .flatMapLatest { comensales ->
                    if (comensales.isEmpty()) {
                        flowOf(emptyList<ResumenComensal>())
                    } else {
                        val flowsPorComensal = comensales.map { comensal ->
                            repository.getPedidosConNombres(comensal.id)
                                .map { pedidos ->
                                    ResumenComensal(
                                        comensal = comensal,
                                        pedidos = pedidos,
                                        subtotal = pedidos.sumOf { it.precio_fijado },
                                        pagado = comensal.estado_pago == "pagado"
                                    )
                                }
                        }
                        combine(flowsPorComensal) { it.toList() }
                    }
                }
                .catch {
                    _uiState.update { it.copy(isLoading = false) }
                }
                .collect { resumenes ->
                    _uiState.update {
                        it.copy(
                            resumenes = resumenes,
                            totalGeneral = resumenes.sumOf { r -> r.subtotal },
                            totalPendiente = resumenes.filter { r -> !r.pagado }.sumOf { r -> r.subtotal },
                            comensalesPagados = resumenes.count { r -> r.pagado },
                            totalComensales = resumenes.size,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun abrirModalPago(resumen: ResumenComensal) {
        _uiState.update {
            it.copy(
                comensalEnPago = resumen,
                metodoPagoSeleccionado = "Efectivo"
            )
        }
    }

    fun cerrarModal() {
        _uiState.update { it.copy(comensalEnPago = null) }
    }

    fun seleccionarMetodoPago(metodo: String) {
        _uiState.update { it.copy(metodoPagoSeleccionado = metodo) }
    }

    fun confirmarPago() {
        val comensal = _uiState.value.comensalEnPago?.comensal ?: return
        viewModelScope.launch {
            try {
                repository.marcarComensalPagado(comensal.id)
                cerrarModal()
            } catch (e: Exception) {
            }
        }
    }

    fun cerrarMesa() {
        viewModelScope.launch {
            try {
                repository.cerrarMesa(mesaId)
                _uiState.update { it.copy(mesaCerrada = true) }
            } catch (e: Exception) {
            }
        }
    }
}
