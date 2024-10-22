package com.thesis.sangkapp_ex.ui.foodLog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thesis.sangkapp_ex.R
import com.thesis.sangkapp_ex.ui.recipe.Recipe

class LogFeaturedFragment : Fragment() {

    private lateinit var foodItemsRecyclerView: RecyclerView
    private lateinit var logFoodAdapter: LogFoodAdapter

    // Dummy food list (replace with actual data)
    private val foodList = listOf(
        Recipe(name = "Arrozcaldo", calories = 200, imageResId = R.drawable.arrozcaldo),
        Recipe(name = "Balbacua", calories = 300, imageResId = R.drawable.balbacua),
        Recipe(name = "Bulalo", calories = 400, imageResId = R.drawable.bulalo),
        Recipe(name = "Champorado", calories = 350, imageResId = R.drawable.champorado),
        Recipe(name = "Chicken Adobo", calories = 500, imageResId = R.drawable.chicken_adobo),
        Recipe(name = "Chicken Inasal", calories = 450, imageResId = R.drawable.chicken_inasal),
        Recipe(name = "Chicken Tinola", calories = 320, imageResId = R.drawable.chicken_tinola),
        Recipe(name = "Daing na Bangus", calories = 350, imageResId = R.drawable.daing_na_bangus)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_log_featured, container, false)

        // Initialize RecyclerView
        foodItemsRecyclerView = view.findViewById(R.id.foodItemsRecyclerView)

        // Set up a GridLayoutManager with 2 columns
        foodItemsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        // Initialize Adapter with a click listener
        logFoodAdapter = LogFoodAdapter(foodList) { selectedRecipe ->
            // Handle item click, for example, navigate to RecipeInfoFragment
            onFoodItemClick(selectedRecipe)
        }

        // Set the adapter
        foodItemsRecyclerView.adapter = logFoodAdapter

        return view
    }

    private fun onFoodItemClick(recipe: Recipe) {
        // Handle the food item click (e.g., navigate to another fragment)
        // This could involve using Navigation Component with SafeArgs or another navigation method
    }
}
