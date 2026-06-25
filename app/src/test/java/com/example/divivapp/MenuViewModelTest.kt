package com.example.divivapp.ui.menu

import androidx.lifecycle.SavedStateHandle
import com.example.divivapp.MainDispatcherRule
import com.example.divivapp.data.Comensal
import com.example.divivapp.data.MenuItem
import com.example.divivapp.repository.MesasRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// Tests unitarios para MenuViewModel (CU-04).
// Valido los estados de UI mas importantes: carga inicial de items,
// seleccion de comensal, confirmacion de asignacion y manejo de errores.
//
// Uso MockK para reemplazar MesasRepository con un doble de prueba que
// devuelve datos controlados sin tocar Room ni Edamam reales.
// MainDispatcherRule reemplaza Dispatchers.Main por UnconfinedTestDispatcher
// para evitar el error de Looper en el entorno de tests unitarios.
@OptIn(ExperimentalCoroutinesApi::class)
class MenuViewModelTest {

    // Regla que reemplaza Dispatchers.Main antes de cada test.
    // Sin esto, viewModelScope.launch{} lanza IllegalStateException
    // porque el Looper de Android no esta disponible en tests unitarios.
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: MesasRepository
    private lateinit var viewModel: MenuViewModel
    private lateinit var savedStateHandle: SavedStateHandle

    // Datos de prueba reutilizables entre tests
    private val mesaId = "mesa_test_001"
    private val itemTest = MenuItem(
        id = "item_001",
        nombre = "Bife de Chorizo",
        ingredientes = "[\"bife\"]",
        health_labels = "[\"Sin TACC\"]",
        image_url = "",
        precio = 3800.0
    )
    private val comensalTest = Comensal(
        id = "comensal_001",
        mesa_id = mesaId,
        nombre = "Ana"
    )

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        savedStateHandle = SavedStateHandle(mapOf("mesaId" to mesaId))

        coEvery { repository.getComensalesFlow(mesaId) } returns flowOf(listOf(comensalTest))
        coEvery { repository.getMenuItemsFlow() } returns flowOf(listOf(itemTest))
        coEvery { repository.searchMenuItems(any()) } returns flowOf(listOf(itemTest))
        coEvery { repository.sincronizarMenuConEdamam() } returns Unit

        viewModel = MenuViewModel(repository, savedStateHandle)
    }

    // Test 1: La lista de items debe cargarse desde Room al inicializar el ViewModel.
    @Test
    fun `al inicializar, el estado tiene los items del menu`() = runTest {
        advanceUntilIdle()
        assertEquals(listOf(itemTest), viewModel.uiState.value.items)
    }

    // Test 2: Los comensales de la mesa deben estar disponibles en el estado.
    @Test
    fun `al inicializar, el estado tiene los comensales de la mesa`() = runTest {
        advanceUntilIdle()
        assertEquals(listOf(comensalTest), viewModel.uiState.value.comensales)
    }

    // Test 3: Abrir el sheet debe guardar el item seleccionado y preseleccionar el primer comensal.
    @Test
    fun `abrirSheetAsignacion guarda el item y preselecciona el primer comensal`() = runTest {
        advanceUntilIdle()
        viewModel.abrirSheetAsignacion(itemTest)
        assertEquals(itemTest, viewModel.uiState.value.itemSeleccionado)
        assertEquals(comensalTest.id, viewModel.uiState.value.comensalSeleccionadoId)
    }

    // Test 4: Cerrar el sheet debe limpiar la seleccion.
    @Test
    fun `cerrarSheet limpia el item y el comensal seleccionado`() = runTest {
        advanceUntilIdle()
        viewModel.abrirSheetAsignacion(itemTest)
        viewModel.cerrarSheet()
        assertNull(viewModel.uiState.value.itemSeleccionado)
        assertNull(viewModel.uiState.value.comensalSeleccionadoId)
    }

    // Test 5: Confirmar asignacion debe llamar a agregarPedido en el Repository.
    @Test
    fun `confirmarAsignacion llama a repository agregarPedido con los datos correctos`() = runTest {
        advanceUntilIdle()
        viewModel.abrirSheetAsignacion(itemTest)
        viewModel.confirmarAsignacion()
        advanceUntilIdle()

        coVerify {
            repository.agregarPedido(
                comensalId = comensalTest.id,
                productoId = itemTest.id,
                precioFijado = itemTest.precio
            )
        }
    }

    // Test 6: Confirmar asignacion sin item seleccionado no debe llamar al Repository.
    @Test
    fun `confirmarAsignacion sin item seleccionado no llama al repository`() = runTest {
        advanceUntilIdle()
        viewModel.confirmarAsignacion()
        advanceUntilIdle()

        coVerify(exactly = 0) {
            repository.agregarPedido(any(), any(), any())
        }
    }

    // Test 7: La busqueda debe actualizar el texto en el estado.
    @Test
    fun `actualizarBusqueda actualiza el estado de busqueda`() = runTest {
        viewModel.actualizarBusqueda("bife")
        assertEquals("bife", viewModel.uiState.value.busqueda)
    }

    // Test 8: El toast debe limpiarse al llamar limpiarToast.
    @Test
    fun `limpiarToast setea mensajeToast a null`() = runTest {
        advanceUntilIdle()
        viewModel.abrirSheetAsignacion(itemTest)
        viewModel.confirmarAsignacion()
        advanceUntilIdle()
        viewModel.limpiarToast()
        assertNull(viewModel.uiState.value.mensajeToast)
    }
}
