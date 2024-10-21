package com.thesis.sangkapp_ex.ui.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.thesis.sangkapp_ex.R
import com.thesis.sangkapp_ex.UserProfileCalculator
import com.thesis.sangkapp_ex.databinding.FragmentProfileInput2Binding
import kotlinx.coroutines.launch

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

        setupPercentages()

        binding.submitButton.setOnClickListener {
            if (validateInputs()) {
                saveCalorieData()
            }
        }
    }

    private fun setupPercentages() {
        // Set up default percentages
        binding.breakfastPercentage.text = "30%"
        binding.lunchPercentage.text = "30%"
        binding.dinnerPercentage.text = "30%"
        binding.snackPercentage.text = "10%"
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        var totalCalories = 0.0

        if (userProfile == null) {
            Toast.makeText(context, "User profile not loaded", Toast.LENGTH_LONG).show()
            return false
        }

        with(binding) {
            val breakfastCalories = breakfastCalories.text.toString().toDoubleOrNull()
            val lunchCalories = lunchCalories.text.toString().toDoubleOrNull()
            val dinnerCalories = dinnerCalories.text.toString().toDoubleOrNull()
            val snackCalories = snackCalories.text.toString().toDoubleOrNull()

            if (breakfastCalories == null) {
                isValid = false
                this.breakfastCalories.error = "Enter a valid number"
            } else {
                totalCalories += breakfastCalories
            }

            if (lunchCalories == null) {
                isValid = false
                this.lunchCalories.error = "Enter a valid number"
            } else {
                totalCalories += lunchCalories
            }

            if (dinnerCalories == null) {
                isValid = false
                this.dinnerCalories.error = "Enter a valid number"
            } else {
                totalCalories += dinnerCalories
            }

            if (snackCalories == null) {
                isValid = false
                this.snackCalories.error = "Enter a valid number"
            } else {
                totalCalories += snackCalories
            }
        }

        if (!isValid) {
            Toast.makeText(context, "Please correct the errors", Toast.LENGTH_LONG).show()
            return false
        }

        val totalAllowedCalories = userProfile!!.calories

        if (totalCalories > totalAllowedCalories) {
            Toast.makeText(
                context,
                "Total allocated calories exceed your daily allowance of $totalAllowedCalories kcal",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        return true
    }

    private fun saveCalorieData() {
        with(binding) {
            val breakfastCalories = breakfastCalories.text.toString().toIntOrNull() ?: 0
            val lunchCalories = lunchCalories.text.toString().toIntOrNull() ?: 0
            val dinnerCalories = dinnerCalories.text.toString().toIntOrNull() ?: 0
            val snackCalories = snackCalories.text.toString().toIntOrNull() ?: 0

            val totalCalories = userProfile?.calories?.toInt() ?: 0 // Ensure it's Int

            val calorieData = hashMapOf(
                "breakfastCalories" to breakfastCalories,
                "lunchCalories" to lunchCalories,
                "dinnerCalories" to dinnerCalories,
                "snackCalories" to snackCalories,
                "calories" to totalCalories // Include total calories as Int
            )

            // Get the anonymous user's UID from SharedPreferences
            val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val userId = sharedPreferences.getString("USER_ID", null)

            if (userId != null) {
                db.collection("users").document(userId).collection("profile").document(userId)
                    .update(calorieData as Map<String, Any>).addOnSuccessListener {
                        Toast.makeText(
                            context, "Calorie data saved successfully", Toast.LENGTH_SHORT
                        ).show()
                        navigateToNextScreen()
                    }.addOnFailureListener { e ->
                        Toast.makeText(
                            context, "Error saving calorie data: ${e.message}", Toast.LENGTH_LONG
                        ).show()
                    }
            } else {
                Toast.makeText(context, "Error: User ID not found", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun navigateToNextScreen() {
        findNavController().navigate(R.id.action_profileInputFragment2_to_profileDetailsFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
