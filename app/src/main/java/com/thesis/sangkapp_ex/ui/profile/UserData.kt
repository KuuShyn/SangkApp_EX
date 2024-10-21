package com.thesis.sangkapp_ex.ui.profile

data class UserProfile(
    val name: String,
    val age: Int,
    val gender: String,
    val heightFt: Int,
    val heightIn: Int,
    val weight: Float,
    val bmi: Double,
    val bmiCategory: String,
    val idealBodyWeight: Int,
    val weightChangeMode: String,
    val calories: Double,
    val carbs: Int,
    val protein: Int,
    val fat: Int,

)
