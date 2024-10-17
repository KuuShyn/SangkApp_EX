package com.thesis.sangkapp_ex.ui.recipe

import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

data class IngredientQuantityFields(
    val ingredientInputLayout: TextInputLayout,
    val ingredientAutoComplete: MaterialAutoCompleteTextView?,
    val quantityInputLayout: TextInputLayout,
    val quantityEditText: TextInputEditText?
)
