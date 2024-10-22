package com.thesis.sangkapp_ex.ui.photo

data class DepthPredictionResults(
    val detectedFoodCount: Int,
    val totalCalories: Float,
    val totalCarbs: Float,
    val totalProteins: Float,
    val totalFats: Float
)
