package com.thesis.sangkapp_ex.ui.foodLog

import com.google.gson.annotations.SerializedName


data class FoodItem(
    @SerializedName("Food ID")
    val foodId: String,

    @SerializedName("Food Name and Description")
    val name: String,

    @SerializedName("Energy, calculated")
    val calories: Double
)

