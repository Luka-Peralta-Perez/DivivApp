package com.example.divivapp.network

import com.google.gson.annotations.SerializedName


data class EdamamResponse(
    val hints: List<EdamamHint> = emptyList(),
    val parsed: List<EdamamParsed> = emptyList()
)

data class EdamamParsed(
    val food: EdamamFood? = null
)

data class EdamamHint(
    val food: EdamamFood? = null
)

data class EdamamFood(
    val foodId: String? = null,
    val label: String? = null,
    val image: String? = null,
    val nutrients: EdamamNutrients? = null,
    val healthLabels: List<String>? = null,
    val cautions: List<String>? = null
)

data class EdamamNutrients(
    @SerializedName("ENERC_KCAL") val calorias: Double? = null,     // Calorias por 100g
    @SerializedName("PROCNT")     val proteinas: Double? = null,    // Proteinas (g) por 100g
    @SerializedName("CHOCDF")     val carbohidratos: Double? = null, // Carbohidratos (g)
    @SerializedName("FAT")        val grasas: Double? = null         // Grasas (g)
)
