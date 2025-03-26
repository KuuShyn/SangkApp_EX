package com.thesis.sangkapp_ex.ui.photo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.thesis.sangkapp_ex.R
import java.util.Locale
import kotlin.text.*

// In NutritionBottomSheet.kt

class NutritionBottomSheet : BottomSheetDialogFragment() {
    private var calorieGoal: Float = 2000f
    private var carbsGoal: Float = 300f
    private var proteinGoal: Float = 150f
    private var fatsGoal: Float = 70f
    private val userId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_nutrition, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from(bottomSheet!!)
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        behavior.peekHeight = 330
        behavior.isHideable = false

        val dishesDetectedValue: TextView = view.findViewById(R.id.dishesDetectedValue)
        val combinedCaloriesValue: TextView = view.findViewById(R.id.combinedCaloriesValue)

        val caloriesProgress: CircularProgressIndicator = view.findViewById(R.id.circularProgressIndicatorCalories)
        val caloriesText: TextView = view.findViewById(R.id.centerTextCalories)

        val carbsProgress: CircularProgressIndicator = view.findViewById(R.id.circularProgressIndicatorCarbs)
        val carbsText: TextView = view.findViewById(R.id.centerTextCarbs)

        val proteinsProgress: CircularProgressIndicator = view.findViewById(R.id.circularProgressIndicatorProteins)
        val proteinsText: TextView = view.findViewById(R.id.centerTextProteins)

        val fatsProgress: CircularProgressIndicator = view.findViewById(R.id.circularProgressIndicatorYellow)
        val fatsText: TextView = view.findViewById(R.id.centerTextYellow)

        val args = arguments
        val dishesDetected = args?.getInt("dishesDetected", 0) ?: 0
        val combinedCalories = args?.getInt("combinedCalories", 0) ?: 0
        val calories = args?.getFloat("calories", 0f) ?: 0f
        val carbs = args?.getFloat("carbs", 0f) ?: 0f
        val proteins = args?.getFloat("proteins", 0f) ?: 0f
        val fats = args?.getFloat("fats", 0f) ?: 0f

        dishesDetectedValue.text = dishesDetected.toString()
        combinedCaloriesValue.text = "$combinedCalories kcal"

        fetchUserGoals { // Pull from Firestore FIRST, then animate UI
            updateProgressIndicator(caloriesProgress, caloriesText, calories, calorieGoal, "")
            updateProgressIndicator(carbsProgress, carbsText, carbs, carbsGoal, "g")
            updateProgressIndicator(proteinsProgress, proteinsText, proteins, proteinGoal, "g")
            updateProgressIndicator(fatsProgress, fatsText, fats, fatsGoal, "g")
        }
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
        val progress = (value / goal * 100).coerceAtMost(100f).toInt()
        val context = requireContext()

        val color = if (value > goal) {
            ContextCompat.getColor(context, R.color.red_600)
        } else {
            when (progressIndicator.id) {
                R.id.circularProgressIndicatorCarbs -> ContextCompat.getColor(context, R.color.blue_700)
                R.id.circularProgressIndicatorProteins -> ContextCompat.getColor(context, R.color.pink_700)
                R.id.circularProgressIndicatorYellow -> ContextCompat.getColor(context, R.color.yellow_700)
                else -> ContextCompat.getColor(context, R.color.green_500) // calories
            }
        }

        progressIndicator.setIndicatorColor(color)
        progressIndicator.setProgressCompat(progress, true)
        centerTextView.text = String.format(Locale.getDefault(), "%.1f %s", value, unit)
    }

    private fun fetchUserGoals(onComplete: () -> Unit) {
        val uid = userId ?: return onComplete()

        db.collection("users")
            .document(uid)
            .collection("profile")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                calorieGoal = doc.getLong("calories")?.toFloat() ?: calorieGoal
                carbsGoal = doc.getLong("carbs")?.toFloat() ?: carbsGoal
                proteinGoal = doc.getLong("proteins")?.toFloat() ?: proteinGoal
                fatsGoal = doc.getLong("fats")?.toFloat() ?: fatsGoal
            }
            .addOnFailureListener {
                // fallback to default if failed
            }
            .addOnCompleteListener {
                onComplete()
            }
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
