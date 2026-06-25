package com.example.divivapp.ui.edicion

import androidx.lifecycle.SavedStateHandle
import com.example.divivapp.MainDispatcherRule
import com.example.divivapp.data.Comensal
import com.example.divivapp.data.PedidoConNombre
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

// Tests unitarios para EdicionViewModel (CU-06).
// Valido la carga reactiva de consumos, el flujo de reasignacion
// y el manejo del bottom sheet de seleccion de comensal destino.
@OptIn(ExperimentalCoroutinesApi::class)
class EdicionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: MesasRepository
    private lateinit var viewModel: EdicionViewModel
    private val mesaId = "mesa_test_001"

    // Datos de prueba
    private val comensalAna = Comensal(id = "c1", mesa_id = mesaId, nombre = "Ana")
    private val comensalBeto = Comensal(id = "c2", mesa_id = mesaId, nombre = "Beto")
    private val pedidoTest = PedidoConNombre(
        pedido_id = "p1",
        comensal_id = "c1",
        precio_fijado = 3800.0,
        nombre_plato = "Bife de Chorizo"
    )

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)

        coEvery { repository.getComensalesFlow(mesaId) } returns flowOf(listOf(comensalAna, comensalBeto))
        coEvery { repository.getPedidosConNombres("c1") } returns flowOf(listOf(pedidoTest))
        coEvery { repository.getPedidosConNombres("c2") } returns flowOf(emptyList())

        viewModel = EdicionViewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle(mapOf("mesaId" to mesaId))
        )
    }

    // Test 1: Los consumos deben cargarse agrupados por comensal desde Room.
    @Test
    fun `al inicializar, los consumos se agrupan por comensal`() = runTest {
        advanceUntilIdle()
        val consumos = viewModel.uiState.value.consumos
        assertEquals(2, consumos.size)
        assertEquals(1, consumos.find { it.comensal.id == "c1" }?.pedidos?.size)
        assertEquals(0, consumos.find { it.comensal.id == "c2" }?.pedidos?.size)
    }

    // Test 2: Abrir el sheet de reasignacion debe preseleccionar el primer comensal distinto.
    @Test
    fun `abrirSheetReasignacion preselecciona comensal diferente al actual`() = runTest {
        advanceUntilIdle()
        viewModel.abrirSheetReasignacion(pedidoTest, comensalAna.nombre)
        // El pedido es de Ana (c1), el destino preseleccionado debe ser Beto (c2)
        assertEquals("c2", viewModel.uiState.value.comensalDestinoId)
        assertEquals(pedidoTest, viewModel.uiState.value.pedidoAReasignar)
    }

    // Test 3: Confirmar reasignacion debe llamar al Repository con los IDs correctos.
    @Test
    fun `confirmarReasignacion llama a repository con los argumentos correctos`() = runTest {
        advanceUntilIdle()
        viewModel.abrirSheetReasignacion(pedidoTest, comensalAna.nombre)
        viewModel.seleccionarComensalDestino("c2")
        viewModel.confirmarReasignacion()
        advanceUntilIdle()
        coVerify { repository.reasignarPedido("p1", "c2") }
    }

    // Test 4: Cerrar el sheet debe limpiar el pedido y el destino seleccionado.
    @Test
    fun `cerrarSheet limpia el estado del sheet`() = runTest {
        advanceUntilIdle()
        viewModel.abrirSheetReasignacion(pedidoTest, comensalAna.nombre)
        viewModel.cerrarSheet()
        assertNull(viewModel.uiState.value.pedidoAReasignar)
        assertNull(viewModel.uiState.value.comensalDestinoId)
    }
}
