// File: RecipeInfoFragment.kt
package com.thesis.sangkapp_ex.ui.recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thesis.sangkapp_ex.R

class RecipeInfoFragment : Fragment() {

    private lateinit var recipe: Recipe
    private lateinit var ingredientsRecyclerView: RecyclerView
    private lateinit var ingredientsAdapter: IngredientsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            recipe = RecipeInfoFragmentArgs.fromBundle(it).selectedRecipe
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recipe_info, container, false)

        // Initialize views
        val recipeTitleTextView = view.findViewById<TextView>(R.id.recipeTitle)
        ingredientsRecyclerView = view.findViewById(R.id.ingredientsRecyclerView)

        // Set recipe title
        recipeTitleTextView.text = recipe.name

        // Set up ingredients RecyclerView
        ingredientsRecyclerView.layoutManager = GridLayoutManager(context, 2)
        ingredientsAdapter = IngredientsAdapter(recipe.ingredients)
        ingredientsRecyclerView.adapter = ingredientsAdapter

        // Set nutrients
        "${recipe.calories} kcal".also { view.findViewById<TextView>(R.id.caloriesValue).text = it }
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
        view.findViewById<TextView>(R.id.vitaminARetinolValue).text = recipe.nutrients.vitaminARetinol
        view.findViewById<TextView>(R.id.vitaminB1Value).text = recipe.nutrients.vitaminB1
        view.findViewById<TextView>(R.id.vitaminB2Value).text = recipe.nutrients.vitaminB2
        view.findViewById<TextView>(R.id.vitaminB3Value).text = recipe.nutrients.vitaminB3
        view.findViewById<TextView>(R.id.vitaminC5Value).text = recipe.nutrients.vitaminC5

        return view
    }
}
