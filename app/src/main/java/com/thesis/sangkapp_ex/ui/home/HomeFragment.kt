package com.thesis.sangkapp_ex.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.thesis.sangkapp_ex.R
import com.thesis.sangkapp_ex.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var foodItemsContainer: LinearLayout
    private lateinit var addFoodButton: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        foodItemsContainer = view.findViewById(R.id.foodItemsContainer)
        addFoodButton = view.findViewById(R.id.addFoodButton)

        // Add food item dynamically on button click
        addFoodButton.setOnClickListener {
            addFoodItem("Pork Adobo", "100g", "308")
        }

        return view
    }

    // Function to add food items programmatically
    private fun addFoodItem(foodName: String, foodQuantity: String, calories: String) {
        // Create a new view to represent the food item
        val foodItemView = LayoutInflater.from(context).inflate(R.layout.food_item, foodItemsContainer, false)

        // Find the views inside the food item layout
        val foodNameTextView = foodItemView.findViewById<TextView>(R.id.foodName)
        val foodQuantityTextView = foodItemView.findViewById<TextView>(R.id.foodQuantity)
        val foodCaloriesTextView = foodItemView.findViewById<TextView>(R.id.foodCalories)

        // Set the values to the respective views
        foodNameTextView.text = foodName
        foodQuantityTextView.text = foodQuantity
        foodCaloriesTextView.text = calories

        // Add the view to the container
        foodItemsContainer.addView(foodItemView)
    }
}

