package com.thesis.sangkapp_ex.ui.foodLog

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.thesis.sangkapp_ex.R
import com.thesis.sangkapp_ex.databinding.FragmentLogFeaturedInfoBinding
import com.thesis.sangkapp_ex.ui.photo.DetailedInfoFragment
import com.thesis.sangkapp_ex.ui.recipe.Nutrients
import com.thesis.sangkapp_ex.ui.recipe.Recipe
import java.util.Date
import java.util.Locale


class LogMyRecipeInfoFragment : Fragment() {

    private var _binding: FragmentLogFeaturedInfoBinding? = null
    private val binding get() = _binding!!

    private var isNutritionFactsVisible = false
    private lateinit var recipe: Recipe
    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private var mealType = ""
    private var allocations: Allocations? = null
    private var consumedNutrientsMap: MutableMap<String, Nutrients> = mutableMapOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args: LogFeaturedInfoFragmentArgs by navArgs()
        val foodName = args.foodName.name
        mealType = args.mealType.toString()

        // Load from Firestore instead of JSON
        fetchRecipeFromFirestore(foodName)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogFeaturedInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDropdownMenu()
        fetchAllocationsAndConsumedCalories()

//        setupRecipeInfo()
        updateButtonAndIcon()

        binding.showHidebutton.setOnClickListener {
            toggleNutritionFactsVisibility()
        }

        binding.addLogButton.setOnClickListener {
            addLogToFirestore()
        }

    }


    private fun setupDropdownMenu() {
        val mealOptions = listOf("Breakfast", "Lunch", "Dinner", "Snack")
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_menu_item, mealOptions)
        binding.dropdownMenu.setAdapter(adapter)
        binding.dropdownMenu.setText("Breakfast", false)
        binding.dropdownMenu.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            val selectedMeal = parent.getItemAtPosition(position) as String
            mealType = selectedMeal
            updateProgressIndicators(mealType)

        }
    }

    private fun fetchAllocationsAndConsumedCalories() {
        if (currentUserId == null) return

        db.collection("users").document(currentUserId)
            .collection("profile").document(currentUserId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    allocations = Allocations(
                        breakfastCalories = doc.getLong("breakfastCalories")?.toInt() ?: 0,
                        lunchCalories = doc.getLong("lunchCalories")?.toInt() ?: 0,
                        dinnerCalories = doc.getLong("dinnerCalories")?.toInt() ?: 0,
                        snackCalories = doc.getLong("snackCalories")?.toInt() ?: 0,
                        breakfastCarbs = doc.getLong("breakfastCarbs")?.toInt() ?: 0,
                        lunchCarbs = doc.getLong("lunchCarbs")?.toInt() ?: 0,
                        dinnerCarbs = doc.getLong("dinnerCarbs")?.toInt() ?: 0,
                        snackCarbs = doc.getLong("snackCarbs")?.toInt() ?: 0,
                        breakfastProteins = doc.getLong("breakfastProteins")?.toInt() ?: 0,
                        lunchProteins = doc.getLong("lunchProteins")?.toInt() ?: 0,
                        dinnerProteins = doc.getLong("dinnerProteins")?.toInt() ?: 0,
                        snackProteins = doc.getLong("snackProteins")?.toInt() ?: 0,
                        breakfastFats = doc.getLong("breakfastFats")?.toInt() ?: 0,
                        lunchFats = doc.getLong("lunchFats")?.toInt() ?: 0,
                        dinnerFats = doc.getLong("dinnerFats")?.toInt() ?: 0,
                        snackFats = doc.getLong("snackFats")?.toInt() ?: 0,
                        totalCalories = doc.getLong("calories")?.toInt() ?: 0
                    )
                    fetchConsumedCalories()
                }
            }
    }

    private fun fetchConsumedCalories() {
        if (currentUserId == null || allocations == null) return

        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        consumedNutrientsMap = mutableMapOf(
            "Breakfast" to Nutrients(),
            "Lunch" to Nutrients(),
            "Dinner" to Nutrients(),
            "Snack" to Nutrients()
        )

        db.collection("users").document(currentUserId)
            .collection("foodEntries")
            .whereEqualTo("date", date)
            .get()
            .addOnSuccessListener { docs ->
                for (doc in docs) {
                    val meal = doc.getString("mealType") ?: continue
                    val nutrients = Nutrients(
                        calories = extractGrams(doc.get("calories")?.toString()!!).toInt(),
                        carbs = extractGrams(doc.get("carbs")?.toString()!!).toInt(),
                        proteins = extractGrams(doc.get("proteins")?.toString()!!).toInt(),
                        fats = extractGrams(doc.get("fats")?.toString()!!).toInt()
                    )

                    val existing = consumedNutrientsMap[meal] ?: Nutrients()
                    consumedNutrientsMap[meal] = Nutrients(
                        calories = existing.calories + nutrients.calories,
                        carbs = existing.carbs + nutrients.carbs,
                        proteins = existing.proteins + nutrients.proteins,
                        fats = existing.fats + nutrients.fats
                    )
                }

                if (mealType.isNotEmpty()) {
                    updateProgressIndicators(mealType)
                }
            }
    }

    private fun updateProgressIndicators(selectedMeal: String) {
        if (allocations == null) return
        val multiplier = getServingMultiplier()

        val context = requireContext()

        // === CALORIES ===
        val addedCalories = recipe.calories.toDouble() * multiplier
        val allocatedCalories = getAllocatedNutrient(selectedMeal, "calories")
        val caloriesProgress = ((addedCalories / allocatedCalories) * 100).coerceAtMost(100.0).toInt()

        val caloriesColor = if (addedCalories > allocatedCalories)
            ContextCompat.getColor(context, R.color.red_600)
        else
            ContextCompat.getColor(context, R.color.green_500)

        binding.circularProgressIndicatorCalories.apply {
            setIndicatorColor(caloriesColor)
            setProgressCompat(caloriesProgress, true)
        }
        binding.centerTextCalories.text = "${addedCalories.toInt()}"

        // === CARBS ===
        val addedCarbs = extractGrams(recipe.nutrients.carbohydrates) * multiplier
        val allocatedCarbs = getAllocatedNutrient(selectedMeal, "carbs")
        val carbsProgress = ((addedCarbs / allocatedCarbs) * 100).coerceAtMost(100.0).toInt()

        val carbsColor = if (addedCarbs > allocatedCarbs)
            ContextCompat.getColor(context, R.color.red_600)
        else
            ContextCompat.getColor(context, R.color.blue_700)

        binding.circularProgressIndicatorCarbs.apply {
            setIndicatorColor(carbsColor)
            setProgressCompat(carbsProgress, true)
        }
        binding.centerTextCarbs.text = String.format("%.2f g", addedCarbs)

        // === PROTEINS ===
        val addedProteins = extractGrams(recipe.nutrients.proteins) * multiplier
        val allocatedProteins = getAllocatedNutrient(selectedMeal, "proteins")
        val proteinsProgress = ((addedProteins / allocatedProteins) * 100).coerceAtMost(100.0).toInt()

        val proteinsColor = if (addedProteins > allocatedProteins)
            ContextCompat.getColor(context, R.color.red_600)
        else
            ContextCompat.getColor(context, R.color.pink_700)

        binding.circularProgressIndicatorProteins.apply {
            setIndicatorColor(proteinsColor)
            setProgressCompat(proteinsProgress, true)
        }
        binding.centerTextProteins.text = String.format("%.2f g", addedProteins)

        // === FATS ===
        val addedFats = extractGrams(recipe.nutrients.fats) * multiplier
        val allocatedFats = getAllocatedNutrient(selectedMeal, "fats")
        val fatsProgress = ((addedFats / allocatedFats) * 100).coerceAtMost(100.0).toInt()

        val fatsColor = if (addedFats > allocatedFats)
            ContextCompat.getColor(context, R.color.red_600)
        else
            ContextCompat.getColor(context, R.color.yellow_700)

        binding.circularProgressIndicatorFats.apply {
            setIndicatorColor(fatsColor)
            setProgressCompat(fatsProgress, true)
        }
        binding.centerTextFats.text = String.format("%.2f g", addedFats)

        // Debug Log (Optional)
        // Log.d("LogFeaturedInfo", "Progress bars updated for $selectedMeal 🍽️")
    }



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

    private fun setupRecipeInfo() {
        val multiplier = getServingMultiplier()

        binding.dishName.text = recipe.name
        binding.caloriesValue.text = String.format("%.2f kcal", recipe.calories.toDouble() * multiplier)
        binding.carbsValue.text = formatGrams(recipe.nutrients.carbohydrates, multiplier)
        binding.proteinsValue.text = formatGrams(recipe.nutrients.proteins, multiplier)
        binding.fatsValue.text = formatGrams(recipe.nutrients.fats, multiplier)
        binding.fibersValue.text = formatGrams(recipe.nutrients.fibers, multiplier)
        binding.sugarsValue.text = formatGrams(recipe.nutrients.sugars, multiplier)
        binding.cholesterolValue.text = formatGrams(recipe.nutrients.cholesterol, multiplier)
        binding.sodiumValue.text = formatGrams(recipe.nutrients.sodium, multiplier)
        binding.calciumValue.text = formatGrams(recipe.nutrients.calcium, multiplier)
        binding.ironValue.text = formatGrams(recipe.nutrients.iron, multiplier)
        binding.vitaminABetaKValue.text = formatGrams(recipe.nutrients.vitaminABetaK, multiplier)
        binding.vitaminARetinolValue.text = formatGrams(recipe.nutrients.vitaminARetinol, multiplier)
        binding.vitaminB1Value.text = formatGrams(recipe.nutrients.vitaminB1, multiplier)
        binding.vitaminB2Value.text = formatGrams(recipe.nutrients.vitaminB2, multiplier)
        binding.vitaminB3Value.text = formatGrams(recipe.nutrients.vitaminB3, multiplier)
        binding.vitaminC5Value.text = formatGrams(recipe.nutrients.vitaminC5, multiplier)
    }

    private fun formatGrams(value: String, multiplier: Double): String {
        val baseValue = extractGrams(value)
        return String.format("%.2f g", baseValue * multiplier)
    }



    private fun toggleNutritionFactsVisibility() {
        isNutritionFactsVisible = !isNutritionFactsVisible
        binding.nutritionFactsCard.isVisible = isNutritionFactsVisible
        updateButtonAndIcon()
    }

    private fun extractGrams(value: String): Double {
        return value.replace("[^\\d.]".toRegex(), "").toDoubleOrNull() ?: 0.0
    }


    private fun updateButtonAndIcon() {
        if (isNutritionFactsVisible) {
            "Hide".also { binding.showHidebutton.text = it }
            binding.showHidebutton.setIconResource(R.drawable.baseline_keyboard_arrow_up_24)
        } else {
            "Show".also { binding.showHidebutton.text = it }
            binding.showHidebutton.setIconResource(R.drawable.baseline_keyboard_arrow_down_24)
        }
    }

    private fun getServingMultiplier(): Double {
        val input = binding.servingSizeInput.text.toString()
        val inputGrams = input.toDoubleOrNull()?.takeIf { it > 0 } ?: recipe.servings.toDouble()
        return inputGrams / recipe.servings
    }


    private fun fetchRecipeFromFirestore(foodName: String) {
        if (currentUserId == null) return

        db.collection("users").document(currentUserId)
            .collection("recipes")
            .whereEqualTo("name", foodName)
            .limit(1)
            .get()
            .addOnSuccessListener { docs ->
                if (!docs.isEmpty) {
                    val doc = docs.documents.first()
                    val servings = doc.getLong("servings")?.toInt() ?: 100

                    val nutrientsMap = doc.get("nutrients") as? Map<*, *> ?: emptyMap<String, String>()

                    val nutrients = Nutrients(
                        carbohydrates = nutrientsMap["carbohydrates"]?.toString() ?: "-",
                        proteins = nutrientsMap["proteins"]?.toString() ?: "-",
                        fats = nutrientsMap["fats"]?.toString() ?: "-",
                        fibers = nutrientsMap["fibers"]?.toString() ?: "-",
                        sugars = nutrientsMap["sugars"]?.toString() ?: "-",
                        cholesterol = nutrientsMap["cholesterol"]?.toString() ?: "-",
                        sodium = nutrientsMap["sodium"]?.toString() ?: "-",
                        calcium = nutrientsMap["calcium"]?.toString() ?: "-",
                        iron = nutrientsMap["iron"]?.toString() ?: "-",
                        vitaminABetaK = nutrientsMap["vitaminABetaK"]?.toString() ?: "-",
                        vitaminARetinol = nutrientsMap["vitaminARetinol"]?.toString() ?: "-",
                        vitaminB1 = nutrientsMap["vitaminB1"]?.toString() ?: "-",
                        vitaminB2 = nutrientsMap["vitaminB2"]?.toString() ?: "-",
                        vitaminB3 = nutrientsMap["vitaminB3"]?.toString() ?: "-",
                        vitaminC5 = nutrientsMap["vitaminC5"]?.toString() ?: "-"
                    )

                    val calories = doc.getDouble("calories") ?: 0.0
                    val imageResId = R.drawable.sangkapp_placeholder

                    recipe = Recipe(
                        name = foodName,
                        servings = servings,
                        calories = calories,
                        imageResId = imageResId,
                        nutrients = nutrients
                    )

// 🟢 Move these here, AFTER recipe is initialized
                    binding.addLogButton.isEnabled = getServingMultiplier() > 0
                    setupRecipeInfo()

                    if (mealType.isNotBlank()) {
                        binding.dropdownMenu.setText(mealType, false)
                        updateProgressIndicators(mealType)
                    }



                    // Now that recipe is loaded, setup UI
                    setupRecipeInfo()
                    updateProgressIndicators(mealType)

                } else {
                    Toast.makeText(requireContext(), "Recipe not found ❌", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load recipe: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }




    private fun addLogToFirestore() {
        val multiplier = getServingMultiplier()

        val logEntry = hashMapOf(
            "calories" to (recipe.calories * multiplier),
            "carbs" to (extractGrams(recipe.nutrients.carbohydrates) * multiplier),
            "fats" to (extractGrams(recipe.nutrients.fats) * multiplier),
            "proteins" to (extractGrams(recipe.nutrients.proteins) * multiplier),
            "date" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            "foodName" to recipe.name,
            "foodQuantity" to binding.servingSizeInput.text.toString().ifEmpty { "1 serving" },
            "mealType" to binding.dropdownMenu.text.toString(),
            "timestamp" to System.currentTimeMillis()
        )
        if (currentUserId != null) {
            db.collection("users")
                .document(currentUserId)
                .collection("foodEntries")
                .add(logEntry)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Log added ✅", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to add log: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
