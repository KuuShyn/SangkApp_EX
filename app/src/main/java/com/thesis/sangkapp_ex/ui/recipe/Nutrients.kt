// File: Nutrients.kt
package com.thesis.sangkapp_ex.ui.recipe

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName

@Parcelize
data class Nutrients(
    @SerializedName("Carbohydrates") val carbohydrates: String = "",
    @SerializedName("Proteins") val proteins: String = "",
    @SerializedName("Fats") val fats: String = "",
    @SerializedName("Fibers") val fibers: String = "",
    @SerializedName("Sugars") val sugars: String = "",
    @SerializedName("Cholesterol") val cholesterol: String = "",
    @SerializedName("Sodium") val sodium: String = "",
    @SerializedName("Calcium") val calcium: String = "",
    @SerializedName("Iron") val iron: String = "",
    @SerializedName("Vitamin A - Beta-K") val vitaminABetaK: String = "",
    @SerializedName("Vitamin A - Retinol") val vitaminARetinol: String = "",
    @SerializedName("Vitamin B1 - Thiamin") val vitaminB1: String = "",
    @SerializedName("Vitamin B2 - Riboflavin") val vitaminB2: String = "",
    @SerializedName("Vitamin B3 - Niacin") val vitaminB3: String = "",
    @SerializedName("Vitamin C - Ascorbic acid") val vitaminC5: String = ""
) : Parcelable
