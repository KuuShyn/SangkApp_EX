// File: FoodDensity.kt
package com.thesis.sangkapp_ex

import com.google.gson.annotations.SerializedName

data class FoodDensity(
    @SerializedName("Food ID") val foodId: String,
    @SerializedName("Food Name and Description") val foodNameAndDescription: String,
    @SerializedName("Energy, calculated") val energyCalculated: Float?,
    @SerializedName("Density, g/ml") val densityGml: Float?
)
