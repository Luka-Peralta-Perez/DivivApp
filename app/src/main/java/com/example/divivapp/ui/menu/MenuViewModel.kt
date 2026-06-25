package com.example.divivapp.ui.menu

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divivapp.data.Comensal
import com.example.divivapp.data.MenuItem
import com.example.divivapp.repository.MesasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MenuUiState(
    val items: List<MenuItem> = emptyList(),
    val comensales: List<Comensal> = emptyList(),
    val busqueda: String = "",
    val categoriaActiva: String = "Todos",
    val itemSeleccionado: MenuItem? = null,
    val comensalSeleccionadoId: String? = null,
    val isLoading: Boolean = false,
    val mensajeToast: String? = null
)

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val repository: MesasRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val mesaId: String = checkNotNull(savedStateHandle["mesaId"])

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    private val _busqueda = MutableStateFlow("")

    init {
        observarComensales()
        observarMenu()
        viewModelScope.launch {
            repository.sincronizarMenuConEdamam()
        }
    }

    private fun observarComensales() {
        viewModelScope.launch {
            repository.getComensalesFlow(mesaId).collect { comensales ->
                _uiState.update { it.copy(comensales = comensales) }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun observarMenu() {
        viewModelScope.launch {
            _busqueda
                .debounce(300L)
                .flatMapLatest { texto ->
                    if (texto.isBlank()) {
                        repository.getMenuItemsFlow()
                    } else {
                        repository.searchMenuItems(texto)
                    }
                }
                .collect { items ->
                    _uiState.update { it.copy(items = items) }
                }
        }
    }


    fun actualizarBusqueda(texto: String) {
        _busqueda.value = texto
        _uiState.update { it.copy(busqueda = texto) }
    }

    fun seleccionarCategoria(categoria: String) {
        _uiState.update { it.copy(categoriaActiva = categoria) }
    }

    fun abrirSheetAsignacion(item: MenuItem) {
        _uiState.update {
            it.copy(
                itemSeleccionado = item,
                comensalSeleccionadoId = it.comensales.firstOrNull()?.id
            )
        }
    }

    fun cerrarSheet() {
        _uiState.update { it.copy(itemSeleccionado = null, comensalSeleccionadoId = null) }
    }

    fun seleccionarComensal(comensalId: String) {
        _uiState.update { it.copy(comensalSeleccionadoId = comensalId) }
    }

    fun confirmarAsignacion() {
        val state = _uiState.value
        val item = state.itemSeleccionado ?: return
        val comensalId = state.comensalSeleccionadoId ?: return

        viewModelScope.launch {
            try {
                repository.agregarPedido(
                    comensalId = comensalId,
                    productoId = item.id,
                    precioFijado = item.precio
                )
                val nombreComensal = state.comensales
                    .find { it.id == comensalId }?.nombre ?: "comensal"
                cerrarSheet()
                mostrarToast("${item.nombre} asignado a $nombreComensal")
            } catch (e: Exception) {
                mostrarToast("Error al guardar el pedido")
            }
        }
    }

    private fun mostrarToast(mensaje: String) {
        _uiState.update { it.copy(mensajeToast = mensaje) }
    }

    fun limpiarToast() {
        _uiState.update { it.copy(mensajeToast = null) }
    }
}
