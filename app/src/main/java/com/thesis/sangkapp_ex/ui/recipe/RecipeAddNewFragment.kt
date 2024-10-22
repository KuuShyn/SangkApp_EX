// File: RecipeAddNewFragment.kt
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
import java.util.Locale

class RecipeAddNewFragment : Fragment() {
    interface OnRecipeAddedListener {
        fun onRecipeAdded(recipe: Recipe)
    }

    private var listener: OnRecipeAddedListener? = null

    private lateinit var dynamicFieldsContainer: LinearLayout
    private lateinit var addIngredientButton: MaterialButton
    private lateinit var addRecipeButton: MaterialButton

    private val ingredientQuantityFieldsList = mutableListOf<IngredientQuantityFields>()

    // List to hold food names
    private var foodNames: List<String> = emptyList()

    // Firestore instance
    private val firestore = FirebaseFirestore.getInstance()

    // List to hold all FoodNutrients loaded from JSON
    private var allFoodNutrients: List<FoodNutrients> = emptyList()

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
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recipe_add_new, container, false)

        // Initialize views
        dynamicFieldsContainer = view.findViewById(R.id.dynamicFieldsContainer)
        addIngredientButton = view.findViewById(R.id.addIngredientButton)
        addRecipeButton = view.findViewById(R.id.addRecipeButton)

        // Set click listener for adding new ingredient fields
        addIngredientButton.setOnClickListener {
            addNewIngredientFields()
        }

        // Set click listener for adding the recipe to Firestore
        addRecipeButton.setOnClickListener {
            saveRecipeToFirestore()
        }

        // Load food names and nutrient data asynchronously
        loadFoodNamesAndNutrients()

        return view
    }

    private fun loadFoodNamesAndNutrients() {
        // Use Coroutines to load data off the main thread
        CoroutineScope(Dispatchers.IO).launch {
            allFoodNutrients = JsonUtils.loadPhilfctDb(requireContext())
            foodNames = allFoodNutrients.map { it.foodNameAndDescription }.distinct()

            withContext(Dispatchers.Main) {
                // Add the first set of fields automatically after loading
                if (isAdded) {
                    addNewIngredientFields()
                }
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

        val ingredientInputLayout =
            newFieldLayout.findViewById<TextInputLayout>(R.id.ingredientInputLayout)
        val ingredientAutoComplete = ingredientInputLayout.editText as? MaterialAutoCompleteTextView

        val quantityInputLayout =
            newFieldLayout.findViewById<TextInputLayout>(R.id.quantityInputLayout)
        val quantityEditText = quantityInputLayout.editText as? TextInputEditText

        // Set up the AutoCompleteTextView with the food names
        foodNames.takeIf { it.isNotEmpty() }?.let { names ->
            val adapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
            ingredientAutoComplete?.setAdapter(adapter)
            ingredientAutoComplete?.threshold = 1 // Start suggesting after 1 character
        }

        dynamicFieldsContainer.addView(newFieldLayout)

        val fields = IngredientQuantityFields(
            ingredientInputLayout = ingredientInputLayout,
            ingredientAutoComplete = ingredientAutoComplete,
            quantityInputLayout = quantityInputLayout,
            quantityEditText = quantityEditText
        )

        ingredientQuantityFieldsList.add(fields)

        // Handle delete button
        val deleteButton = newFieldLayout.findViewById<MaterialButton>(R.id.deleteButton)
        deleteButton?.setOnClickListener {
            dynamicFieldsContainer.removeView(newFieldLayout)
            ingredientQuantityFieldsList.remove(fields)
        }
    }

    private fun saveRecipeToFirestore() {
        // Retrieve the dish name
        val dishNameInputLayout = view?.findViewById<TextInputLayout>(R.id.dishNameInputLayout)
        val dishNameEditText = dishNameInputLayout?.editText as? TextInputEditText
        val dishName = dishNameEditText?.text.toString().trim()

        if (dishName.isEmpty()) {
            dishNameEditText?.error = "Dish name is required"
            return
        }

        // Retrieve the number of servings
        val servingsInputLayout = view?.findViewById<TextInputLayout>(R.id.servingsInputLayout)
        val servingsEditText = servingsInputLayout?.editText as? TextInputEditText
        val servingsText = servingsEditText?.text.toString().trim()
        val servings = servingsText.toIntOrNull()

        if (servings == null || servings <= 0) {
            servingsEditText?.error = "Enter a valid number of servings"
            return
        }

        // Retrieve ingredients
        val ingredients = mutableListOf<Ingredient>()
        for (fields in ingredientQuantityFieldsList) {
            val ingredientName = fields.ingredientAutoComplete?.text.toString().trim()
            val quantityText = fields.quantityEditText?.text.toString().trim()

            if (ingredientName.isEmpty() || quantityText.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Please fill all ingredient fields",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            // Validate quantity as a positive number
            val quantity = quantityText.toFloatOrNull()
            if (quantity == null || quantity <= 0f) {
                fields.quantityEditText?.error = "Enter a valid quantity in grams"
                return
            }

            // Convert quantity back to String to match Ingredient data class
            ingredients.add(Ingredient(name = ingredientName, quantity = quantityText))
        }

        // Calculate total nutrients and calories
        val (totalNutrients, totalCalories) = calculateTotalNutrients(ingredients)

        // Create the Recipe object with calories
        val recipe = Recipe(
            name = dishName,
            calories = totalCalories.toInt(), // Convert Float to Int
            quantity = servings,
            ingredients = ingredients,
            nutrients = totalNutrients
        )

        // Get user ID from SharedPreferences
        val userId = context?.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            ?.getString("USER_ID", null)

        if (userId != null) {
            firestore.collection("users").document(userId)
                .collection("recipes")
                .add(recipe)
                .addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        "Recipe added successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    listener?.onRecipeAdded(recipe)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        requireContext(),
                        "Failed to add recipe: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            Toast.makeText(requireContext(), "Error: User ID not found", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Calculates the total nutrients and calories for the recipe based on the ingredients.
     *
     * @param ingredients The list of ingredients in the recipe.
     * @return A Pair containing the total Nutrients object and total calories as Float.
     */
    private fun calculateTotalNutrients(ingredients: List<Ingredient>): Pair<Nutrients, Float> {
        // Initialize totals as Float
        var totalCarbohydrates = 0f
        var totalProteins = 0f
        var totalFats = 0f
        var totalFibers = 0f
        var totalSugars = 0f
        var totalCholesterol = 0f
        var totalSodium = 0f
        var totalCalcium = 0f
        var totalIron = 0f
        var totalVitaminABetaK = 0f
        var totalVitaminARetinol = 0f
        var totalVitaminB1 = 0f
        var totalVitaminB2 = 0f
        var totalVitaminB3 = 0f
        var totalVitaminC5 = 0f
        var totalCalories = 0f // Initialize total calories

        for (ingredient in ingredients) {
            // Find the corresponding FoodNutrients
            val foodNutrients = allFoodNutrients.find {
                it.foodNameAndDescription.equals(ingredient.name, ignoreCase = true)
            }

            if (foodNutrients == null) {
                Toast.makeText(
                    requireContext(),
                    "Nutrient data not found for ingredient: ${ingredient.name}",
                    Toast.LENGTH_SHORT
                ).show()
                continue
            }

            // Parse the quantity from String to Float
            val quantity = ingredient.quantity.toFloatOrNull()
            if (quantity == null || quantity <= 0f) {
                Toast.makeText(
                    requireContext(),
                    "Invalid quantity for ingredient: ${ingredient.name}",
                    Toast.LENGTH_SHORT
                ).show()
                continue
            }

            // Assume that nutrient values are per 100g
            val factor = quantity / 100f

            // Helper function to parse nutrient strings to Float
            fun parseNutrient(value: String?): Float {
                return value?.toFloatOrNull() ?: 0f
            }

            // Sum up the nutrients
            totalCarbohydrates += parseNutrient(foodNutrients.carbohydrateTotal?.toString()) * factor
            totalProteins += parseNutrient(foodNutrients.protein?.toString()) * factor
            totalFats += parseNutrient(foodNutrients.totalFat?.toString()) * factor
            totalFibers += parseNutrient(foodNutrients.fiberTotalDietary?.toString()) * factor
            totalSugars += parseNutrient(foodNutrients.sugarsTotal?.toString()) * factor
            totalCholesterol += parseNutrient(foodNutrients.cholesterol?.toString()) * factor
            totalSodium += parseNutrient(foodNutrients.sodiumNa?.toString()) * factor
            totalCalcium += parseNutrient(foodNutrients.calciumCa?.toString()) * factor
            totalIron += parseNutrient(foodNutrients.ironFe?.toString()) * factor
            totalVitaminABetaK += parseNutrient(foodNutrients.betaCarotene?.toString()) * factor
            totalVitaminARetinol += parseNutrient(foodNutrients.retinolVitaminA?.toString()) * factor
            totalVitaminB1 += parseNutrient(foodNutrients.thiaminVitaminB1?.toString()) * factor
            totalVitaminB2 += parseNutrient(foodNutrients.riboflavinVitaminB2?.toString()) * factor
            totalVitaminB3 += parseNutrient(foodNutrients.niacin?.toString()) * factor
            totalVitaminC5 += parseNutrient(foodNutrients.ascorbicAcidVitaminC?.toString()) * factor

            // Calculate calories
            totalCalories += parseNutrient(foodNutrients.energyCalculated?.toString()) * factor
        }

        // Convert totals back to String with appropriate formatting (e.g., two decimal places)
        val totalNutrients = Nutrients(
            carbohydrates = String.format(Locale.getDefault(), "%.2f g", totalCarbohydrates),
            proteins = String.format(Locale.getDefault(), "%.2f g", totalProteins),
            fats = String.format(Locale.getDefault(), "%.2f g", totalFats),
            fibers = String.format(Locale.getDefault(), "%.2f g", totalFibers),
            sugars = String.format(Locale.getDefault(), "%.2f g", totalSugars),
            cholesterol = String.format(Locale.getDefault(), "%.2f mg", totalCholesterol),
            sodium = String.format(Locale.getDefault(), "%.2f mg", totalSodium),
            calcium = String.format(Locale.getDefault(), "%.2f mg", totalCalcium),
            iron = String.format(Locale.getDefault(), "%.2f mg", totalIron),
            vitaminABetaK = String.format(Locale.getDefault(), "%.2f µg", totalVitaminABetaK),
            vitaminARetinol = String.format(Locale.getDefault(), "%.2f µg", totalVitaminARetinol),
            vitaminB1 = String.format(Locale.getDefault(), "%.2f mg", totalVitaminB1),
            vitaminB2 = String.format(Locale.getDefault(), "%.2f mg", totalVitaminB2),
            vitaminB3 = String.format(Locale.getDefault(), "%.2f mg", totalVitaminB3),
            vitaminC5 = String.format(Locale.getDefault(), "%.2f mg", totalVitaminC5)
        )

        return Pair(totalNutrients, totalCalories)
    }
}
