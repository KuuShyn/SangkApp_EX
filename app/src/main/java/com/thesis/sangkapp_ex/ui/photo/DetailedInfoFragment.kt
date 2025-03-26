    package com.thesis.sangkapp_ex.ui.photo

    import android.graphics.BitmapFactory
    import android.os.Bundle
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.ArrayAdapter
    import android.widget.Toast
    import androidx.core.content.ContextCompat
    import androidx.fragment.app.Fragment
    import androidx.navigation.fragment.navArgs
    import com.thesis.sangkapp_ex.R
    import com.thesis.sangkapp_ex.databinding.FragmentDetailedInfoBinding
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.firestore.FirebaseFirestore
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

        // Variables to hold allocations and consumed nutrients
        private var allocations: Allocations? = null
        private var consumedNutrientsMap: MutableMap<String, Nutrients> = mutableMapOf()

        // Nutrient Estimation Result
        private lateinit var nutrientEstimationResult: NutrientEstimationResult

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
            nutrientEstimationResult = args.nutrientEstimationResult

            // Load the image from the Uri and set it to an ImageView
            val inputStream = requireContext().contentResolver.openInputStream(croppedImageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            binding.foodImage.setImageBitmap(bitmap)
            binding.foodName.text = formatFoodName(foodName)


            // Convert detected volume and meal portion to cups
            val detectedVolumeInCups = convertVolumeToCups(detectedVolume)
            val mealPortionInCups = convertGramsToCups(mealPortion)

            binding.centerTextCalories.text = String.format(Locale.getDefault(), "%.2f", nutrientEstimationResult.totalCalories)
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

            // Fetch allocations and consumed nutrients
            fetchAllocationsAndConsumedCalories()

            binding.btnAddToMeal.setOnClickListener {
                val selectedMeal = binding.dropdownMenu.text.toString()
                if (selectedMeal.isEmpty()) {
                    Toast.makeText(requireContext(), "Please select a meal type", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val logEntry = hashMapOf(
                    "calories" to nutrientEstimationResult.totalCalories.toInt(),
                    "carbs" to nutrientEstimationResult.carbohydrateTotal.toDouble(),
                    "fats" to nutrientEstimationResult.totalFat.toDouble(),
                    "proteins" to nutrientEstimationResult.protein.toDouble(),
                    "date" to dateFormatQuery.format(Calendar.getInstance().time),
                    "foodName" to formatFoodName(args.foodName),
                    "foodQuantity" to args.mealPortion.toInt().toString(),
                    "mealType" to selectedMeal,
                    "timestamp" to System.currentTimeMillis()
                )

                userId?.let { uid ->
                    db.collection("users")
                        .document(uid)
                        .collection("foodEntries")
                        .add(logEntry)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Meal logged ✅", Toast.LENGTH_SHORT).show()
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Failed to log meal: ${e.message}", Toast.LENGTH_SHORT).show()
                            Log.e("DetailedInfoFragment", "Error adding meal", e)
                        }
                }
            }

        }

        /**
         * Data class to hold allocations.
         */
        data class Allocations(
            val breakfastCalories: Int = 0,
            val lunchCalories: Int = 0,
            val dinnerCalories: Int = 0,
            val snackCalories: Int = 0,
            val breakfastCarbs: Int = 0,
            val lunchCarbs: Int = 0,
            val dinnerCarbs: Int = 0,
            val snackCarbs: Int = 0,
            val breakfastProteins: Int = 0,
            val lunchProteins: Int = 0,
            val dinnerProteins: Int = 0,
            val snackProteins: Int = 0,
            val breakfastFats: Int = 0,
            val lunchFats: Int = 0,
            val dinnerFats: Int = 0,
            val snackFats: Int = 0,
            val totalCalories: Int = 0
        )

        /**
         * Data class to hold consumed nutrients.
         */
        data class Nutrients(
            var calories: Int = 0,
            var carbs: Int = 0,
            var proteins: Int = 0,
            var fats: Int = 0
        )

        /**
         * Fetches calorie and nutrient allocations from Firestore.
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
                            breakfastCarbs = document.getLong("breakfastCarbs")?.toInt() ?: 0,
                            lunchCarbs = document.getLong("lunchCarbs")?.toInt() ?: 0,
                            dinnerCarbs = document.getLong("dinnerCarbs")?.toInt() ?: 0,
                            snackCarbs = document.getLong("snackCarbs")?.toInt() ?: 0,
                            breakfastProteins = document.getLong("breakfastProteins")?.toInt() ?: 0,
                            lunchProteins = document.getLong("lunchProteins")?.toInt() ?: 0,
                            dinnerProteins = document.getLong("dinnerProteins")?.toInt() ?: 0,
                            snackProteins = document.getLong("snackProteins")?.toInt() ?: 0,
                            breakfastFats = document.getLong("breakfastFats")?.toInt() ?: 0,
                            lunchFats = document.getLong("lunchFats")?.toInt() ?: 0,
                            dinnerFats = document.getLong("dinnerFats")?.toInt() ?: 0,
                            snackFats = document.getLong("snackFats")?.toInt() ?: 0,
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
         * Fetches consumed nutrients for each meal type.
         */
        private fun fetchConsumedCalories() {
            if (userId == null || allocations == null) {
                Log.e("DetailedInfoFragment", "User is not authenticated or allocations not fetched")
                return
            }
            val selectedDate: java.util.Date = Calendar.getInstance().time

            val dateString = dateFormatQuery.format(selectedDate)

            // Initialize consumed nutrients map
            consumedNutrientsMap = mutableMapOf(
                "Breakfast" to Nutrients(),
                "Lunch" to Nutrients(),
                "Dinner" to Nutrients(),
                "Snack" to Nutrients()
            )

            db.collection("users").document(userId!!)
                .collection("foodEntries")
                .whereEqualTo("date", dateString)
                .get()
                .addOnSuccessListener { snapshots ->
                    for (document in snapshots.documents) {
                        val mealType = document.getString("mealType") ?: continue
                        val nutrients = Nutrients(
                            calories = parseNumber(document.get("calories")),
                            carbs = parseNumber(document.get("carbs")),
                            proteins = parseNumber(document.get("proteins")),
                            fats = parseNumber(document.get("fats"))
                        )


                        val existing = consumedNutrientsMap[mealType] ?: Nutrients()
                        consumedNutrientsMap[mealType] = Nutrients(
                            calories = existing.calories + nutrients.calories,
                            carbs = existing.carbs + nutrients.carbs,
                            proteins = existing.proteins + nutrients.proteins,
                            fats = existing.fats + nutrients.fats
                        )
                    }
                    Log.d("DetailedInfoFragment", "Consumed nutrients: $consumedNutrientsMap")
                    // After fetching consumed nutrients, update the recommendation and progress indicators
                    val selectedMeal = binding.dropdownMenu.text.toString()
                    if (selectedMeal.isNotEmpty()) {
                        updateRecommendation(selectedMeal)
                        updateProgressIndicators(selectedMeal)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("DetailedInfoFragment", "Error fetching consumed nutrients", e)
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
            binding.dropdownMenu.setOnItemClickListener { parent, _, position, _ ->
                val selectedMeal = parent.getItemAtPosition(position) as String
                updateRecommendation(selectedMeal)
                updateProgressIndicators(selectedMeal)
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

            val calories = nutrientEstimationResult.totalCalories
            val allocatedCalories = getAllocatedNutrient(selectedMeal, "calories")
            val consumedCalories = consumedNutrientsMap[selectedMeal]?.calories ?: 0
            val remainingCalories = allocatedCalories - consumedCalories

            val recommendationText = if (remainingCalories >= calories.toInt()) {
                "You can consume this meal. You’ll still have ${remainingCalories - calories.toInt()} kcal left for $selectedMeal."
            } else {
                "This meal exceeds your $selectedMeal allocation by ${calories.toInt() - remainingCalories} kcal."
            }


            binding.recommendationValue.text = recommendationText
        }

        /**
         * Updates the progress indicators based on the selected meal.
         *
         * @param selectedMeal The meal selected by the user.
         */
        private fun updateProgressIndicators(selectedMeal: String) {
            if (allocations == null) {
                Log.e("DetailedInfoFragment", "Allocations not fetched yet")
                return
            }

            val context = requireContext()

            // Nutrient values from result
            val calories = nutrientEstimationResult.totalCalories
            val carbs = nutrientEstimationResult.carbohydrateTotal
            val proteins = nutrientEstimationResult.protein
            val fats = nutrientEstimationResult.totalFat

            // ===== CALORIES =====
            val allocatedCalories = getAllocatedNutrient(selectedMeal, "calories").toFloat()
            val caloriesProgress = if (allocatedCalories > 0) ((calories / allocatedCalories) * 100).toInt().coerceAtMost(100) else 0
            val caloriesRemaining = allocatedCalories - (consumedNutrientsMap[selectedMeal]?.calories ?: 0)
            val colorCalories = if (calories > caloriesRemaining)
                ContextCompat.getColor(context, R.color.red_600)
            else
                ContextCompat.getColor(context, R.color.green_500)

            binding.circularProgressIndicatorCalories.apply {
                setIndicatorColor(colorCalories)
                setProgressCompat(caloriesProgress, true)
            }
            binding.centerTextCalories.text = String.format(Locale.getDefault(), "%.2f", calories)

            // ===== CARBS =====
            val allocatedCarbs = getAllocatedNutrient(selectedMeal, "carbs").toFloat()
            val carbsProgress = if (allocatedCarbs > 0) ((carbs / allocatedCarbs) * 100).toInt().coerceAtMost(100) else 0
            val carbsRemaining = allocatedCarbs - (consumedNutrientsMap[selectedMeal]?.carbs ?: 0)
            val colorCarbs = if (carbs > carbsRemaining)
                ContextCompat.getColor(context, R.color.red_600)
            else
                ContextCompat.getColor(context, R.color.blue_700)

            binding.circularProgressIndicatorCarbs.apply {
                setIndicatorColor(colorCarbs)
                setProgressCompat(carbsProgress, true)
            }
            binding.centerTextCarbs.text = String.format(Locale.getDefault(), "%.2f g", carbs)

            // ===== PROTEINS =====
            val allocatedProteins = getAllocatedNutrient(selectedMeal, "proteins").toFloat()
            val proteinsProgress = if (allocatedProteins > 0) ((proteins / allocatedProteins) * 100).toInt().coerceAtMost(100) else 0
            val proteinsRemaining = allocatedProteins - (consumedNutrientsMap[selectedMeal]?.proteins ?: 0)
            val colorProteins = if (proteins > proteinsRemaining)
                ContextCompat.getColor(context, R.color.red_600)
            else
                ContextCompat.getColor(context, R.color.pink_700)

            binding.circularProgressIndicatorProteins.apply {
                setIndicatorColor(colorProteins)
                setProgressCompat(proteinsProgress, true)
            }
            binding.centerTextProteins.text = String.format(Locale.getDefault(), "%.2f g", proteins)

            // ===== FATS =====
            val allocatedFats = getAllocatedNutrient(selectedMeal, "fats").toFloat()
            val fatsProgress = if (allocatedFats > 0) ((fats / allocatedFats) * 100).toInt().coerceAtMost(100) else 0
            val fatsRemaining = allocatedFats - (consumedNutrientsMap[selectedMeal]?.fats ?: 0)
            val colorFats = if (fats > fatsRemaining)
                ContextCompat.getColor(context, R.color.red_600)
            else
                ContextCompat.getColor(context, R.color.yellow_700)

            binding.circularProgressIndicatorYellow.apply {
                setIndicatorColor(colorFats)
                setProgressCompat(fatsProgress, true)
            }
            binding.centerTextFats.text = String.format(Locale.getDefault(), "%.2f g", fats)

            Log.d("DetailedInfoFragment", "Progress bars updated + animated with conditional color ✅")
        }


        /**
         * Helper function to get allocated nutrient based on meal type and nutrient name.
         *
         * @param meal The selected meal.
         * @param nutrient The nutrient name ("calories", "carbs", "proteins", "fats").
         * @return Allocated nutrient value.
         */
        private fun getAllocatedNutrient(meal: String, nutrient: String): Int {
            return when (nutrient) {
                "calories" -> when (meal) {
                    "Breakfast" -> allocations!!.breakfastCalories
                    "Lunch" -> allocations!!.lunchCalories
                    "Dinner" -> allocations!!.dinnerCalories
                    "Snack" -> allocations!!.snackCalories
                    else -> 0
                }
                "carbs" -> when (meal) {
                    "Breakfast" -> allocations!!.breakfastCarbs
                    "Lunch" -> allocations!!.lunchCarbs
                    "Dinner" -> allocations!!.dinnerCarbs
                    "Snack" -> allocations!!.snackCarbs
                    else -> 0
                }
                "proteins" -> when (meal) {
                    "Breakfast" -> allocations!!.breakfastProteins
                    "Lunch" -> allocations!!.lunchProteins
                    "Dinner" -> allocations!!.dinnerProteins
                    "Snack" -> allocations!!.snackProteins
                    else -> 0
                }
                "fats" -> when (meal) {
                    "Breakfast" -> allocations!!.breakfastFats
                    "Lunch" -> allocations!!.lunchFats
                    "Dinner" -> allocations!!.dinnerFats
                    "Snack" -> allocations!!.snackFats
                    else -> 0
                }
                else -> 0
            }
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

        private fun parseNumber(value: Any?): Int {
            return when (value) {
                is Number -> value.toInt()
                is String -> value.toFloatOrNull()?.toInt() ?: 0
                else -> 0
            }
        }

        private fun formatFoodName(rawName: String): String {
            return rawName
                .split("_") // split by underscore
                .joinToString(" ") { word ->
                    word.lowercase().replaceFirstChar { it.uppercaseChar() }
                }
        }


        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
    }
