package com.thesis.sangkapp_ex.ui.foodLog

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.thesis.sangkapp_ex.R
import com.thesis.sangkapp_ex.ui.recipe.Recipe
import com.thesis.sangkapp_ex.ui.recipe.RecipeAdapter

class LogMyRecipeFragment : Fragment() {

    interface OnMyRecipeSelectedListener {
        fun onMyRecipeSelected(recipe: Recipe)
    }

    private var listener: OnMyRecipeSelectedListener? = null

    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var recyclerView: RecyclerView

    private val firestore = FirebaseFirestore.getInstance()
    private var recipesList = mutableListOf<Recipe>()

    private var firestoreListener: ListenerRegistration? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? OnMyRecipeSelectedListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        firestoreListener?.remove()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_log_my_recipe, container, false)
        recyclerView = view.findViewById(R.id.recipeRecyclerView)

        // Initialize RecyclerView
        recipeAdapter = RecipeAdapter(recipesList) { selectedRecipe ->
            listener?.onMyRecipeSelected(selectedRecipe)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = recipeAdapter

        // Fetch recipes from Firestore
        fetchRecipesFromFirestore()

        return view
    }

    private fun fetchRecipesFromFirestore() {
        val userId = context?.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            ?.getString("USER_ID", null)
        if (userId != null) {
            firestore.collection("users").document(userId).collection("recipes")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w("LogMyRecipeHistory", "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (snapshots != null) {
                        recipesList.clear()
                        for (doc in snapshots) {
                            val recipe = doc.toObject(Recipe::class.java)
                            recipesList.add(recipe)
                        }
                        recipeAdapter.notifyDataSetChanged()
                    } else {
                        Log.d("LogMyRecipeHistory", "Current data: null")
                    }
                }
        } else {
            Log.w("LogMyRecipeHistory", "User ID is null")
        }
    }
}



