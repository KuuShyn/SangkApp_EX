package com.thesis.sangkapp_ex.ui.recipe

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

class RecipeAnalyzerFragment : Fragment(), RecipeHistoryFragment.OnRecipeSelectedListener, RecipeAddNewFragment.OnRecipeAddedListener {

    private lateinit var toggleGroup: MaterialButtonToggleGroup
    private lateinit var addNewButton: MaterialButton
    private lateinit var historyButton: MaterialButton
    private lateinit var fragmentContainer: FrameLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recipe_analyzer, container, false)

        // Initialize the toggle group and buttons
        toggleGroup = view.findViewById(R.id.toggleGroup)
        addNewButton = view.findViewById(R.id.addNewButton)
        historyButton = view.findViewById(R.id.historyButton)
        fragmentContainer = view.findViewById(R.id.fragmentContainer)

        // Set default fragment to History
        if (savedInstanceState == null) {
            replaceChildFragment(RecipeHistoryFragment())
        }

        // Handle button checked events
        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.addNewButton -> {
                        replaceChildFragment(RecipeAddNewFragment())
                        updateButtonStyles(addNewButton, historyButton)
                    }
                    R.id.historyButton -> {
                        replaceChildFragment(RecipeHistoryFragment())
                        updateButtonStyles(historyButton, addNewButton)
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

    override fun onRecipeSelected(recipe: Recipe) {
        val action = RecipeAnalyzerFragmentDirections.actionNavRecipeToRecipeInfoFragment(recipe)
        findNavController().navigate(action)
    }

    override fun onRecipeAdded(recipe: Recipe) {
        val action = RecipeAnalyzerFragmentDirections.actionNavRecipeToRecipeInfoFragment(recipe)
        findNavController().navigate(action)
    }


}
