package com.example.divivapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey



@Entity(tableName = "mesa")
data class Mesa(
    @PrimaryKey
    val id: String,                // UUID generado en Kotlin antes de la insercion
    val camarero_uid: String,      // UID del camarero autenticado en Firebase
    val numero: Int,               // Numero fisico de la mesa en el salon
    val estado: String = "abierta" // Valores posibles: abierta / cerrando / finalizada
)


@Entity(
    tableName = "comensal",
    foreignKeys = [
        ForeignKey(
            entity = Mesa::class,
            parentColumns = ["id"],
            childColumns = ["mesa_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("mesa_id")] // Indice para acelerar las queries por mesa
)
data class Comensal(
    @PrimaryKey
    val id: String,                        // UUID del comensal
    val mesa_id: String,                   // FK hacia mesa(id)
    val nombre: String,                    // Nombre ingresado por el camarero
    val estado_pago: String = "pendiente"  // pendiente / pagado
)


@Entity(
    tableName = "pedido",
    foreignKeys = [
        ForeignKey(
            entity = Comensal::class,
            parentColumns = ["id"],
            childColumns = ["comensal_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("comensal_id")]
)
data class Pedido(
    @PrimaryKey
    val id: String,             // UUID de la orden
    val comensal_id: String,    // FK hacia comensal(id)
    val producto_id: String,    // ID del plato referenciado en menu_item
    val precio_fijado: Double   // Precio al momento de cargar el pedido
)


@Entity(tableName = "menu_item")
data class MenuItem(
    @PrimaryKey
    val id: String,
    val nombre: String,
    val categoria: String = "General",
    val ingredientes: String,      // Array serializado como JSON: "[\"bife\",\"papa\"]"
    val health_labels: String?,    // Nullable — se llena con la respuesta de Edamam
    val image_url: String,
    val precio: Double = 0.0,
    val calorias: Double? = null,
    val proteinas: Double? = null,
    val carbohidratos: Double? = null,
    val grasas: Double? = null
)
