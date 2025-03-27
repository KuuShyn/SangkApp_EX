// File: RecipeHistoryFragment.kt
package com.thesis.sangkapp_ex.ui.recipe

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.thesis.sangkapp_ex.R

class RecipeHistoryFragment : Fragment() {

    interface OnRecipeSelectedListener {
        fun onRecipeSelected(recipe: Recipe)
    }

    private var listener: OnRecipeSelectedListener? = null

    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var recyclerView: RecyclerView

    private val firestore = FirebaseFirestore.getInstance()
    private var recipesList = mutableListOf<Recipe>()

    private var firestoreListener: ListenerRegistration? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? OnRecipeSelectedListener
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
        val view = inflater.inflate(R.layout.fragment_recipe_history, container, false)
        recyclerView = view.findViewById(R.id.recipeRecyclerView)

        // Initialize RecyclerView
        recipeAdapter = RecipeAdapter(
            recipesList,
            onItemClick = { selectedRecipe ->
                listener?.onRecipeSelected(selectedRecipe)
            },
            onDeleteClick = { recipe ->
                // Show confirmation dialog before deletion
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Delete Recipe")
                    .setMessage("Are you sure you want to delete \"${recipe.name}\"?")
                    .setPositiveButton("Delete") { _, _ ->
                        deleteRecipeFromFirestore(recipe)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

        )


        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = recipeAdapter

        // Fetch recipes from Firestore
        fetchRecipesFromFirestore()

        return view
    }

    private fun fetchRecipesFromFirestore() {
        val userId = context?.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            ?.getString("USER_ID", null)

        if (userId == null) {
            Log.e("RecipeHistory", "USER_ID not found in SharedPreferences ❌")
            return
        }


        // Listen for real-time updates
        firestoreListener = firestore.collection("users")
            .document(userId)
            .collection("recipes")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("RecipeHistory", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    recipesList.clear()
                    for (doc in snapshots) {
                        val recipe = doc.toObject(Recipe::class.java)
                        recipesList.add(recipe)
                    }
                    recipeAdapter.notifyDataSetChanged()
                    Log.d("RecipeHistory", "Fetched ${recipesList.size} recipes ✅")
                } else {
                    Log.d("RecipeHistory", "No recipes found 🍽️")
                    Toast.makeText(requireContext(), "No recipes found", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun deleteRecipeFromFirestore(recipe: Recipe) {
        val userId = context?.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            ?.getString("USER_ID", null) ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("recipes")
            .whereEqualTo("name", recipe.name)
            .whereEqualTo("calories", recipe.calories)
            .whereEqualTo("servings", recipe.servings)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    snapshot.documents[0].reference.delete()
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Recipe deleted ✅", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to delete ❌", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }

}
