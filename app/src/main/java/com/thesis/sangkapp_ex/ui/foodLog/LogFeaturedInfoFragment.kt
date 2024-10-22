package com.thesis.sangkapp_ex.ui.foodLog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.thesis.sangkapp_ex.R
import com.thesis.sangkapp_ex.databinding.FragmentLogFeaturedInfoBinding
import com.thesis.sangkapp_ex.ui.recipe.Recipe
import com.thesis.sangkapp_ex.ui.recipe.RecipeInfoFragmentArgs

class LogFeaturedInfoFragment : Fragment() {

    private lateinit var recipe: Recipe

    // ViewBinding instance
    private var _binding: FragmentLogFeaturedInfoBinding? = null
    private val binding get() = _binding!!

    // Flag to track visibility state
    private var isNutritionFactsVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            recipe = RecipeInfoFragmentArgs.fromBundle(it).selectedRecipe
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using ViewBinding
        _binding = FragmentLogFeaturedInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize button text and icon based on initial visibility
        updateButtonAndIcon()
        setupDropdownMenu()

        // Set OnClickListener for the Show/Hide button
        binding.showHidebutton.setOnClickListener {
            toggleNutritionFactsVisibility()
        }

        view.findViewById<TextView>(R.id.dish_name).text = recipe.name

        "${recipe.calories} kcal".also {
            view.findViewById<TextView>(R.id.caloriesValue).text = it
        }
        view.findViewById<TextView>(R.id.carbsValue).text = recipe.nutrients.carbohydrates
        view.findViewById<TextView>(R.id.proteinsValue).text = recipe.nutrients.proteins
        view.findViewById<TextView>(R.id.fatsValue).text = recipe.nutrients.fats
        view.findViewById<TextView>(R.id.fibersValue).text = recipe.nutrients.fibers
        view.findViewById<TextView>(R.id.sugarsValue).text = recipe.nutrients.sugars
        view.findViewById<TextView>(R.id.cholesterolValue).text = recipe.nutrients.cholesterol
        view.findViewById<TextView>(R.id.sodiumValue).text = recipe.nutrients.sodium
        view.findViewById<TextView>(R.id.calciumValue).text = recipe.nutrients.calcium
        view.findViewById<TextView>(R.id.ironValue).text = recipe.nutrients.iron
        view.findViewById<TextView>(R.id.vitaminABetaKValue).text = recipe.nutrients.vitaminABetaK
        view.findViewById<TextView>(R.id.vitaminARetinolValue).text =
            recipe.nutrients.vitaminARetinol
        view.findViewById<TextView>(R.id.vitaminB1Value).text = recipe.nutrients.vitaminB1
        view.findViewById<TextView>(R.id.vitaminB2Value).text = recipe.nutrients.vitaminB2
        view.findViewById<TextView>(R.id.vitaminB3Value).text = recipe.nutrients.vitaminB3
        view.findViewById<TextView>(R.id.vitaminC5Value).text = recipe.nutrients.vitaminC5
    }

    private fun toggleNutritionFactsVisibility() {
        if (isNutritionFactsVisible) {
            // Hide the Nutrition Facts TextView
            binding.nutritionFactsCard.visibility = View.GONE
        } else {
            // Show the Nutrition Facts TextView
            binding.nutritionFactsCard.visibility = View.VISIBLE
        }

        // Toggle the flag
        isNutritionFactsVisible = !isNutritionFactsVisible

        // Update button text and icon
        updateButtonAndIcon()
    }

    private fun updateButtonAndIcon() {
        if (isNutritionFactsVisible) {
            "Hide".also { binding.showHidebutton.text = it }
            binding.showHidebutton.setIconResource(R.drawable.baseline_keyboard_arrow_up_24)
        } else {
            "Show".also { binding.showHidebutton.text = it }
            binding.showHidebutton.setIconResource(R.drawable.baseline_keyboard_arrow_down_24)
        }
    }

    private fun setupDropdownMenu() {
        // Define the options for the dropdown
        val mealOptions = listOf("Breakfast", "Lunch", "Dinner", "Snack")

        // Create an ArrayAdapter using the string array and a default dropdown layout
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_menu_item, // A custom layout for dropdown items
            mealOptions
        )

        // Set the adapter to the MaterialAutoCompleteTextView
        binding.dropdownMenu.setAdapter(adapter)

        // Optionally, set a default selection
        binding.dropdownMenu.setText("Breakfast", false)

        // Handle item selection
        binding.dropdownMenu.setOnItemClickListener { parent, view, position, id ->
            val selectedMeal = parent.getItemAtPosition(position) as String
//            updateRecommendation(selectedMeal)

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Avoid memory leaks
        _binding = null
    }
}