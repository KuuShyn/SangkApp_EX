package com.thesis.sangkapp_ex.ui.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.thesis.sangkapp_ex.R
import com.thesis.sangkapp_ex.UserProfileCalculator
import com.thesis.sangkapp_ex.databinding.FragmentProfileInput2Binding
import kotlinx.coroutines.launch
import androidx.core.content.edit

class ProfileInputFragment2 : Fragment() {

    private var _binding: FragmentProfileInput2Binding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore

    private var userProfile: UserProfile? = null // Added this line

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileInput2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val calculator = UserProfileCalculator(requireContext())
        viewLifecycleOwner.lifecycleScope.launch {
            userProfile = calculator.calculateUserProfile()
            if (userProfile != null) {
                binding.caloriesValue.text = userProfile!!.calories.toString()
            } else {
                Toast.makeText(context, "Failed to load user profile", Toast.LENGTH_LONG).show()
            }
        }

        binding.submitButton.setOnClickListener {
            if (validateInputs()) {
                saveCalorieData()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        var totalPercent = 0.0

        if (userProfile == null) {
            Toast.makeText(context, "User profile not loaded", Toast.LENGTH_LONG).show()
            return false
        }

        with(binding) {
            val fields = listOf(
                breakfastCalories to "Breakfast",
                lunchCalories to "Lunch",
                dinnerCalories to "Dinner",
                snackCalories to "Snack"
            )

            fields.forEach { (editText, _) ->
                val rawInput = editText.text.toString().trim().removeSuffix("%")
                val percent = rawInput.toDoubleOrNull()

                if (percent == null || percent < 0.0 || percent > 100.0) {
                    isValid = false
                    editText.error = "Enter a valid %"
                } else {
                    totalPercent += percent
                }
            }
        }

        if (!isValid) {
            Toast.makeText(context, "Please correct the invalid percentage(s)", Toast.LENGTH_LONG).show()
            return false
        }

        if (totalPercent > 100.0) {
            Toast.makeText(
                context,
                "Total allocation exceeds 100% ($totalPercent%) — please adjust.",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        return true
    }


    private fun saveCalorieData() {
        with(binding) {
            val totalCalories = userProfile?.calories?.toInt() ?: return
            val totalCarbs = userProfile?.carbs ?: return
            val totalProteins = userProfile?.protein ?: return
            val totalFats = userProfile?.fat ?: return

            fun percentToKcal(editText: android.widget.EditText): Int {
                val percent = editText.text.toString().trim().removeSuffix("%").toDoubleOrNull() ?: 0.0
                return ((totalCalories * percent) / 100.0).toInt()
            }

            val breakfastCalories = percentToKcal(breakfastCalories)
            val lunchCalories = percentToKcal(lunchCalories)
            val dinnerCalories = percentToKcal(dinnerCalories)
            val snackCalories = percentToKcal(snackCalories)

            // 🍽️ Macronutrients (60% carbs, 15% protein, 25% fat)
            fun macrosFromCalories(kcal: Int): Triple<Int, Int, Int> {
                val carbs = ((kcal * 0.6) / 4).toInt()
                val protein = ((kcal * 0.15) / 4).toInt()
                val fat = ((kcal * 0.25) / 9).toInt()
                return Triple(carbs, protein, fat)
            }

            val (breakfastCarbs, breakfastProtein, breakfastFat) = macrosFromCalories(breakfastCalories)
            val (lunchCarbs, lunchProtein, lunchFat) = macrosFromCalories(lunchCalories)
            val (dinnerCarbs, dinnerProtein, dinnerFat) = macrosFromCalories(dinnerCalories)
            val (snackCarbs, snackProtein, snackFat) = macrosFromCalories(snackCalories)

            val calorieData = hashMapOf(
                "calories" to totalCalories,
                "carbs" to totalCarbs,
                "proteins" to totalProteins,
                "fats" to totalFats,
                "breakfastCalories" to breakfastCalories,
                "breakfastCarbs" to breakfastCarbs,
                "breakfastProteins" to breakfastProtein,
                "breakfastFats" to breakfastFat,
                "lunchCalories" to lunchCalories,
                "lunchCarbs" to lunchCarbs,
                "lunchProteins" to lunchProtein,
                "lunchFats" to lunchFat,
                "dinnerCalories" to dinnerCalories,
                "dinnerCarbs" to dinnerCarbs,
                "dinnerProteins" to dinnerProtein,
                "dinnerFats" to dinnerFat,
                "snackCalories" to snackCalories,
                "snackCarbs" to snackCarbs,
                "snackProteins" to snackProtein,
                "snackFats" to snackFat
            )

            val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val userId = sharedPreferences.getString("USER_ID", null)

            if (userId != null) {
                db.collection("users").document(userId).collection("profile").document(userId)
                    .update(calorieData as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Macros + Calorie data saved", Toast.LENGTH_SHORT).show()
                        navigateToNextScreen()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error saving data: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(context, "Error: User ID not found", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun navigateToNextScreen() {
        // Mark profile as complete in SharedPreferences
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.profile_input2, true) // 💥 Pop everything INCLUDING input2
            .setLaunchSingleTop(true)              // 🧠 Prevent duplicate destinations
            .setRestoreState(false)
            .build()

        findNavController().navigate(R.id.nav_home, null, navOptions)

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
