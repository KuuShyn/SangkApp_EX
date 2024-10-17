// File: FoodNutrients.kt
package com.thesis.sangkapp_ex

import com.google.gson.annotations.SerializedName

data class FoodNutrients(
    @SerializedName("Food ID") val foodId: String = "",
    @SerializedName("Food Name and Description") val foodNameAndDescription: String = "",
    @SerializedName("Scientific Name") val scientificName: String? = "",
    @SerializedName("Alternate/Common Name") val alternateCommonName: String? = "",
    @SerializedName("Edible Portion") val ediblePortion: String? = "",
    @SerializedName("Water") val water: Float? = 0f,
    @SerializedName("Energy, calculated") val energyCalculated: Float? = 0f,
    @SerializedName("Protein") val protein: Float? = 0f,
    @SerializedName("Total Fat") val totalFat: Float? = 0f,
    @SerializedName("Carbohydrate, total") val carbohydrateTotal: Float? = 0f,
    @SerializedName("Ash, total") val ashTotal: Float? = 0f,
    @SerializedName("Fiber, total dietary") val fiberTotalDietary: Float? = 0f,
    @SerializedName("Sugars, total") val sugarsTotal: Float? = 0f,
    @SerializedName("Calcium, Ca") val calciumCa: Float? = 0f,
    @SerializedName("Phosphorus, P") val phosphorusP: Float? = 0f,
    @SerializedName("Iron, Fe") val ironFe: Float? = 0f,
    @SerializedName("Sodium, Na") val sodiumNa: Float? = 0f,
    @SerializedName("Retinol, Vitamin A") val retinolVitaminA: Float? = 0f,
    @SerializedName("beta-Carotene") val betaCarotene: Float? = 0f,
    @SerializedName("Retinol Activity Equivalent, RAE") val retinolActivityEquivalentRAE: Float? = 0f,
    @SerializedName("Thiamin, Vitamin B1") val thiaminVitaminB1: Float? = 0f,
    @SerializedName("Riboflavin, Vitamin B2") val riboflavinVitaminB2: Float? = 0f,
    @SerializedName("Niacin") val niacin: Float? = 0f,
    @SerializedName("Ascorbic Acid, Vitamin C") val ascorbicAcidVitaminC: Float? = 0f,
    @SerializedName("Fatty acids, saturated, total") val fattyAcidsSaturatedTotal: Float? = 0f,
    @SerializedName("Fatty acids, monounsaturated, total") val fattyAcidsMonounsaturatedTotal: Float? = 0f,
    @SerializedName("Cholesterol") val cholesterol: Float? = 0f
)
