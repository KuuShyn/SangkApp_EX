package com.thesis.sangkapp_ex.ui.photo

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.thesis.sangkapp_ex.R
import com.thesis.sangkapp_ex.databinding.FragmentDetailedInfoBinding

// com.thesis.sangkapp_ex.ui.photo.DetailedInfoFragment.kt
class DetailedInfoFragment : Fragment() {

    private var _binding: FragmentDetailedInfoBinding? = null
    private val binding get() = _binding!!

    private val args: DetailedInfoFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailedInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        "$detectedVolume cm³".also { binding.detectedVolumeValue.text = it }
        "$mealPortion grams".also { binding.maximumMealPortionValue.text = it }

        "${nutrientEstimationResult.totalCalories} kcal".also { view.findViewById<TextView>(R.id.caloriesValue).text = it }
        view.findViewById<TextView>(R.id.carbsValue).text = nutrientEstimationResult.carbohydrateTotal.toString()
        view.findViewById<TextView>(R.id.proteinsValue).text = nutrientEstimationResult.protein.toString()
        view.findViewById<TextView>(R.id.fatsValue).text = nutrientEstimationResult.totalFat.toString()
        view.findViewById<TextView>(R.id.fibersValue).text = nutrientEstimationResult.fiberTotalDietary.toString()
        view.findViewById<TextView>(R.id.sugarsValue).text = nutrientEstimationResult.sugarsTotal.toString()
        view.findViewById<TextView>(R.id.cholesterolValue).text = nutrientEstimationResult.cholesterol.toString()
        view.findViewById<TextView>(R.id.sodiumValue).text = nutrientEstimationResult.sodiumNa.toString()
        view.findViewById<TextView>(R.id.calciumValue).text = nutrientEstimationResult.calciumCa.toString()
        view.findViewById<TextView>(R.id.ironValue).text = nutrientEstimationResult.ironFe.toString()
        view.findViewById<TextView>(R.id.vitaminARetinolValue).text = nutrientEstimationResult.vitaminA.toString()
        view.findViewById<TextView>(R.id.vitaminB1Value).text = nutrientEstimationResult.vitaminB1.toString()
        view.findViewById<TextView>(R.id.vitaminB2Value).text = nutrientEstimationResult.vitaminB2.toString()
        view.findViewById<TextView>(R.id.vitaminB3Value).text = nutrientEstimationResult.niacin.toString()
        view.findViewById<TextView>(R.id.vitaminC5Value).text = nutrientEstimationResult.vitaminC.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
