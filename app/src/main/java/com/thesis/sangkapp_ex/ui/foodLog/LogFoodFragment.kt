package com.thesis.sangkapp_ex.ui.foodLog

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.thesis.sangkapp_ex.R
import com.thesis.sangkapp_ex.ui.recipe.Recipe

class LogFoodFragment : Fragment(), LogMyRecipeFragment.OnMyRecipeSelectedListener,
    LogFeaturedFragment.OnLogFeaturedListener {

    private lateinit var toggleGroup: MaterialButtonToggleGroup
    private lateinit var featuredButton: MaterialButton
    private lateinit var myRecipeButton: MaterialButton
    private lateinit var fragmentContainer: FrameLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_log_food, container, false)

        // Initialize the toggle group and buttons
        toggleGroup = view.findViewById(R.id.toggleGroup)
        featuredButton = view.findViewById(R.id.featuredButton)
        myRecipeButton = view.findViewById(R.id.myRecipeButton)
        fragmentContainer = view.findViewById(R.id.fragmentContainer)


        if (savedInstanceState == null) {
            replaceChildFragment(LogFeaturedFragment())
        }

        // Handle button checked events
        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.featuredButton -> {
                        replaceChildFragment(LogFeaturedFragment())
                        updateButtonStyles(featuredButton, myRecipeButton)
                    }

                    R.id.myRecipeButton -> {
                        replaceChildFragment(LogMyRecipeFragment())
                        updateButtonStyles(myRecipeButton, featuredButton)
                    }
                }
            }
        }

        return view
    }

    // Function to replace child fragment in container
    private fun replaceChildFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    // Update button styles based on selection
    private fun updateButtonStyles(activeButton: MaterialButton, inactiveButton: MaterialButton) {
        // Set active button style
        activeButton.setBackgroundColor(resources.getColor(R.color.green_700, null))
        activeButton.setTextColor(Color.WHITE)

        // Set inactive button style
        inactiveButton.setBackgroundColor(resources.getColor(R.color.gray, null))
        inactiveButton.setTextColor(Color.BLACK)
    }

    // Handle recipe selection and navigate to RecipeInfoFragment
    override fun onMyRecipeSelected(recipe: Recipe) {
        // Use SafeArgs to pass the selected recipe to RecipeInfoFragment
        val action = LogFoodFragmentDirections.actionNavLogFoodToRecipeInfoFragment(recipe)
        findNavController().navigate(action)
    }

    override fun onLogFeaturedSelected(recipe: Recipe) {
        val action = LogFoodFragmentDirections.actionNavLogFoodToLogFeaturedInfoFragment(recipe)
        findNavController().navigate(action)
    }


}
