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

    interface OnRecipeSelectedListener {
        fun onRecipeSelected(recipe: Recipe)
    }

    private var listener: OnRecipeSelectedListener? = null

    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var recyclerView: RecyclerView

    private val firestore = FirebaseFirestore.getInstance()
    private var firestoreListener: ListenerRegistration? = null
    private val recipesList = mutableListOf<Recipe>()

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
        val view = inflater.inflate(R.layout.fragment_log_my_recipe, container, false)
        recyclerView = view.findViewById(R.id.recipeRecyclerView)

        recipeAdapter = RecipeAdapter(recipesList) { selectedRecipe ->
            listener?.onRecipeSelected(selectedRecipe)
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = recipeAdapter

        fetchRecipesFromFirestore()

        return view
    }

    private fun fetchRecipesFromFirestore() {
        val userId = context
            ?.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            ?.getString("USER_ID", null)

        if (userId == null) {
            Log.w("LogMyRecipeFragment", "User ID not found in SharedPreferences ❌")
            return
        }

        firestoreListener = firestore
            .collection("users")
            .document(userId)
            .collection("recipes")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("LogMyRecipeFragment", "Listen failed ❌", e)
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
                    Log.d("LogMyRecipeFragment", "No recipe data found")
                }
            }
    }
}
