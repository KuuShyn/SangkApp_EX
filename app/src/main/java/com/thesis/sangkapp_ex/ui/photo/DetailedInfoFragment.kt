package com.thesis.sangkapp_ex.ui.photo

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.thesis.sangkapp_ex.R
import com.thesis.sangkapp_ex.databinding.FragmentDetailedInfoBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Data class representing the nutrient estimation results.
 * Ensure this class implements Serializable or Parcelable to pass it via Safe Args.
 */

class DetailedInfoFragment : Fragment() {

    private var _binding: FragmentDetailedInfoBinding? = null
    private val binding get() = _binding!!

    private val args: DetailedInfoFragmentArgs by navArgs()

    // Firestore instance
    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    // Firestore user ID
    private val userId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    // Date formatter
    private val dateFormatQuery = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Variables to hold allocations and consumed calories
    private var allocations: Allocations? = null
    private var consumedCaloriesMap: MutableMap<String, Int> = mutableMapOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDetailedInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Extract data from arguments
        val foodName = args.foodName
        val croppedImageUri = args.croppedImageUri
        val detectedVolume = args.detectedVolume
        val mealPortion = args.mealPortion
        val nutrientEstimationResult = args.nutrientEstimationResult

        // Load the image from the Uri and set it to an ImageView
        val inputStream = requireContext().contentResolver.openInputStream(croppedImageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        binding.foodImage.setImageBitmap(bitmap)
        binding.foodName.text = foodName

        // Convert detected volume and meal portion to cups
        val detectedVolumeInCups = convertVolumeToCups(detectedVolume)
        val mealPortionInCups = convertGramsToCups(mealPortion)

        binding.circularProgressIndicatorCalories.progress = nutrientEstimationResult.totalCalories.toInt()
        binding.circularProgressIndicatorCarbs.progress = nutrientEstimationResult.carbohydrateTotal.toInt()
        binding.circularProgressIndicatorProteins.progress = nutrientEstimationResult.protein.toInt()
        binding.circularProgressIndicatorFats.progress = nutrientEstimationResult.totalFat.toInt()

        binding.centerTextCalories.text = String.format(Locale.getDefault(), "%.2f kcal", nutrientEstimationResult.totalCalories)
        binding.centerTextCarbs.text = String.format(Locale.getDefault(), "%.2f g", nutrientEstimationResult.carbohydrateTotal)
        binding.centerTextProteins.text = String.format(Locale.getDefault(), "%.2f g", nutrientEstimationResult.protein)
        binding.centerTextFats.text = String.format(Locale.getDefault(), "%.2f g", nutrientEstimationResult.totalFat)

        binding.detectedVolumeValue.text = String.format(Locale.getDefault(), "%.2f cups", detectedVolumeInCups)
        binding.maximumMealPortionValue.text = String.format(Locale.getDefault(), "%.2f cups", mealPortionInCups)

        // Set Nutritional Information
        binding.caloriesValue.text = String.format(Locale.getDefault(), "%.2f kcal", nutrientEstimationResult.totalCalories)
        binding.carbsValue.text = String.format(Locale.getDefault(), "%.2f g", nutrientEstimationResult.carbohydrateTotal)
        binding.proteinsValue.text = String.format(Locale.getDefault(), "%.2f g", nutrientEstimationResult.protein)
        binding.fatsValue.text = String.format(Locale.getDefault(), "%.2f g", nutrientEstimationResult.totalFat)
        binding.fibersValue.text = String.format(Locale.getDefault(), "%.2f g", nutrientEstimationResult.fiberTotalDietary)
        binding.sugarsValue.text = String.format(Locale.getDefault(), "%.2f g", nutrientEstimationResult.sugarsTotal)
        binding.cholesterolValue.text = String.format(Locale.getDefault(), "%.2f g", nutrientEstimationResult.cholesterol)
        binding.sodiumValue.text = String.format(Locale.getDefault(), "%.2f g", nutrientEstimationResult.sodiumNa)
        binding.calciumValue.text = String.format(Locale.getDefault(), "%.2f g", nutrientEstimationResult.calciumCa)
        binding.ironValue.text = String.format(Locale.getDefault(), "%.2f g", nutrientEstimationResult.ironFe)
        binding.vitaminARetinolValue.text = String.format(Locale.getDefault(), "%.2f g", nutrientEstimationResult.vitaminA)
        binding.vitaminB1Value.text = String.format(Locale.getDefault(), "%.2f g", nutrientEstimationResult.vitaminB1)
        binding.vitaminB2Value.text = String.format(Locale.getDefault(), "%.2f g", nutrientEstimationResult.vitaminB2)
        binding.vitaminB3Value.text = String.format(Locale.getDefault(), "%.2f g", nutrientEstimationResult.niacin)
        binding.vitaminC5Value.text = String.format(Locale.getDefault(), "%.2f g", nutrientEstimationResult.vitaminC)

        // Set up the dropdown menu
        setupDropdownMenu()

        // Fetch allocations and consumed calories
        fetchAllocationsAndConsumedCalories()
    }

    /**
     * Data class to hold allocations.
     */
    data class Allocations(
        val breakfastCalories: Int = 0,
        val lunchCalories: Int = 0,
        val dinnerCalories: Int = 0,
        val snackCalories: Int = 0,
        val totalCalories: Int = 0
    )

    /**
     * Fetches calorie allocations and consumed calories from Firestore.
     */
    private fun fetchAllocationsAndConsumedCalories() {
        if (userId == null) {
            Log.e("DetailedInfoFragment", "User is not authenticated")
            return
        }

        // Fetch allocations
        db.collection("users").document(userId!!)
            .collection("profile").document(userId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    allocations = Allocations(
                        breakfastCalories = document.getLong("breakfastCalories")?.toInt() ?: 0,
                        lunchCalories = document.getLong("lunchCalories")?.toInt() ?: 0,
                        dinnerCalories = document.getLong("dinnerCalories")?.toInt() ?: 0,
                        snackCalories = document.getLong("snackCalories")?.toInt() ?: 0,
                        totalCalories = document.getLong("calories")?.toInt() ?: 0
                    )
                    Log.d("DetailedInfoFragment", "Allocations fetched: $allocations")
                    fetchConsumedCalories()
                } else {
                    Log.e("DetailedInfoFragment", "No profile data found")
                }
            }
            .addOnFailureListener { e ->
                Log.e("DetailedInfoFragment", "Error fetching allocations", e)
            }
    }

    /**
     * Fetches consumed calories for each meal type.
     */
    private fun fetchConsumedCalories() {
        if (userId == null || allocations == null) {
            Log.e("DetailedInfoFragment", "User is not authenticated or allocations not fetched")
            return
        }
        val selectedDate: java.util.Date = Calendar.getInstance().time

         // Assuming you pass the date as an argument
        val dateString = dateFormatQuery.format(selectedDate)

        // Initialize consumed calories map
        consumedCaloriesMap = mutableMapOf(
            "Breakfast" to 0,
            "Lunch" to 0,
            "Dinner" to 0,
            "Snack" to 0
        )

        db.collection("users").document(userId!!)
            .collection("foodEntries")
            .whereEqualTo("date", dateString)
            .get()
            .addOnSuccessListener { snapshots ->
                for (document in snapshots.documents) {
                    val mealType = document.getString("mealType") ?: continue
                    val caloriesStr = document.getString("calories") ?: "0"
                    val calories = caloriesStr.toIntOrNull() ?: 0
                    consumedCaloriesMap[mealType] = consumedCaloriesMap.getOrDefault(mealType, 0) + calories
                }
                Log.d("DetailedInfoFragment", "Consumed calories: $consumedCaloriesMap")
                // After fetching consumed calories, update the recommendation if a meal is selected
                val selectedMeal = binding.dropdownMenu.text.toString()
                if (selectedMeal.isNotEmpty()) {
                    updateRecommendation(selectedMeal)
                }
            }
            .addOnFailureListener { e ->
                Log.e("DetailedInfoFragment", "Error fetching consumed calories", e)
            }
    }

    /**
     * Sets up the dropdown menu with meal options.
     */
    private fun setupDropdownMenu() {
        // Define the options for the dropdown
        val mealOptions = listOf("Breakfast", "Lunch", "Dinner", "Snack")

        // Create an ArrayAdapter using the string array and a default dropdown layout
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_menu_item, // A custom layout for dropdown items
            mealOptions
        )

        // Set the adapter to the MaterialAutoCompleteTextView
        binding.dropdownMenu.setAdapter(adapter)

        // Optionally, set a default selection
        binding.dropdownMenu.setText("Breakfast", false)

        // Handle item selection
        binding.dropdownMenu.setOnItemClickListener { parent, view, position, id ->
            val selectedMeal = parent.getItemAtPosition(position) as String
            updateRecommendation(selectedMeal)
        }
    }

    /**
     * Updates the recommendation based on the selected meal.
     *
     * @param selectedMeal The meal selected by the user.
     */
    private fun updateRecommendation(selectedMeal: String) {
        if (allocations == null) {
            Log.e("DetailedInfoFragment", "Allocations not fetched yet")
            return
        }

        val allocatedCalories = when (selectedMeal) {
            "Breakfast" -> allocations!!.breakfastCalories
            "Lunch" -> allocations!!.lunchCalories
            "Dinner" -> allocations!!.dinnerCalories
            "Snack" -> allocations!!.snackCalories
            else -> 0
        }

        val consumedCalories = consumedCaloriesMap[selectedMeal] ?: 0
        val remainingCalories = allocatedCalories - consumedCalories

        val recommendationText = if (remainingCalories > 0) {
            "You can consume up to $remainingCalories kcal for $selectedMeal."
        } else {
            "You have reached your allocated calories for $selectedMeal."
        }

        binding.recommendationValue.text = recommendationText
    }

    /**
     * Converts volume from cubic centimeters to cups.
     *
     * @param volumeInCm3 Volume in cubic centimeters.
     * @return Volume in cups.
     */
    private fun convertVolumeToCups(volumeInCm3: Float): Float {
        return volumeInCm3 / 240
    }

    /**
     * Converts weight from grams to cups.
     *
     * @param grams Weight in grams.
     * @return Weight in cups.
     */
    private fun convertGramsToCups(grams: Float): Float {
        return grams / 240
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
