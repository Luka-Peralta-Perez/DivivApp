package com.example.divivapp.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.divivapp.data.MenuItem
import com.example.divivapp.repository.MesasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AdminUiState(
    val menuItems: List<MenuItem> = emptyList(),
    val isModalOpen: Boolean = false,
    val isUploading: Boolean = false,
    val itemEnEdicion: MenuItem? = null,
    val inputNombre: String = "",
    val inputCategoria: String = "",
    val inputIngredientes: String = "", // Separados por coma
    val inputPrecio: String = "",
    val inputImageUrl: String = "", // URL publica de Firebase Storage
    val selectedImageUri: android.net.Uri? = null, // URI local de la galeria
    val errorValidacion: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val repository: MesasRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        observarMenu()
    }

    private fun observarMenu() {
        viewModelScope.launch {
            repository.getAllMenuItemsFlow().collect { items ->
                _uiState.update { it.copy(menuItems = items) }
            }
        }
    }

    fun abrirModalCrear() {
        _uiState.update {
            it.copy(
                isModalOpen = true,
                itemEnEdicion = null,
                inputNombre = "",
                inputCategoria = "General",
                inputIngredientes = "",
                inputPrecio = "",
                inputImageUrl = "",
                selectedImageUri = null,
                errorValidacion = null
            )
        }
    }

    fun abrirModalEditar(item: MenuItem) {
        val ingredientesList = item.ingredientes
            .removePrefix("[")
            .removeSuffix("]")
            .replace("\"", "")
        
        _uiState.update {
            it.copy(
                isModalOpen = true,
                itemEnEdicion = item,
                inputNombre = item.nombre,
                inputCategoria = item.categoria,
                inputIngredientes = ingredientesList,
                inputPrecio = item.precio.toString(),
                inputImageUrl = item.image_url,
                selectedImageUri = null,
                errorValidacion = null
            )
        }
    }

    fun cerrarModal() {
        _uiState.update { it.copy(isModalOpen = false) }
    }

    fun updateInputNombre(v: String) = _uiState.update { it.copy(inputNombre = v) }
    fun updateInputCategoria(v: String) = _uiState.update { it.copy(inputCategoria = v) }
    fun updateInputIngredientes(v: String) = _uiState.update { it.copy(inputIngredientes = v) }
    fun updateInputPrecio(v: String) = _uiState.update { it.copy(inputPrecio = v) }
    fun updateInputImageUrl(v: String) = _uiState.update { it.copy(inputImageUrl = v) }
    fun updateSelectedImageUri(uri: android.net.Uri?) = _uiState.update { it.copy(selectedImageUri = uri) }

    fun guardarPlato() {
        val st = _uiState.value
        val nombre = st.inputNombre.trim()
        val cat = st.inputCategoria.trim()
        val prec = st.inputPrecio.toDoubleOrNull()
        
        if (nombre.isEmpty() || cat.isEmpty() || prec == null || prec <= 0) {
            _uiState.update { it.copy(errorValidacion = "Revisa los campos (precio debe ser > 0)") }
            return
        }

        _uiState.update { it.copy(isUploading = true, errorValidacion = null) }

        viewModelScope.launch {
            try {
                var finalImageUrl = st.inputImageUrl.trim()
                if (st.selectedImageUri != null) {
                    finalImageUrl = repository.uploadImage(st.selectedImageUri)
                }

                val listaIngredientes = st.inputIngredientes.split(",").map { "\"${it.trim()}\"" }
                val jsonIngredientes = "[${listaIngredientes.joinToString(",")}]"

                val item = MenuItem(
                    id = st.itemEnEdicion?.id ?: UUID.randomUUID().toString(),
                    nombre = nombre,
                    categoria = cat,
                    ingredientes = jsonIngredientes,
                    health_labels = st.itemEnEdicion?.health_labels,
                    image_url = finalImageUrl,
                    precio = prec
                )

                repository.guardarPlato(item)
                cerrarModal()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorValidacion = "Error al guardar: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isUploading = false) }
            }
        }
    }

    fun eliminarPlato(item: MenuItem) {
        viewModelScope.launch {
            repository.eliminarPlato(item)
        }
    }
}
