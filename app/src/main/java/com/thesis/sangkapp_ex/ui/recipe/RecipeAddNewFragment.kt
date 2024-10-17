// File: RecipeAddNewFragment.kt
package com.thesis.sangkapp_ex.ui.recipe

import android.content.Context
import android.os.Bundle
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

    // List to hold food names
    private var foodNames: List<String> = emptyList()

    // Firestore instance
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

        // Load food names asynchronously
        loadFoodNames()

        return view
    }

    private fun loadFoodNames() {
        // Use Coroutines to load data off the main thread
        CoroutineScope(Dispatchers.IO).launch {
            val nutrientsList = JsonUtils.loadPhilfctDb(requireContext())
            foodNames = nutrientsList.map { it.foodNameAndDescription }.distinct()

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
        // Retrieve Dish Name
        val dishNameInputLayout = view?.findViewById<TextInputLayout>(R.id.dishNameInputLayout)
        val dishNameEditText = dishNameInputLayout?.editText as? TextInputEditText
        val dishName = dishNameEditText?.text.toString().trim()

        if (dishName.isEmpty()) {
            dishNameEditText?.error = "Dish name is required"
            return
        }

        // Retrieve Number of Servings
        val servingsInputLayout = view?.findViewById<TextInputLayout>(R.id.servingsInputLayout)
        val servingsEditText = servingsInputLayout?.editText as? TextInputEditText
        val servingsText = servingsEditText?.text.toString().trim()
        val servings = servingsText.toIntOrNull()

        if (servings == null || servings <= 0) {
            servingsEditText?.error = "Enter a valid number of servings"
            return
        }

        // Retrieve Ingredients
        val ingredients = mutableListOf<Ingredient>()
        for (fields in ingredientQuantityFieldsList) {
            val ingredientName = fields.ingredientAutoComplete?.text.toString().trim()
            val quantity = fields.quantityEditText?.text.toString().trim()

            if (ingredientName.isEmpty() || quantity.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Please fill all ingredient fields",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            ingredients.add(Ingredient(name = ingredientName, quantity = quantity))
        }

        // Retrieve Nutrients (Assuming you calculate nutrients based on ingredients)
        // For simplicity, let's assume dummy data or implement nutrient calculation logic
        // Here, we'll mock the nutrients data

        // TODO: Implement nutrient calculation based on ingredients
        val nutrients = Nutrients(
            carbohydrates = "20g",
            proteins = "30g",
            fats = "15g",
            fibers = "5g",
            sugars = "10g",
            cholesterol = "0g",
            sodium = "1g",
            calcium = "22mg",
            iron = "1.3mg",
            vitaminABetaK = "65µg",
            vitaminARetinol = "5µg",
            vitaminB1 = "0.14mg",
            vitaminB2 = "0.03mg",
            vitaminB3 = "1.3mg",
            vitaminC5 = "0mg"
        )

        // Create Recipe object
        val recipe = Recipe(
            name = dishName,
            servings = servings,
            ingredients = ingredients,
            nutrients = nutrients
        )

        // Save to Firestore
        firestore.collection("recipes")
            .add(recipe)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Recipe added successfully!", Toast.LENGTH_SHORT)
                    .show()
                listener?.onRecipeAdded(recipe)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Failed to add recipe: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun clearFields() {
        // Clear Dish Name and Servings
        val dishNameEditText =
            view?.findViewById<TextInputLayout>(R.id.dishNameInputLayout)?.editText as? TextInputEditText
        dishNameEditText?.text?.clear()

        val servingsEditText =
            view?.findViewById<TextInputLayout>(R.id.servingsInputLayout)?.editText as? TextInputEditText
        servingsEditText?.text?.clear()

        // Remove all ingredient fields and add a new one
        dynamicFieldsContainer.removeAllViews()
        ingredientQuantityFieldsList.clear()
        addNewIngredientFields()
    }
}