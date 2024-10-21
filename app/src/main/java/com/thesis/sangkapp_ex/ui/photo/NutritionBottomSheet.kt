package com.thesis.sangkapp_ex.ui.photo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.thesis.sangkapp_ex.R
import java.util.Locale
import kotlin.text.*

// In NutritionBottomSheet.kt

class NutritionBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_nutrition, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up BottomSheetBehavior to enable full expansion and prevent full hide
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet!!)

        // Allow full expansion
        behavior.isFitToContents = true
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        behavior.peekHeight = 300  // Minimum visible height when collapsed

        // Prevent hiding when dragged down
        behavior.isHideable = false

        // Optional: Set the maximum height programmatically if needed
        bottomSheet.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT

        // Find views
        val dishesDetectedValue: TextView = view.findViewById(R.id.dishesDetectedValue)
        val combinedCaloriesValue: TextView = view.findViewById(R.id.combinedCaloriesValue)

        val caloriesProgress: CircularProgressIndicator =
            view.findViewById(R.id.circularProgressIndicatorCalories)
        val caloriesText: TextView = view.findViewById(R.id.centerTextCalories)

        val carbsProgress: CircularProgressIndicator =
            view.findViewById(R.id.circularProgressIndicatorCarbs)
        val carbsText: TextView = view.findViewById(R.id.centerTextCarbs)

        val proteinsProgress: CircularProgressIndicator =
            view.findViewById(R.id.circularProgressIndicatorProteins)
        val proteinsText: TextView = view.findViewById(R.id.centerTextProteins)

        val fatsProgress: CircularProgressIndicator =
            view.findViewById(R.id.circularProgressIndicatorYellow)
        val fatsText: TextView = view.findViewById(R.id.centerTextYellow)

        // Retrieve the arguments passed from PhotoFragment
        val args = arguments
        val dishesDetected = args?.getInt("dishesDetected", 0) ?: 0
        val combinedCalories = args?.getInt("combinedCalories", 0) ?: 0
        val calories = args?.getFloat("calories", 0f) ?: 0f
        val carbs = args?.getFloat("carbs", 0f) ?: 0f
        val proteins = args?.getFloat("proteins", 0f) ?: 0f
        val fats = args?.getFloat("fats", 0f) ?: 0f

        // Set the values on the UI
        dishesDetectedValue.text = dishesDetected.toString()
        combinedCaloriesValue.text = "$combinedCalories kcal"

        // Update the CircularProgressIndicators and their center text values
        updateProgressIndicator(
            caloriesProgress,
            caloriesText,
            calories,
            2000f,
            "kcal"
        ) // Assuming 2000 kcal is the daily goal
        updateProgressIndicator(
            carbsProgress,
            carbsText,
            carbs,
            300f,
            "g"
        ) // Assuming 300g carbs goal
        updateProgressIndicator(
            proteinsProgress,
            proteinsText,
            proteins,
            150f,
            "g"
        ) // Assuming 150g proteins goal
        updateProgressIndicator(fatsProgress, fatsText, fats, 70f, "g") // Assuming 70g fats goal
    }

    /**
     * Helper method to update the progress indicator and text
     */
    private fun updateProgressIndicator(
        progressIndicator: CircularProgressIndicator,
        centerTextView: TextView,
        value: Float,
        goal: Float,
        unit: String
    ) {
        val progress = (value / goal * 100).toInt()
        progressIndicator.progress = progress
        centerTextView.text = String.format(Locale.getDefault(), "%.1f %s", value, unit)
    }

    companion object {
        fun newInstance(
            dishesDetected: Int,
            combinedCalories: Int,
            calories: Float,
            carbs: Float,
            proteins: Float,
            fats: Float
        ): NutritionBottomSheet {
            return NutritionBottomSheet().apply {
                arguments = Bundle().apply {
                    putInt("dishesDetected", dishesDetected)
                    putInt("combinedCalories", combinedCalories)
                    putFloat("calories", calories)
                    putFloat("carbs", carbs)
                    putFloat("proteins", proteins)
                    putFloat("fats", fats)
                }
            }
        }
    }
}
