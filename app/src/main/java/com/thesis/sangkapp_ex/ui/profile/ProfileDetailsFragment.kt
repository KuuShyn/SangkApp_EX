package com.thesis.sangkapp_ex.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.thesis.sangkapp_ex.R
import com.thesis.sangkapp_ex.UserProfileCalculator
import com.thesis.sangkapp_ex.databinding.FragmentProfileDetailsBinding
import kotlinx.coroutines.launch

class ProfileDetailsFragment : Fragment() {

    private var _binding: FragmentProfileDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val calculator = UserProfileCalculator(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            val userProfile: UserProfile? = calculator.calculateUserProfile()
            if (userProfile != null) {
                displayUserProfile(userProfile)
            } else {
                Toast.makeText(context, "Failed to load user profile", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun displayUserProfile(userProfile: UserProfile) {
        binding.apply {
            nameText.text = userProfile.name
            "Age: ${userProfile.age} years old".also { ageText.text = it }
            "Gender: ${userProfile.gender}".also { genderText.text = it }
            "Height: ${userProfile.heightFt}'${userProfile.heightIn}\"".also { heightText.text = it }
            "Weight: ${userProfile.weight} kg".also { weightText.text = it }
            "BMI: ${userProfile.bmi}".also { bmiText.text = it }
            "BMI Category: ${userProfile.bmiCategory}".also { bmiCategoryText.text = it }
            "Ideal Body Weight: ${userProfile.idealBodyWeight} kg".also { idealWeightText.text = it }
            "Mode: ${userProfile.weightChangeMode.uppercase()} weight".also { modeText.text = it }
            "Calories: ${userProfile.calories} kcal".also { caloriesText.text = it }
            "Carbohydrates: ${userProfile.carbs} grams".also { carbsText.text = it }
            "Protein: ${userProfile.protein} grams".also { proteinText.text = it }
            "Fat: ${userProfile.fat} grams".also { fatText.text = it }

            profileImage.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.sangkapp_placeholder
                )
            )

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
