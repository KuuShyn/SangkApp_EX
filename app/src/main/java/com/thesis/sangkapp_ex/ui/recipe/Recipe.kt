package com.thesis.sangkapp_ex.ui.recipe

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.firebase.firestore.IgnoreExtraProperties


@Parcelize
data class Recipe(
    val name: String = "",
    val calories: Int = 1,
    val quantity: Int = 1,
    val ingredients: List<Ingredient> = listOf(),
    val nutrients: Nutrients = Nutrients(),
    val imageResId: Int? = null
) : Parcelable
