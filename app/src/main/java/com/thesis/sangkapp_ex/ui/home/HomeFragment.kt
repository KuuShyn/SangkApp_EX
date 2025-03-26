package com.thesis.sangkapp_ex.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.ktx.Firebase
import com.thesis.sangkapp_ex.GlobalVariables.Companion.remainingCalories
import com.thesis.sangkapp_ex.ui.ArcProgressBar
import com.thesis.sangkapp_ex.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    // Date variables
    private var selectedDate: Date = Date()
    private val dateFormatDisplay = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    private val dateFormatQuery = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Date navigation views
    private lateinit var previousButton: ImageButton
    private lateinit var nextButton: ImageButton
    private lateinit var currentDateText: TextView

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

    // Nutrient progress indicators
    private lateinit var circularProgressIndicatorCarbs: CircularProgressIndicator
    private lateinit var circularProgressIndicatorProteins: CircularProgressIndicator
    private lateinit var circularProgressIndicatorFats: CircularProgressIndicator

    // Center TextViews for nutrients
    private lateinit var centerTextCarbs: TextView
    private lateinit var centerTextProteins: TextView
    private lateinit var centerTextFats: TextView

    private val db = Firebase.firestore

    // Variables to track calories
    private var totalCalories: Long = 0L
    private var consumedCalories: Long = 0L

    private var breakfastConsumedCalories: Long = 0L
    private var lunchConsumedCalories: Long = 0L
    private var dinnerConsumedCalories: Long = 0L
    private var snacksConsumedCalories: Long = 0L

    private var breakfastTotalCalories: Long = 0L
    private var lunchTotalCalories: Long = 0L
    private var dinnerTotalCalories: Long = 0L
    private var snacksTotalCalories: Long = 0L

    // Variables to track consumed nutrients
    private var consumedCarbs: Float = 0f
    private var consumedProteins: Float = 0f
    private var consumedFats: Float = 0f

    // Nutrient goals
    private val carbsGoal: Float = 300f
    private val proteinsGoal: Float = 50f
    private val fatsGoal: Float = 70f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize date navigation views
        previousButton = view.findViewById(R.id.previousButton)
        nextButton = view.findViewById(R.id.nextButton)
        currentDateText = view.findViewById(R.id.currentDateText)

        // Set the current date
        selectedDate = Date()
        currentDateText.text = dateFormatDisplay.format(selectedDate)

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

        // Initialize nutrient progress indicators
        circularProgressIndicatorCarbs = view.findViewById(R.id.circularProgressIndicatorCarbs)
        circularProgressIndicatorProteins = view.findViewById(R.id.circularProgressIndicatorProteins)
        circularProgressIndicatorFats = view.findViewById(R.id.circularProgressIndicatorFats)

        // Initialize center texts
        centerTextCarbs = view.findViewById(R.id.centerTextCarbs)
        centerTextProteins = view.findViewById(R.id.centerTextProteins)
        centerTextFats = view.findViewById(R.id.centerTextFats)

        // Set max progress for nutrient indicators
        circularProgressIndicatorCarbs.max = carbsGoal.toInt()
        circularProgressIndicatorProteins.max = proteinsGoal.toInt()
        circularProgressIndicatorFats.max = fatsGoal.toInt()

        // Ensure Firestore offline persistence is enabled
        Firebase.firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        // Fetch and display allocated calories
        fetchAndDisplayCalorieAllocations()

        // Fetch and display saved food entries
        fetchAndDisplayFoodEntries()

        // Set up date navigation listeners
        previousButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.time = selectedDate
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            selectedDate = calendar.time
            currentDateText.text = dateFormatDisplay.format(selectedDate)
            fetchAndDisplayFoodEntries()
        }

        nextButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.time = selectedDate
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            selectedDate = calendar.time
            currentDateText.text = dateFormatDisplay.format(selectedDate)
            fetchAndDisplayFoodEntries()
        }

        // Set up click listeners for the "Add Food" buttons
        addBreakfastButton.setOnClickListener {
            navigateToLogFoodFragment("Breakfast")
        }

        addLunchButton.setOnClickListener {
            navigateToLogFoodFragment("Lunch")
        }

        addDinnerButton.setOnClickListener {
            navigateToLogFoodFragment("Dinner")
        }

        addSnacksButton.setOnClickListener {
            navigateToLogFoodFragment("Snack")
        }

        return view
    }

    private fun navigateToLogFoodFragment(mealType: String) {
        val action = HomeFragmentDirections.actionNavHomeToLogFoodFragment(mealType)
        findNavController().navigate(action)
    }

    private fun fetchAndDisplayCalorieAllocations() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            db.collection("users").document(userId).collection("profile").document(userId)
                .addSnapshotListener { document, e ->
                    if (e != null) {
                        Log.e("HomeFragment", "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (document != null && document.exists()) {
                        breakfastTotalCalories = document.getLong("breakfastCalories") ?: 0L
                        lunchTotalCalories = document.getLong("lunchCalories") ?: 0L
                        dinnerTotalCalories = document.getLong("dinnerCalories") ?: 0L
                        snacksTotalCalories = document.getLong("snackCalories") ?: 0L
                        totalCalories = document.getLong("calories") ?: 0L

                        // Update the TextViews
                        "${breakfastConsumedCalories}/${breakfastTotalCalories}".also { breakfastCaloriesTextView.text = it }
                        "${lunchConsumedCalories}/${lunchTotalCalories}".also { lunchCaloriesTextView.text = it }
                        "${dinnerConsumedCalories}/${dinnerTotalCalories}".also { dinnerCaloriesTextView.text = it }
                        "${snacksConsumedCalories}/${snacksTotalCalories}".also { snackCaloriesTextView.text = it }

                        // Update total calorie count
                        calorieCountTextView.text = totalCalories.toString()

                        // Calculate remaining calories
                        remainingCalories = totalCalories - consumedCalories
                        remainingValueTextView.text = remainingCalories.toString()

                        // Update ArcProgressBar
                        val progress = if (totalCalories > 0) {
                            (consumedCalories.toFloat() / totalCalories.toFloat()) * 100
                        } else {
                            0f
                        }
                        arcProgressBar.progress = progress
                    } else {
                        Log.d("HomeFragment", "No profile data found")
                    }
                }
        } else {
            Log.e("HomeFragment", "User is not authenticated")
        }
    }

    private fun fetchAndDisplayFoodEntries() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val dateString = dateFormatQuery.format(selectedDate)

            db.collection("users").document(userId)
                .collection("foodEntries")
                .whereEqualTo("date", dateString)
                .orderBy("timestamp")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.e("HomeFragment", "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    clearFoodItems()
                    resetConsumedValues()

                    if (snapshots != null && !snapshots.isEmpty) {
                        for (document in snapshots.documents) {
                            val mealType = document.getString("mealType") ?: continue
                            val foodName = document.getString("foodName") ?: ""
                            val foodQuantity = document.getString("foodQuantity") ?: ""
                            val calories = document.getDouble("calories") ?: "0"
                            val carbs = document.getDouble("carbs") ?: "0"
                            val proteins = document.getDouble("proteins") ?: "0"
                            val fats = document.getDouble("fats") ?: "0"

                            addFoodItemToMealFromFirestore(
                                mealType,
                                foodName,
                                foodQuantity,
                                calories.toString(),
                                carbs.toString(),
                                proteins.toString(),
                                fats.toString()
                            )
                        }
                    } else {
                        resetConsumedValues()
                        Log.d("HomeFragment", "No food entries found for $dateString")
                    }

                    // After processing all entries, update the UI
                    updateUI()
                }
        } else {
            Log.e("HomeFragment", "User is not authenticated")
        }
    }

    private fun clearFoodItems() {
        breakfastFoodItemsContainer.removeAllViews()
        lunchFoodItemsContainer.removeAllViews()
        dinnerFoodItemsContainer.removeAllViews()
        snacksFoodItemsContainer.removeAllViews()
    }

    private fun resetConsumedValues() {
        consumedCalories = 0L
        breakfastConsumedCalories = 0L
        lunchConsumedCalories = 0L
        dinnerConsumedCalories = 0L
        snacksConsumedCalories = 0L

        consumedCarbs = 0f
        consumedProteins = 0f
        consumedFats = 0f
    }

    @SuppressLint("SetTextI18n")
    private fun addFoodItemToMealFromFirestore(
        mealType: String,
        foodName: String,
        foodQuantity: String,
        calories: String,
        carbs: String,
        proteins: String,
        fats: String
    ) {
        // Determine the parent container based on meal type
        val parentContainer = when (mealType) {
            "Breakfast" -> breakfastFoodItemsContainer
            "Lunch" -> lunchFoodItemsContainer
            "Dinner" -> dinnerFoodItemsContainer
            "Snacks" -> snacksFoodItemsContainer
            else -> null
        }

        // Ensure the parent container is not null
        if (parentContainer != null) {
            // Inflate the layout with the parent container
            val context = context ?: return
            val inflater = LayoutInflater.from(context)
            val foodItemView = inflater.inflate(R.layout.food_item, parentContainer, false)

            // Find the views inside the food item layout
            val foodNameTextView = foodItemView.findViewById<TextView>(R.id.foodName)
            val foodQuantityTextView = foodItemView.findViewById<TextView>(R.id.foodQuantity)
            val foodCaloriesTextView = foodItemView.findViewById<TextView>(R.id.foodCalories)

            // Set the values to the respective views
            foodNameTextView.text = foodName
            foodQuantityTextView.text = "Servings ${foodQuantity}g"
            val caloriesValueFormatted = String.format(Locale.getDefault(), "%.0f kcal", calories.toDoubleOrNull() ?: 0.0)
            foodCaloriesTextView.text = caloriesValueFormatted

            // Add the view to the appropriate container
            parentContainer.addView(foodItemView)

            // Update consumed calories and nutrients
            val caloriesValue = calories.toDoubleOrNull()?.toInt() ?: 0
            val carbsValue = carbs.toDoubleOrNull()?.toFloat() ?: 0f
            val proteinsValue = proteins.toDoubleOrNull()?.toFloat() ?: 0f
            val fatsValue = fats.toDoubleOrNull()?.toFloat() ?: 0f

            updateConsumedCalories(caloriesValue, mealType)
            updateConsumedNutrients(carbsValue, proteinsValue, fatsValue)
        } else {
            Log.e("HomeFragment", "Invalid meal type: $mealType")
        }
    }

    private fun updateConsumedCalories(addedCalories: Int, mealType: String) {
        consumedCalories += addedCalories

        when (mealType) {
            "Breakfast" -> {
                breakfastConsumedCalories += addedCalories
                "${breakfastConsumedCalories}/${breakfastTotalCalories}".also {
                    breakfastCaloriesTextView.text = it
                }
            }

            "Lunch" -> {
                lunchConsumedCalories += addedCalories
                "${lunchConsumedCalories}/${lunchTotalCalories}".also {
                    lunchCaloriesTextView.text = it
                }
            }

            "Dinner" -> {
                dinnerConsumedCalories += addedCalories
                "${dinnerConsumedCalories}/${dinnerTotalCalories}".also {
                    dinnerCaloriesTextView.text = it
                }
            }

            "Snacks" -> {
                snacksConsumedCalories += addedCalories
                "${snacksConsumedCalories}/${snacksTotalCalories}".also {
                    snackCaloriesTextView.text = it
                }
            }
        }

        consumedValueTextView.text = consumedCalories.toString()
        // Update remaining calories
        remainingCalories = totalCalories - consumedCalories

        // Update remaining value text and change color if negative
        remainingValueTextView.text = remainingCalories.toString()
        if (remainingCalories < 0) {
            remainingValueTextView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.red_600
                )
            )
        } else {
            remainingValueTextView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.black
                )
            )
        }

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
            val colorRes = if (consumedCalories > totalCalories) R.color.red_600 else R.color.green_500
            arcProgressBar.progressColor = ContextCompat.getColor(context ?: return@runOnUiThread, colorRes)
        }

    }

    private fun updateConsumedNutrients(carbs: Float, proteins: Float, fats: Float) {
        consumedCarbs += carbs
        consumedProteins += proteins
        consumedFats += fats

        // Update center texts
        centerTextCarbs.text = String.format(Locale.getDefault(), "%.1fg", consumedCarbs)
        centerTextProteins.text = String.format(Locale.getDefault(), "%.1fg", consumedProteins)
        centerTextFats.text = String.format(Locale.getDefault(), "%.1fg", consumedFats)

        // Update progress indicators
        circularProgressIndicatorCarbs.progress = consumedCarbs.toInt()
        circularProgressIndicatorProteins.progress = consumedProteins.toInt()
        circularProgressIndicatorFats.progress = consumedFats.toInt()

        // Change color if goal exceeded
        val carbsIndicatorColor = if (consumedCarbs <= carbsGoal) {
            ContextCompat.getColor(requireContext(), R.color.blue_700)
        } else {
            ContextCompat.getColor(requireContext(), R.color.red_600)
        }
        val proteinsIndicatorColor = if (consumedProteins <= proteinsGoal) {
            ContextCompat.getColor(requireContext(), R.color.pink_700)
        } else {
            ContextCompat.getColor(requireContext(), R.color.red_600)
        }
        val fatsIndicatorColor = if (consumedFats <= fatsGoal) {
            ContextCompat.getColor(requireContext(), R.color.yellow_700)
        } else {
            ContextCompat.getColor(requireContext(), R.color.red_600)
        }

        circularProgressIndicatorCarbs.setIndicatorColor(carbsIndicatorColor)
        circularProgressIndicatorProteins.setIndicatorColor(proteinsIndicatorColor)
        circularProgressIndicatorFats.setIndicatorColor(fatsIndicatorColor)
    }

    private fun updateUI() {
        // Update total consumed calories and remaining calories
        consumedValueTextView.text = consumedCalories.toString()
        remainingCalories = totalCalories - consumedCalories
        remainingValueTextView.text = remainingCalories.toString()

        // Update ArcProgressBar
        val progress = if (totalCalories > 0) {
            (consumedCalories.toFloat() / totalCalories.toFloat()) * 100
        } else {
            0f
        }
        arcProgressBar.progress = progress

        // Update nutrient progress indicators and center texts
        centerTextCarbs.text = String.format(Locale.getDefault(), "%.1fg", consumedCarbs)
        centerTextProteins.text = String.format(Locale.getDefault(), "%.1fg", consumedProteins)
        centerTextFats.text = String.format(Locale.getDefault(), "%.1fg", consumedFats)

        circularProgressIndicatorCarbs.progress = consumedCarbs.toInt()
        circularProgressIndicatorProteins.progress = consumedProteins.toInt()
        circularProgressIndicatorFats.progress = consumedFats.toInt()
    }
}
