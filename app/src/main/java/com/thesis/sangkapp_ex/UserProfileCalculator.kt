package com.thesis.sangkapp_ex

import android.content.Context
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.thesis.sangkapp_ex.ui.profile.UserProfile
import kotlinx.coroutines.tasks.await
import kotlin.math.pow
import kotlin.math.round

class UserProfileCalculator(private val context: Context) {

    private val db = Firebase.firestore
    private val TAG = "UserProfileCalculator"

    suspend fun calculateUserProfile(): UserProfile? {
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("USER_ID", null)
        if (userId != null) {
            try {
                val userDoc =
                    db.collection("users")
                        .document(userId)
                        .collection("profile")
                        .document(userId)
                        .get().await()

                if (userDoc.exists()) {
                    val name = userDoc.getString("name") ?: ""
                    val age = userDoc.getLong("age")?.toInt() ?: 0
                    val weight = userDoc.getDouble("weight")?.toFloat() ?: 0f
                    val heightFt = userDoc.getLong("heightFt")?.toInt() ?: 0
                    val heightIn = userDoc.getLong("heightIn")?.toInt() ?: 0
                    val sex = userDoc.getString("sex") ?: ""

                    // Convert height to cm
                    val totalHeightIn = heightFt * 12 + heightIn
                    val heightCm = totalHeightIn * 2.54

                    val ibw = computeIbw(age, heightCm)
                    val (bmi, bmiCategory) = computeBmi(weight, heightCm)
                    val ter = computeTer(ibw)
                    val weightChangeMode = defineWeightChangeMode(bmiCategory)

                    return UserProfile(
                        name = name,
                        age = age,
                        gender = sex,
                        heightFt = heightFt,
                        heightIn = heightIn,
                        weight = weight,
                        bmi = bmi,
                        bmiCategory = bmiCategory,
                        idealBodyWeight = ibw,
                        weightChangeMode = weightChangeMode,
                        calories = ter,
                        carbs = 307,    // Placeholder values; replace with actual data
                        protein = 71,    // Placeholder values; replace with actual data
                        fat = 52,        // Placeholder values; replace with actual data

                    )
                } else {
                    Log.e(TAG, "User document does not exist")
                    return null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user profile: ${e.message}")
                return null
            }
        } else {
            Log.e(TAG, "User ID not found")
            return null
        }
    }

    private fun computeIbw(age: Int, heightCm: Double): Int {
        return when {
            age in 1..12 -> (age * 2) + 8
            age >= 13 -> {
                val x = heightCm - 100
                round(x - (0.10 * x)).toInt()
            }

            else -> throw IllegalArgumentException("Invalid age. Age must be 1 or older.")
        }
    }

    private fun computeBmi(weightKg: Float, heightCm: Double): Pair<Double, String> {
        val heightM = heightCm / 100
        val index = weightKg / heightM.pow(2)
        val roundedIndex = round(index * 100) / 100

        val category = when {
            roundedIndex < 18.5 -> "Underweight"
            roundedIndex in 18.5..22.9 -> "Normal"
            roundedIndex in 23.0..24.9 -> "Overweight"
            else -> "Obese"
        }

        return Pair(roundedIndex, category)
    }

    private fun computeTer(ibw: Int): Double {
        return (ibw * 30).toDouble()
    }

    private fun defineWeightChangeMode(bmiCategory: String): String {
        return when (bmiCategory) {
            "Underweight" -> "gain"
            "Overweight", "Obese" -> "lose"
            else -> "maintain"
        }
    }
}
