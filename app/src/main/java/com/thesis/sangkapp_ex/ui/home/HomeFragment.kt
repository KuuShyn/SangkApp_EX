package com.thesis.sangkapp_ex.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.juanarton.arcprogressbar.ArcProgressBar
import com.thesis.sangkapp_ex.R

class HomeFragment : Fragment() {

    // Food item containers
    private lateinit var breakfastFoodItemsContainer: LinearLayout
    private lateinit var lunchFoodItemsContainer: LinearLayout
    private lateinit var dinnerFoodItemsContainer: LinearLayout
    private lateinit var snacksFoodItemsContainer: LinearLayout

    // Add Food buttons
    private lateinit var addBreakfastButton: MaterialButton
    private lateinit var addLunchButton: MaterialButton
    private lateinit var addDinnerButton: MaterialButton
    private lateinit var addSnacksButton: MaterialButton

    // TextViews for calorie counts
    private lateinit var breakfastCaloriesTextView: TextView
    private lateinit var lunchCaloriesTextView: TextView
    private lateinit var dinnerCaloriesTextView: TextView
    private lateinit var snackCaloriesTextView: TextView
    private lateinit var calorieCountTextView: TextView
    private lateinit var consumedValueTextView: TextView
    private lateinit var remainingValueTextView: TextView

    // Progress bar
    private lateinit var arcProgressBar: ArcProgressBar

    private val db = Firebase.firestore

    // Variables to track calories
    private var totalCalories: Long = 0L
    private var consumedCalories: Long = 0L
    private var remainingCalories: Long = 0L

    private var breakfastConsumedCalories: Long = 0L
    private var lunchConsumedCalories: Long = 0L
    private var dinnerConsumedCalories: Long = 0L
    private var snacksConsumedCalories: Long = 0L

    private var breakfastTotalCalories: Long = 0L
    private var lunchTotalCalories: Long = 0L
    private var dinnerTotalCalories: Long = 0L
    private var snacksTotalCalories: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize food item containers
        breakfastFoodItemsContainer = view.findViewById(R.id.foodItemsContainer)
        lunchFoodItemsContainer = view.findViewById(R.id.lunchFoodItemsContainer)
        dinnerFoodItemsContainer = view.findViewById(R.id.dinnerFoodItemsContainer)
        snacksFoodItemsContainer = view.findViewById(R.id.snacksFoodItemsContainer)

        // Initialize buttons
        addBreakfastButton = view.findViewById(R.id.addBreakfastFoodButton)
        addLunchButton = view.findViewById(R.id.addLunchButton)
        addDinnerButton = view.findViewById(R.id.addDinnerButton)
        addSnacksButton = view.findViewById(R.id.addSnacksButton)

        // Initialize TextViews
        breakfastCaloriesTextView = view.findViewById(R.id.breakfastCalories)
        lunchCaloriesTextView = view.findViewById(R.id.lunchCalories)
        dinnerCaloriesTextView = view.findViewById(R.id.dinnerCalories)
        snackCaloriesTextView = view.findViewById(R.id.snacksCalories)
        calorieCountTextView = view.findViewById(R.id.calorieCount)
        consumedValueTextView = view.findViewById(R.id.consumedValue)
        remainingValueTextView = view.findViewById(R.id.remainingValue)

        // Initialize ArcProgressBar
        arcProgressBar = view.findViewById(R.id.arc_img)

        // Fetch and display allocated calories
        fetchAndDisplayCalorieAllocations()

        // Set up click listeners for the "Add Food" buttons
        addBreakfastButton.setOnClickListener {
            addFoodItemToMeal("Breakfast", "Pancakes", "2 pieces", "350")
        }

        addLunchButton.setOnClickListener {
            addFoodItemToMeal("Lunch", "Grilled Chicken", "150g", "400")
        }

        addDinnerButton.setOnClickListener {
            addFoodItemToMeal("Dinner", "Steak", "200g", "500")
        }

        addSnacksButton.setOnClickListener {
            addFoodItemToMeal("Snacks", "Apple", "1 piece", "80")
        }

        return view
    }

    private fun fetchAndDisplayCalorieAllocations() {
        // Get the user's UID from SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("USER_ID", null)

        if (userId != null) {
            db.collection("users").document(userId).collection("profile").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        breakfastTotalCalories = document.getLong("breakfastCalories") ?: 0L
                        lunchTotalCalories = document.getLong("lunchCalories") ?: 0L
                        dinnerTotalCalories = document.getLong("dinnerCalories") ?: 0L
                        snacksTotalCalories = document.getLong("snackCalories") ?: 0L
                        totalCalories = document.getLong("calories") ?: 0L

                        // Update the TextViews
                        breakfastCaloriesTextView.text = "0/$breakfastTotalCalories"
                        lunchCaloriesTextView.text = "0/$lunchTotalCalories"
                        dinnerCaloriesTextView.text = "0/$dinnerTotalCalories"
                        snackCaloriesTextView.text = "0/$snacksTotalCalories"

                        // Update total calorie count
                        calorieCountTextView.text = totalCalories.toString()

                        // Initialize consumed calories


                        // Calculate remaining calories
                        remainingCalories = totalCalories - consumedCalories
                        remainingValueTextView.text = remainingCalories.toString()

                        // Update ArcProgressBar
                        arcProgressBar.progress = 0f
                    }
                }
                .addOnFailureListener { e ->
                    // Handle error
                }
        } else {
            // Handle error
        }
    }

    private fun addFoodItemToMeal(mealType: String, foodName: String, foodQuantity: String, calories: String) {
        // Create a new view to represent the food item
        val foodItemView = LayoutInflater.from(context).inflate(R.layout.food_item, null, false)

        // Find the views inside the food item layout
        val foodNameTextView = foodItemView.findViewById<TextView>(R.id.foodName)
        val foodQuantityTextView = foodItemView.findViewById<TextView>(R.id.foodQuantity)
        val foodCaloriesTextView = foodItemView.findViewById<TextView>(R.id.foodCalories)

        // Set the values to the respective views
        foodNameTextView.text = foodName
        foodQuantityTextView.text = foodQuantity
        foodCaloriesTextView.text = calories

        // Add the view to the appropriate container and update consumed calories
        when (mealType) {
            "Breakfast" -> {
                breakfastFoodItemsContainer.addView(foodItemView)
                updateConsumedCalories(calories.toInt(), mealType)
            }
            "Lunch" -> {
                lunchFoodItemsContainer.addView(foodItemView)
                updateConsumedCalories(calories.toInt(), mealType)
            }
            "Dinner" -> {
                dinnerFoodItemsContainer.addView(foodItemView)
                updateConsumedCalories(calories.toInt(), mealType)
            }
            "Snacks" -> {
                snacksFoodItemsContainer.addView(foodItemView)
                updateConsumedCalories(calories.toInt(), mealType)
            }
        }
    }

    private fun updateConsumedCalories(addedCalories: Int, mealType: String) {
        consumedCalories += addedCalories

        when (mealType) {
            "Breakfast" -> {
                breakfastConsumedCalories += addedCalories
                breakfastCaloriesTextView.text = "$breakfastConsumedCalories/$breakfastTotalCalories"
            }
            "Lunch" -> {
                lunchConsumedCalories += addedCalories
                lunchCaloriesTextView.text = "$lunchConsumedCalories/$lunchTotalCalories"
            }
            "Dinner" -> {
                dinnerConsumedCalories += addedCalories
                dinnerCaloriesTextView.text = "$dinnerConsumedCalories/$dinnerTotalCalories"
            }
            "Snacks" -> {
                snacksConsumedCalories += addedCalories
                snackCaloriesTextView.text = "$snacksConsumedCalories/$snacksTotalCalories"
            }
        }

        consumedValueTextView.text = consumedCalories.toString()

        // Update remaining calories
        remainingCalories = totalCalories - consumedCalories
        remainingValueTextView.text = remainingCalories.toString()

        // Calculate progress percentage
        val progress = if (totalCalories > 0) {
            (consumedCalories.toFloat() / totalCalories.toFloat()) * 100
        } else {
            0f
        }

        // Ensure progress is within 0 to 100
        val clampedProgress = progress.coerceIn(0f, 100f)

        // Update the progress bar on the main thread
        activity?.runOnUiThread {
            arcProgressBar.progress = clampedProgress
        }
    }
}
