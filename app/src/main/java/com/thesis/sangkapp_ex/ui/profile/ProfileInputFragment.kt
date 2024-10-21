package com.thesis.sangkapp_ex.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import android.content.Context
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.thesis.sangkapp_ex.R
import com.thesis.sangkapp_ex.databinding.FragmentProfileInputBinding

class ProfileInputFragment : Fragment() {

    private var _binding: FragmentProfileInputBinding? = null
    private val binding get() = _binding!!
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.submitButton.setOnClickListener {
            if (validateInputs()) {
                saveProfileData()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        with(binding) {
            if (nameEditText.text.isNullOrBlank()) {
                nameInputLayout.error = "Name is required"
                isValid = false
            } else {
                nameInputLayout.error = null
            }

            if (ageEditText.text.isNullOrBlank()) {
                ageInputLayout.error = "Age is required"
                isValid = false
            } else {
                ageInputLayout.error = null
            }

            if (weightEditText.text.isNullOrBlank()) {
                weightInputLayout.error = "Weight is required"
                isValid = false
            } else {
                weightInputLayout.error = null
            }

            if (heightFtEditText.text.isNullOrBlank() || heightInEditText.text.isNullOrBlank()) {
                heightFtInputLayout.error = "Height is required"
                heightInInputLayout.error = "Height is required"
                isValid = false
            } else {
                heightFtInputLayout.error = null
                heightInInputLayout.error = null
            }

            if (sexRadioGroup.checkedRadioButtonId == -1) {
                // Show an error message for sex selection
                // You might want to add a TextView for this error in your layout
                isValid = false
            }
        }

        return isValid
    }

    private fun saveProfileData() {
        with(binding) {
            val name = nameEditText.text.toString()
            val age = ageEditText.text.toString().toIntOrNull() ?: 0
            val weight = weightEditText.text.toString().toFloatOrNull() ?: 0f
            val heightFt = heightFtEditText.text.toString().toIntOrNull() ?: 0
            val heightIn = heightInEditText.text.toString().toIntOrNull() ?: 0
            val sex = if (maleRadioButton.isChecked) "Male" else "Female"

            val userProfile = hashMapOf(
                "name" to name,
                "age" to age,
                "weight" to weight,
                "heightFt" to heightFt,
                "heightIn" to heightIn,
                "sex" to sex
            )

            // Get the anonymous user's UID from SharedPreferences
            val sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val userId = sharedPreferences.getString("USER_ID", null)

            if (userId != null) {
                // Use the UID to write profile data to Firestore
                db.collection("users").document(userId)
                    .collection("profile") // Saving profile as a sub-collection of the user's document
                    .document(userId) // Use UID as the document ID
                    .set(userProfile) // Use .set() to override or create the document
                    .addOnSuccessListener {
                        Toast.makeText(context, "Profile saved successfully", Toast.LENGTH_SHORT).show()
                        navigateToNextFragment()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error saving profile: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(context, "Error: User ID not found", Toast.LENGTH_LONG).show()
            }
        }
    }



    private fun navigateToNextFragment() {
        findNavController().navigate(R.id.action_profileInputFragment_to_profileInputFragment2)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}