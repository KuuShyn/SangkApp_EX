package com.thesis.sangkapp_ex.ui.recipe

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.thesis.sangkapp_ex.FoodNutrients
import com.thesis.sangkapp_ex.JsonUtils
import com.thesis.sangkapp_ex.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecipeAddNewFragment : Fragment() {

    interface OnRecipeAddedListener {
        fun onRecipeAdded(recipe: Recipe)
    }

    private var listener: OnRecipeAddedListener? = null

    private lateinit var dynamicFieldsContainer: LinearLayout
    private lateinit var addIngredientButton: MaterialButton
    private lateinit var addRecipeButton: MaterialButton

    private val ingredientQuantityFieldsList = mutableListOf<IngredientQuantityFields>()
    private var foodNames: List<String> = emptyList()

    private val firestore = FirebaseFirestore.getInstance()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? OnRecipeAddedListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recipe_add_new, container, false)

        dynamicFieldsContainer = view.findViewById(R.id.dynamicFieldsContainer)
        addIngredientButton = view.findViewById(R.id.addIngredientButton)
        addRecipeButton = view.findViewById(R.id.addRecipeButton)

        addIngredientButton.setOnClickListener { addNewIngredientFields() }

        addRecipeButton.setOnClickListener {
            saveRecipeToFirestore()
        }

        loadFoodNames()
        return view
    }

    private fun loadFoodNames() {
        CoroutineScope(Dispatchers.IO).launch {
            val nutrientsList = JsonUtils.loadPhilfctDb(requireContext())
            foodNames = nutrientsList.map { it.foodNameAndDescription }.distinct()

            withContext(Dispatchers.Main) {
                if (isAdded) addNewIngredientFields()
            }
        }
    }

    private fun addNewIngredientFields() {
        if (!isAdded) return

        val inflater = LayoutInflater.from(requireContext())
        val newFieldLayout = inflater.inflate(
            R.layout.ingredient_quantity_field,
            dynamicFieldsContainer,
            false
        ) as LinearLayout

        val ingredientInputLayout = newFieldLayout.findViewById<TextInputLayout>(R.id.ingredientInputLayout)
        val ingredientAutoComplete = ingredientInputLayout.editText as? MaterialAutoCompleteTextView

        val quantityInputLayout = newFieldLayout.findViewById<TextInputLayout>(R.id.quantityInputLayout)
        val quantityEditText = quantityInputLayout.editText as? TextInputEditText

        foodNames.takeIf { it.isNotEmpty() }?.let { names ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
            ingredientAutoComplete?.setAdapter(adapter)
            ingredientAutoComplete?.threshold = 1
        }

        dynamicFieldsContainer.addView(newFieldLayout)

        val fields = IngredientQuantityFields(
            ingredientInputLayout = ingredientInputLayout,
            ingredientAutoComplete = ingredientAutoComplete,
            quantityInputLayout = quantityInputLayout,
            quantityEditText = quantityEditText
        )
        ingredientQuantityFieldsList.add(fields)

        val deleteButton = newFieldLayout.findViewById<MaterialButton>(R.id.deleteButton)
        deleteButton?.setOnClickListener {
            dynamicFieldsContainer.removeView(newFieldLayout)
            ingredientQuantityFieldsList.remove(fields)
        }
    }

    private fun saveRecipeToFirestore() {
        val dishNameInputLayout = view?.findViewById<TextInputLayout>(R.id.dishNameInputLayout)
        val dishNameEditText = dishNameInputLayout?.editText as? TextInputEditText
        val dishName = dishNameEditText?.text.toString().trim()

        if (dishName.isEmpty()) {
            dishNameEditText?.error = "Dish name is required"
            return
        }

        val servingsInputLayout = view?.findViewById<TextInputLayout>(R.id.servingsInputLayout)
        val servingsEditText = servingsInputLayout?.editText as? TextInputEditText
        val servingsText = servingsEditText?.text.toString().trim()
        val servings = servingsText.toIntOrNull()

        if (servings == null || servings <= 0) {
            servingsEditText?.error = "Enter a valid number of servings"
            return
        }

        val ingredients = mutableListOf<Ingredient>()
        for ((index, fields) in ingredientQuantityFieldsList.withIndex()) {
            val ingredientName = fields.ingredientAutoComplete?.text?.toString()?.trim().orEmpty()
            val quantity = fields.quantityEditText?.text?.toString()?.trim().orEmpty()

            if (ingredientName.isEmpty() || quantity.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all ingredient fields", Toast.LENGTH_SHORT).show()
                return
            }
            ingredients.add(Ingredient(name = ingredientName, quantity = quantity))
        }

        CoroutineScope(Dispatchers.IO).launch {
            val foodDatabase = JsonUtils.loadPhilfctDb(requireContext())
            val (nutrients, totalCalories) = calculateTotalNutrients(ingredients, foodDatabase)

            val recipe = Recipe(
                name = dishName,
                servings = servings,
                calories = totalCalories.toDouble(),
                ingredients = ingredients,
                nutrients = nutrients
            )

            withContext(Dispatchers.Main) {
                val userId = context?.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    ?.getString("USER_ID", null)

                if (userId != null) {
                    firestore.collection("users").document(userId)
                        .collection("recipes")
                        .add(recipe)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Recipe added successfully! ✅", Toast.LENGTH_SHORT).show()
                            listener?.onRecipeAdded(recipe)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Failed to add recipe: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "Error: User ID not found", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun calculateTotalNutrients(
        ingredients: List<Ingredient>,
        foodDatabase: List<FoodNutrients>
    ): Pair<Nutrients, Float> {
        var totalCalories = 0f
        var totalCarbs = 0f
        var totalProteins = 0f
        var totalFats = 0f
        var totalFibers = 0f
        var totalSugars = 0f
        var totalCholesterol = 0f
        var totalSodium = 0f
        var totalCalcium = 0f
        var totalIron = 0f
        var totalVitA = 0f
        var totalRetinol = 0f
        var totalB1 = 0f
        var totalB2 = 0f
        var totalB3 = 0f
        var totalVitC = 0f

        for (ingredient in ingredients) {
            val name = ingredient.name
            val quantity = ingredient.quantity.toFloatOrNull() ?: continue
            val foodItem = foodDatabase.find { it.foodNameAndDescription.equals(name, ignoreCase = true) }
                ?: continue

            val factor = quantity / 100f

            totalCalories += (foodItem.energyCalculated ?: 0f) * factor
            totalCarbs += (foodItem.carbohydrateTotal ?: 0f) * factor
            totalProteins += (foodItem.protein ?: 0f) * factor
            totalFats += (foodItem.totalFat ?: 0f) * factor
            totalFibers += (foodItem.fiberTotalDietary ?: 0f) * factor
            totalSugars += (foodItem.sugarsTotal ?: 0f) * factor
            totalCholesterol += (foodItem.cholesterol ?: 0f) * factor
            totalSodium += (foodItem.sodiumNa ?: 0f) * factor
            totalCalcium += (foodItem.calciumCa ?: 0f) * factor
            totalIron += (foodItem.ironFe ?: 0f) * factor
            totalVitA += (foodItem.betaCarotene ?: 0f) * factor
            totalRetinol += (foodItem.retinolVitaminA ?: 0f) * factor
            totalB1 += (foodItem.thiaminVitaminB1 ?: 0f) * factor
            totalB2 += (foodItem.riboflavinVitaminB2 ?: 0f) * factor
            totalB3 += (foodItem.niacin ?: 0f) * factor
            totalVitC += (foodItem.ascorbicAcidVitaminC ?: 0f) * factor
        }

        val nutrients = Nutrients(
            carbohydrates = "%.2f g".format(totalCarbs),
            proteins = "%.2f g".format(totalProteins),
            fats = "%.2f g".format(totalFats),
            fibers = "%.2f g".format(totalFibers),
            sugars = "%.2f g".format(totalSugars),
            cholesterol = "%.2f mg".format(totalCholesterol),
            sodium = "%.2f mg".format(totalSodium),
            calcium = "%.2f mg".format(totalCalcium),
            iron = "%.2f mg".format(totalIron),
            vitaminABetaK = "%.2f µg".format(totalVitA),
            vitaminARetinol = "%.2f µg".format(totalRetinol),
            vitaminB1 = "%.2f mg".format(totalB1),
            vitaminB2 = "%.2f mg".format(totalB2),
            vitaminB3 = "%.2f mg".format(totalB3),
            vitaminC5 = "%.2f mg".format(totalVitC)
        )

        return Pair(nutrients, totalCalories)
    }
}
