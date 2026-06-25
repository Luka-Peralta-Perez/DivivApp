package com.example.divivapp.repository

import com.example.divivapp.MainDispatcherRule
import com.example.divivapp.data.MesaDao
import com.example.divivapp.data.MenuItem
import com.example.divivapp.network.EdamamApiService
import com.example.divivapp.network.EdamamFood
import com.example.divivapp.network.EdamamHint
import com.example.divivapp.network.EdamamResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// Tests de integracion para MesasRepository.
// Valido la interaccion entre el Repository, el DAO (Room) y EdamamApiService (Retrofit).
//
// Firebase (FirebaseFirestore y FirebaseAuth) se inyecta como mock para evitar que
// el constructor llame a Firebase.firestore / Firebase.auth, que requieren un contexto
// de Android real que no esta disponible en tests unitarios JVM puros.
//
// IMPORTANTE: buscarAlimento tiene 4 parametros (appId, appKey, ingrediente, nutritionType).
// MockK requiere que todos los matchers coincidan — uso any() para los que no me importan.
@OptIn(ExperimentalCoroutinesApi::class)
class MesasRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var mesaDao: MesaDao
    private lateinit var edamamApiService: EdamamApiService
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var repository: MesasRepository
    private lateinit var context: android.content.Context

    // Item de prueba con imagen y health labels vacios (como si fuera del seed inicial)
    private val itemSinEnriquecer = MenuItem(
        id = "item_001",
        nombre = "Bife de Chorizo",
        ingredientes = "[\"bife\"]",
        health_labels = null,
        image_url = "",
        precio = 3800.0
    )

    @Before
    fun setUp() {
        io.mockk.mockkStatic(android.util.Log::class)
        io.mockk.every { android.util.Log.d(any(), any()) } returns 0
        io.mockk.every { android.util.Log.e(any(), any(), any()) } returns 0
        io.mockk.every { android.util.Log.e(any(), any()) } returns 0
        
        context = mockk(relaxed = true)
        mesaDao = mockk(relaxed = true)
        edamamApiService = mockk()
        // MockK para Firebase: relaxed = true evita que las llamadas reales se ejecuten
        firestore = mockk(relaxed = true)
        auth = mockk(relaxed = true)
        // Inyecto los mocks de Firebase y Context
        repository = MesasRepository(context, mesaDao, edamamApiService, firestore, auth)
    }

    // Test 1: Cuando Edamam devuelve imagen y health labels,
    //         el Repository debe persistirlos en Room via el DAO.
    @Test
    fun `enriquecerItemConEdamam actualiza imagen y health labels en Room`() = runTest {
        val respuestaEdamam = EdamamResponse(
            hints = listOf(
                EdamamHint(
                    food = EdamamFood(
                        foodId = "food_001",
                        label = "Bife de Chorizo",
                        image = "https://www.edamam.com/food-img/bife.jpg",
                        healthLabels = listOf("HIGH_PROTEIN", "GLUTEN_FREE")
                    )
                )
            )
        )
        // 4 params: appId, appKey, ingrediente, nutritionType (con default "cooking")
        coEvery {
            edamamApiService.buscarAlimento(any(), any(), "Bife de Chorizo", any())
        } returns respuestaEdamam

        val urlCapturada = slot<String>()
        coEvery { mesaDao.updateImageUrl("item_001", capture(urlCapturada)) } returns Unit

        val labelsCapturadas = slot<String>()
        coEvery { mesaDao.updateHealthLabels("item_001", capture(labelsCapturadas)) } returns Unit

        repository.enriquecerItemConEdamam(itemSinEnriquecer, appId = "test_id", appKey = "test_key")

        // Verifico que se llamo a updateImageUrl con la URL correcta
        coVerify { mesaDao.updateImageUrl("item_001", any()) }
        assertEquals("https://www.edamam.com/food-img/bife.jpg", urlCapturada.captured)

        // Verifico que se llamo a updateHealthLabels con las etiquetas correctas
        coVerify { mesaDao.updateHealthLabels("item_001", any()) }
        assert(labelsCapturadas.captured.contains("HIGH_PROTEIN"))
        assert(labelsCapturadas.captured.contains("GLUTEN_FREE"))
    }

    // Test 2: Si Edamam no devuelve hints ni parsed, el DAO no debe ser llamado.
    @Test
    fun `enriquecerItemConEdamam con respuesta vacia no llama al DAO`() = runTest {
        coEvery {
            edamamApiService.buscarAlimento(any(), any(), any(), any())
        } returns EdamamResponse(hints = emptyList(), parsed = emptyList())

        repository.enriquecerItemConEdamam(itemSinEnriquecer, appId = "test_id", appKey = "test_key")

        coVerify(exactly = 0) { mesaDao.updateImageUrl(any(), any()) }
        coVerify(exactly = 0) { mesaDao.updateHealthLabels(any(), any()) }
    }

    // Test 3: Si Edamam lanza una excepcion (sin red, timeout, etc.),
    //         el Repository debe capturarla sin propagar el error.
    @Test
    fun `enriquecerItemConEdamam con excepcion de red no propaga el error`() = runTest {
        coEvery {
            edamamApiService.buscarAlimento(any(), any(), any(), any())
        } throws Exception("Network error: timeout")

        // No debe lanzar excepcion — el Repository la absorbe
        repository.enriquecerItemConEdamam(itemSinEnriquecer, appId = "test_id", appKey = "test_key")

        coVerify(exactly = 0) { mesaDao.updateImageUrl(any(), any()) }
    }

    // Test 4: sincronizarMenuConEdamam solo debe enriquecer items incompletos.
    //         Un item esta completo cuando tiene AMBOS: imagen Y health labels.
    //         Solo se consulta Edamam para items que les falte al menos uno de los dos.
    //
    //         Verifico que buscarAlimento se llame exactamente 1 vez en total,
    //         lo que confirma que el item ya enriquecido no fue consultado.
    @Test
    fun `sincronizarMenuConEdamam solo llama a Edamam para items sin imagen`() = runTest {
        // itemConImagen tiene imagen Y health labels — esta completamente enriquecido
        val itemConImagen = itemSinEnriquecer.copy(
            id = "item_002",
            nombre = "Ensalada Cesar",
            image_url = "https://existing.url/imagen.jpg",
            health_labels = "[\"HIGH_PROTEIN\"]"  // ya enriquecido: no debe consultarse
        )
        coEvery { mesaDao.getMenuItemsOnce() } returns listOf(itemSinEnriquecer, itemConImagen)
        coEvery { edamamApiService.buscarAlimento(any(), any(), any(), any()) } returns EdamamResponse()

        repository.sincronizarMenuConEdamam(appId = "test_id", appKey = "test_key")

        // Verifico que buscarAlimento se llamo exactamente 1 vez en total.
        // Si ambos items se consultaran, el conteo seria 2.
        // Esto confirma que el item ya enriquecido (Ensalada Cesar) fue ignorado.
        coVerify(exactly = 1) {
            edamamApiService.buscarAlimento(any(), any(), any(), any())
        }
    }
}
