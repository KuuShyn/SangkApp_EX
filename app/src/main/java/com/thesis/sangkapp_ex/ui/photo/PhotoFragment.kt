package com.thesis.sangkapp_ex.ui.photo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.thesis.sangkapp_ex.BoundingBox
import com.thesis.sangkapp_ex.Constants.LABELS_PATH
import com.thesis.sangkapp_ex.Constants.MODEL_PATH
import com.thesis.sangkapp_ex.DepthAnything
import com.thesis.sangkapp_ex.DepthExtractor
import com.thesis.sangkapp_ex.Detector
import com.thesis.sangkapp_ex.FoodDensity
import com.thesis.sangkapp_ex.FoodNutrients
import com.thesis.sangkapp_ex.JsonUtils
import com.thesis.sangkapp_ex.databinding.FragmentPhotoBinding
import com.thesis.sangkapp_ex.ui.OverlayView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PhotoFragment : Fragment(), Detector.DetectorListener {

    private var _binding: FragmentPhotoBinding? = null
    private val binding get() = _binding!!
    private val args: PhotoFragmentArgs by navArgs()
    private lateinit var depthAnything: DepthAnything
    private val photoViewModel: PhotoViewModel by activityViewModels() // Shared ViewModel
    private var detector: Detector? = null
    private var boundingBoxes = listOf<BoundingBox>() // Store detected bounding boxes
    private lateinit var foodNutrientsList: List<FoodNutrients>
    private lateinit var foodDensityList: List<FoodDensity>
    private lateinit var foodNutrientsMap: Map<String, FoodNutrients>
    private lateinit var foodDensityMap: Map<String, FoodDensity>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadFoodData()

        binding.depthMapView.visibility = View.GONE

        // Set BoundingBoxClickListener for OverlayView
        binding.overlay.boundingBoxClickListener = object : OverlayView.BoundingBoxClickListener {
            override fun onBoundingBoxClicked(boundingBox: BoundingBox) {
                Toast.makeText(requireContext(), "Clicked on: ${boundingBox.clsName}", Toast.LENGTH_SHORT).show()
                // Handle the click event (e.g., navigate to a detail view, highlight the box, etc.)
                Log.d("PhotoFragment", "Clicked on: ${boundingBox.clsName}")
            }
        }

        photoViewModel.cameraParams.observe(viewLifecycleOwner) { params ->
            if (params != null) {
                Log.d(
                    "PhotoFragment",
                    "Received Camera Params: Focal Length=${params.focalLength}, Sensor Width=${params.sensorWidth}, Sensor Height=${params.sensorHeight}"
                )
                val imageUri = args.imageUri
                val bitmap = getFixedBitmap(imageUri)

                detector = Detector(requireContext(), MODEL_PATH, LABELS_PATH, this@PhotoFragment) {
                    toast(it)
                }
                depthAnything = DepthAnything(requireContext(), "fused_model_uint8_512.onnx")

                binding.imageView.setImageBitmap(bitmap)

                detector?.detect(bitmap)
            } else {
                Log.d("PhotoFragment", "Camera Params not available yet")
            }
        }
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
        if (view != null && isAdded) {
            requireActivity().runOnUiThread {
                this.boundingBoxes = boundingBoxes
                binding.overlay.apply {
                    setResults(boundingBoxes)
                    invalidate()
                }

                photoViewModel.cameraParams.value?.let { params ->
                    startDepthPredictionOnCroppedImages(
                        params.focalLength,
                        params.sensorWidth,
                        params.sensorHeight
                    )
                }
            }
        }
    }

    private fun loadFoodData() {
        foodNutrientsList = JsonUtils.loadFoodNutrients(requireContext())
        foodNutrientsMap = foodNutrientsList.associateBy { it.foodId }

        foodDensityList = JsonUtils.loadFoodDensity(requireContext())
        foodDensityMap = foodDensityList.associateBy { it.foodId }

        Log.d("PhotoFragment", "Loaded ${foodNutrientsList.size} food nutrients entries.")
        Log.d("PhotoFragment", "Loaded ${foodDensityList.size} food density entries.")
    }

    // Inside PhotoFragment.kt

    private fun startDepthPredictionOnCroppedImages(
        focalLength: Float,
        sensorWidth: Float,
        sensorHeight: Float
    ) {
        showLoadingScreen()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val originalBitmap = binding.imageView.drawable.toBitmap()
                var totalCalories = 0f
                var totalCarbs = 0f
                var totalProteins = 0f
                var totalFats = 0f

                var detectedFoodCount = 0

                for (box in boundingBoxes) {
                    val croppedBitmap = cropBitmapForBoundingBox(originalBitmap, box)

                    val (depthMap, _) = depthAnything.predict(croppedBitmap)
                    val normalizedDepth = DepthExtractor.extractNormalizedDepth(depthMap)

                    val actualDepth = DepthExtractor.mapNormalizedDepth(
                        normalizedDepth,
                        0.5f,
                        10.0f
                    ) // Example range in meters

                    val pixelWidth = (box.x2 - box.x1) * originalBitmap.width
                    val pixelHeight = (box.y2 - box.y1) * originalBitmap.height

                    val realWorldWidth = convertPixelToRealWorldWidth(
                        pixelWidth,
                        originalBitmap.width,
                        focalLength,
                        sensorWidth
                    )
                    val realWorldHeight = convertPixelToRealWorldHeight(
                        pixelHeight,
                        originalBitmap.height,
                        focalLength,
                        sensorHeight
                    )

                    val averageDepthInMeters = calculateAverageDepthFromMap(actualDepth)

                    val detectedFood = capitalizeWords(box.clsName)

                    val nutrientEstimationResult = estimatePortionSizeAndNutrients(
                        realWorldWidth,
                        realWorldHeight,
                        averageDepthInMeters,
                        detectedFood
                    )

                    if (nutrientEstimationResult != null) {
                        // Accumulate the total values for calories, carbs, proteins, and fats
                        totalCalories += nutrientEstimationResult.totalCalories
                        totalCarbs += nutrientEstimationResult.carbohydrateTotal
                        totalProteins += nutrientEstimationResult.protein
                        totalFats += nutrientEstimationResult.totalFat

                        detectedFoodCount++
                    }
                }

                // Once the loop is done, pass these aggregated values to NutritionBottomSheet
                if (detectedFoodCount > 0) {
                    withContext(Dispatchers.Main) {
                        showNutritionBottomSheet(
                            detectedFoodCount,
                            totalCalories.toInt(),
                            totalCalories,
                            totalCarbs,
                            totalProteins,
                            totalFats
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("PhotoFragment", "Error during depth prediction on cropped images", e)
            } finally {
                withContext(Dispatchers.Main) {
                    hideLoadingScreen()
                }
            }
        }
    }

    // Function to display the NutritionBottomSheet with calculated nutrients
    private fun showNutritionBottomSheet(
        dishesDetected: Int,
        combinedCalories: Int,
        calories: Float,
        carbs: Float,
        proteins: Float,
        fats: Float
    ) {
        val bottomSheet = NutritionBottomSheet.newInstance(
            dishesDetected,
            combinedCalories,
            calories,
            carbs,
            proteins,
            fats
        )
        bottomSheet.show(parentFragmentManager, "NutritionBottomSheet")
    }


    private fun convertPixelToRealWorldWidth(
        pixelWidth: Float,
        imageWidth: Int,
        focalLength: Float,
        sensorWidth: Float,
        scalingFactor: Float = 3f // Adjust this factor based on testing
    ): Float {
        val f_x = (imageWidth * focalLength) / sensorWidth
        val realWorldWidthInMeters = (pixelWidth * sensorWidth) / (f_x * 1000f) * scalingFactor // Apply scaling factor
        return realWorldWidthInMeters
    }

    private fun convertPixelToRealWorldHeight(
        pixelHeight: Float,
        imageHeight: Int,
        focalLength: Float,
        sensorHeight: Float,
        scalingFactor: Float = 3f // Adjust this factor based on testing
    ): Float {
        val f_y = (imageHeight * focalLength) / sensorHeight
        val realWorldHeightInMeters = (pixelHeight * sensorHeight) / (f_y * 1000f) * scalingFactor // Apply scaling factor
        return realWorldHeightInMeters
    }


    private fun calculateAverageDepthFromMap(actualDepth: Array<FloatArray>): Float {
        var totalDepth = 0f
        var pixelCount = 0

        for (row in actualDepth) {
            for (depth in row) {
                totalDepth += depth
                pixelCount++
            }
        }

        return if (pixelCount > 0) totalDepth / pixelCount else 0f
    }

    // In PhotoFragment.kt
    private fun estimatePortionSizeAndNutrients(
        realWorldWidth: Float,
        realWorldHeight: Float,
        averageDepth: Float,
        detectedFood: String
    ): NutrientEstimationResult? {
        val realVolume = realWorldWidth * realWorldHeight * averageDepth
        val volumeInCubicCentimeters = realVolume * 1_000_000 // Convert to cubic centimeters (cm³)

        val foodNutrients = foodNutrientsMap.values.find {
            it.foodNameAndDescription == capitalizeWords(detectedFood)
        }

        if (foodNutrients == null) {
            Log.e("PhotoFragment", "Food item '$detectedFood' not found in the nutrients database.")
            return null
        }

        val foodDensity = foodDensityMap[foodNutrients.foodId]

        if (foodDensity?.densityGml == null) {
            Log.e(
                "PhotoFragment",
                "Density data for food item '${foodNutrients.foodNameAndDescription}' not found."
            )
            return null
        }

        val density = foodDensity.densityGml
        val portionWeight = volumeInCubicCentimeters * density

        // Calculate nutrients per portion
        val energyPer100g = foodNutrients.energyCalculated ?: 0f
        val totalCalories = (portionWeight / 100) * energyPer100g

        val protein = (portionWeight / 100) * (foodNutrients.protein ?: 0f)
        val totalFat = (portionWeight / 100) * (foodNutrients.totalFat ?: 0f)
        val carbohydrateTotal = (portionWeight / 100) * (foodNutrients.carbohydrateTotal ?: 0f)
        val fiberTotalDietary = (portionWeight / 100) * (foodNutrients.fiberTotalDietary ?: 0f)
        val sugarsTotal = (portionWeight / 100) * (foodNutrients.sugarsTotal ?: 0f)
        val calciumCa = (portionWeight / 100) * (foodNutrients.calciumCa ?: 0f)
        val phosphorusP = (portionWeight / 100) * (foodNutrients.phosphorusP ?: 0f)
        val ironFe = (portionWeight / 100) * (foodNutrients.ironFe ?: 0f)
        val sodiumNa = (portionWeight / 100) * (foodNutrients.sodiumNa ?: 0f)
        val vitaminA = (portionWeight / 100) * (foodNutrients.retinolVitaminA ?: 0f)
        val vitaminB1 = (portionWeight / 100) * (foodNutrients.thiaminVitaminB1 ?: 0f)
        val vitaminB2 = (portionWeight / 100) * (foodNutrients.riboflavinVitaminB2 ?: 0f)
        val niacin = (portionWeight / 100) * (foodNutrients.niacin ?: 0f)
        val vitaminC = (portionWeight / 100) * (foodNutrients.ascorbicAcidVitaminC ?: 0f)
        val fattyAcidsSaturatedTotal = (portionWeight / 100) * (foodNutrients.fattyAcidsSaturatedTotal ?: 0f)
        val fattyAcidsMonounsaturatedTotal = (portionWeight / 100) * (foodNutrients.fattyAcidsMonounsaturatedTotal ?: 0f)
        val cholesterol = (portionWeight / 100) * (foodNutrients.cholesterol ?: 0f)

        return NutrientEstimationResult(
            portionWeight = portionWeight,
            totalCalories = totalCalories,
            protein = protein,
            totalFat = totalFat,
            carbohydrateTotal = carbohydrateTotal,
            fiberTotalDietary = fiberTotalDietary,
            sugarsTotal = sugarsTotal,
            calciumCa = calciumCa,
            phosphorusP = phosphorusP,
            ironFe = ironFe,
            sodiumNa = sodiumNa,
            vitaminA = vitaminA,
            vitaminB1 = vitaminB1,
            vitaminB2 = vitaminB2,
            niacin = niacin,
            vitaminC = vitaminC,
            fattyAcidsSaturatedTotal = fattyAcidsSaturatedTotal,
            fattyAcidsMonounsaturatedTotal = fattyAcidsMonounsaturatedTotal,
            cholesterol = cholesterol
        )
    }

    private fun cropBitmapForBoundingBox(bitmap: Bitmap, box: BoundingBox): Bitmap {
        val x1 = (box.x1 * bitmap.width).toInt().coerceIn(0, bitmap.width)
        val y1 = (box.y1 * bitmap.height).toInt().coerceIn(0, bitmap.height)
        val width = ((box.x2 - box.x1) * bitmap.width).toInt().coerceIn(0, bitmap.width - x1)
        val height = ((box.y2 - box.y1) * bitmap.height).toInt().coerceIn(0, bitmap.height - y1)

        return Bitmap.createBitmap(bitmap, x1, y1, width, height)
    }

    private fun getFixedBitmap(imageUri: Uri): Bitmap {
        val inputStream = requireContext().contentResolver.openInputStream(imageUri)
        var imageBitmap = BitmapFactory.decodeStream(inputStream)

        inputStream?.use { stream ->
            val exifInterface = ExifInterface(stream)
            imageBitmap = when (exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(imageBitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(imageBitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(imageBitmap, 270f)
                else -> imageBitmap
            }
        }

        return imageBitmap
    }

    private fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, false)
    }

    private fun capitalizeWords(input: String): String {
        return input.replace('_', ' ').split(' ').joinToString(" ") { word ->
            word.split('-').joinToString("-") { part ->
                part.lowercase().replaceFirstChar { it.uppercase() }
            }
        }
    }
    
    private fun showLoadingScreen() {
        binding.loadingScreen.visibility = View.VISIBLE
        binding.imageView.visibility = View.INVISIBLE
        binding.overlay.visibility = View.INVISIBLE
    }

    // Hide the loading screen
    private fun hideLoadingScreen() {
        binding.loadingScreen.visibility = View.GONE
        binding.imageView.visibility = View.VISIBLE
        binding.overlay.visibility = View.VISIBLE
    }

    override fun onEmptyDetect() {
        Log.d("PhotoFragment", "No objects detected.")
    }

    private fun toast(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
