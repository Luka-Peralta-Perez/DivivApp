package com.example.divivapp.repository

import com.example.divivapp.BuildConfig
import com.example.divivapp.data.Comensal
import com.example.divivapp.data.Mesa
import com.example.divivapp.data.MesaDao
import com.example.divivapp.data.MenuItem
import com.example.divivapp.data.Pedido
import com.example.divivapp.data.PedidoConNombre
import com.example.divivapp.network.EdamamApiService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MesasRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mesaDao: MesaDao,
    private val edamamApiService: EdamamApiService,
    private val firestore: FirebaseFirestore = Firebase.firestore,
    private val auth: FirebaseAuth = Firebase.auth
) {

    private val storage = FirebaseStorage.getInstance()

    fun getMesasFlow(): Flow<List<Mesa>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return mesaDao.getMesasByUid(uid)
    }

    fun getMesaFlow(mesaId: String): Flow<Mesa?> {
        return mesaDao.getMesaById(mesaId)
    }


    fun getComensalesFlow(mesaId: String): Flow<List<Comensal>> {
        return mesaDao.getComensalesByMesa(mesaId)
    }


    fun getMenuItemsFlow(): Flow<List<MenuItem>> {
        return mesaDao.getAllMenuItems()
    }


    fun searchMenuItems(query: String): Flow<List<MenuItem>> {
        return mesaDao.searchMenuItems(query)
    }


    suspend fun getMenuItemById(itemId: String): MenuItem? {
        return mesaDao.getMenuItemById(itemId)
    }


    fun getPedidosConNombres(comensalId: String): Flow<List<PedidoConNombre>> {
        return mesaDao.getPedidosConNombresByComensal(comensalId)
    }


    suspend fun enriquecerItemConEdamam(
        item: MenuItem,
        appId: String = BuildConfig.EDAMAM_APP_ID,
        appKey: String = BuildConfig.EDAMAM_APP_KEY
    ) {
        if (appId.isBlank() || appKey.isBlank()) return

        try {
            val respuesta = edamamApiService.buscarAlimento(
                appId = appId,
                appKey = appKey,
                ingrediente = item.nombre
            )

            // Usamos el primer 'parsed' si existe, si no, el primer 'hint'
            val foodInfo = respuesta.parsed.firstOrNull()?.food 
                ?: respuesta.hints.firstOrNull()?.food

            val cal = foodInfo?.nutrients?.calorias ?: -1.0
            val prot = foodInfo?.nutrients?.proteinas ?: -1.0
            val carb = foodInfo?.nutrients?.carbohidratos ?: -1.0
            val gras = foodInfo?.nutrients?.grasas ?: -1.0
            
            // Priorizar la imagen actual del item. Solo usar la de Edamam si el item no tiene imagen.
            val img = if (item.image_url.isNotBlank()) item.image_url else (foodInfo?.image ?: "")
            
            val nombreLower = item.nombre.lowercase()
            val tags = mutableListOf<String>()
            if (nombreLower.contains("queso") || nombreLower.contains("leche") || nombreLower.contains("crema") || nombreLower.contains("carbonara")) tags.add("Lácteos")
            if (nombreLower.contains("pan") || nombreLower.contains("fideo") || nombreLower.contains("pasta") || nombreLower.contains("milanesa") || nombreLower.contains("pizza")) tags.add("Gluten")
            if (nombreLower.contains("mani") || nombreLower.contains("nuez")) tags.add("Frutos Secos")
            if (nombreLower.contains("pescado") || nombreLower.contains("salmon") || nombreLower.contains("atun")) tags.add("Pescado")
            if (nombreLower.contains("huevo") || nombreLower.contains("carbonara")) tags.add("Huevo")

            val jsonTags = com.google.gson.Gson().toJson(tags)

            mesaDao.updateMenuItem(item.copy(
                calorias = cal, 
                proteinas = prot,
                carbohidratos = carb,
                grasas = gras,
                image_url = img, 
                health_labels = jsonTags
            ))

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    suspend fun sincronizarMenuConEdamam(
        appId: String = BuildConfig.EDAMAM_APP_ID,
        appKey: String = BuildConfig.EDAMAM_APP_KEY
    ) {
        val items = mesaDao.getMenuItemsOnce()
        items.filter { 
            it.image_url.isBlank() || 
            it.health_labels == null || 
            it.calorias == null || 
            it.calorias == 0.0 ||
            it.proteinas == null ||
            it.proteinas == 0.0
        }
            .forEach { item -> enriquecerItemConEdamam(item, appId, appKey) }
    }


    suspend fun crearMesaConComensales(
        numero: Int,
        nombresComensales: List<String>
    ) {
        val uid = auth.currentUser?.uid ?: return
        val mesaId = java.util.UUID.randomUUID().toString()

        val mesa = Mesa(
            id = mesaId,
            camarero_uid = uid,
            numero = numero,
            estado = "abierta"
        )
        mesaDao.insertMesa(mesa)

        val comensales = nombresComensales.map { nombre ->
            Comensal(
                id = java.util.UUID.randomUUID().toString(),
                mesa_id = mesaId,
                nombre = nombre.trim()
            )
        }
        comensales.forEach { mesaDao.insertComensal(it) }

        try {
            val mesaData = hashMapOf(
                "id" to mesaId,
                "camarero_uid" to uid,
                "numero" to numero,
                "estado" to "abierta",
                "consumos_agrupados" to emptyMap<String, Any>(),
                "total_facturado" to 0.0
            )
            firestore.collection("mesas_activas")
                .document(mesaId)
                .set(mesaData)
                .await()
        } catch (e: Exception) {
        }
    }


    suspend fun agregarPedido(
        comensalId: String,
        productoId: String,
        precioFijado: Double
    ) {
        val pedido = Pedido(
            id = java.util.UUID.randomUUID().toString(),
            comensal_id = comensalId,
            producto_id = productoId,
            precio_fijado = precioFijado
        )
        mesaDao.insertPedido(pedido)
    }


    suspend fun reasignarPedido(pedidoId: String, nuevoComensalId: String) {
        mesaDao.reasignarPedido(pedidoId, nuevoComensalId)
    }


    suspend fun eliminarPedido(pedido: Pedido) {
        mesaDao.deletePedido(pedido)
    }


    suspend fun marcarComensalPagado(comensalId: String) {
        mesaDao.updateEstadoPago(comensalId, "pagado")
    }


    suspend fun cerrarMesa(mesaId: String) {
        mesaDao.updateEstadoMesa(mesaId, "finalizada")
        try {
            val ref = firestore.collection("mesas_activas").document(mesaId)
            val snapshot = ref.get().await()
            if (snapshot.exists()) {
                val data = snapshot.data ?: emptyMap<String, Any>()
                firestore.collection("mesas_historico").document(mesaId).set(data).await()
                ref.delete().await()
            }
        } catch (e: Exception) {
        }
    }


    suspend fun guardarPlato(item: MenuItem) {
        mesaDao.insertMenuItems(listOf(item))
    }

    suspend fun eliminarPlato(item: MenuItem) {
        mesaDao.deleteMenuItem(item)
    }

    suspend fun uploadImage(uri: android.net.Uri): String {
        try {
            val fileName = "plato_${java.util.UUID.randomUUID()}.jpg"
            val file = java.io.File(context.filesDir, fileName)
            
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            return file.absolutePath
        } catch (e: Exception) {
            throw Exception("Error al guardar la imagen localmente: ${e.message}")
        }
    }

    fun getAllMenuItemsFlow() = mesaDao.getAllMenuItems()
}