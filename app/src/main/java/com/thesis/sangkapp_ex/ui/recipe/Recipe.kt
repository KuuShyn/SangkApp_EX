package com.thesis.sangkapp_ex.ui.recipe

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
@Parcelize
data class Recipe(
    val name: String = "",
    val calories: Double = 0.0,
    val servings: Int = 1,
    val ingredients: List<Ingredient> = listOf(),
    val nutrients: Nutrients = Nutrients(),
    val imageResId: Int? = null
) : Parcelable
