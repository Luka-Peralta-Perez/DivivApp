package com.example.divivapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class PedidoConNombre(
    val pedido_id: String,       // pedido.id
    val comensal_id: String,     // pedido.comensal_id
    val precio_fijado: Double,   // pedido.precio_fijado
    val nombre_plato: String     // menu_item.nombre
)

@Dao
interface MesaDao {


    @Query("SELECT * FROM mesa WHERE camarero_uid = :uid ORDER BY numero ASC")
    fun getMesasByUid(uid: String): Flow<List<Mesa>>

    @Query("SELECT * FROM mesa WHERE id = :mesaId")
    fun getMesaById(mesaId: String): Flow<Mesa?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMesa(mesa: Mesa)

    @Update
    suspend fun updateMesa(mesa: Mesa)

    @Query("UPDATE mesa SET estado = :estado WHERE id = :mesaId")
    suspend fun updateEstadoMesa(mesaId: String, estado: String)



    @Query("SELECT * FROM comensal WHERE mesa_id = :mesaId ORDER BY nombre ASC")
    fun getComensalesByMesa(mesaId: String): Flow<List<Comensal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComensal(comensal: Comensal)

    @Query("UPDATE comensal SET estado_pago = :estado WHERE id = :comensalId")
    suspend fun updateEstadoPago(comensalId: String, estado: String)



    @Query("SELECT * FROM pedido WHERE comensal_id = :comensalId")
    fun getPedidosByComensal(comensalId: String): Flow<List<Pedido>>

    @Query("""
        SELECT p.id AS pedido_id,
               p.comensal_id,
               p.precio_fijado,
               m.nombre AS nombre_plato
        FROM pedido p
        INNER JOIN menu_item m ON p.producto_id = m.id
        WHERE p.comensal_id = :comensalId
    """)
    fun getPedidosConNombresByComensal(comensalId: String): Flow<List<PedidoConNombre>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPedido(pedido: Pedido)

    @Query("UPDATE pedido SET comensal_id = :nuevoComensalId WHERE id = :pedidoId")
    suspend fun reasignarPedido(pedidoId: String, nuevoComensalId: String)

    @Delete
    suspend fun deletePedido(pedido: Pedido)



    @Query("SELECT * FROM menu_item ORDER BY nombre ASC")
    fun getAllMenuItems(): Flow<List<MenuItem>>

    @Query("SELECT * FROM menu_item WHERE nombre LIKE '%' || :query || '%' ORDER BY nombre ASC")
    fun searchMenuItems(query: String): Flow<List<MenuItem>>

    @Query("SELECT * FROM menu_item WHERE id = :itemId")
    suspend fun getMenuItemById(itemId: String): MenuItem?

    @Query("SELECT COUNT(*) FROM menu_item")
    suspend fun countMenuItems(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItems(items: List<MenuItem>)

    @Query("UPDATE menu_item SET health_labels = :labels WHERE id = :id")
    suspend fun updateHealthLabels(id: String, labels: String)

    @Query("UPDATE menu_item SET calorias = :calorias, proteinas = :proteinas, carbohidratos = :carbos, grasas = :grasas WHERE id = :id")
    suspend fun updateNutricion(id: String, calorias: Double, proteinas: Double, carbos: Double, grasas: Double)

    @Query("UPDATE menu_item SET image_url = :url WHERE id = :itemId")
    suspend fun updateImageUrl(itemId: String, url: String)

    @Query("SELECT * FROM menu_item")
    suspend fun getMenuItemsOnce(): List<MenuItem>

    @Update
    suspend fun updateMenuItem(item: MenuItem)

    @Delete
    suspend fun deleteMenuItem(item: MenuItem)
}