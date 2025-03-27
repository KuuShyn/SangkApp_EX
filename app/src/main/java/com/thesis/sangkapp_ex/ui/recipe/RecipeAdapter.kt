package com.thesis.sangkapp_ex.ui.recipe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thesis.sangkapp_ex.R

class RecipeAdapter(
    private val recipeList: MutableList<Recipe>,
    private val onItemClick: (Recipe) -> Unit,
    private val onDeleteClick: (Recipe) -> Unit = {} // <-- default empty lambda
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.recipeName)
        private val quantityTextView: TextView = itemView.findViewById(R.id.recipeQuantity)
        private val caloriesTextView: TextView = itemView.findViewById(R.id.recipeCalories)
        private val deleteButton: View = itemView.findViewById(R.id.deleteFoodButton)

        fun bind(recipe: Recipe) {
            nameTextView.text = recipe.name
            "${recipe.calories} kcal".also { caloriesTextView.text = it }
            "${recipe.servings} g".also { quantityTextView.text = it }

            itemView.setOnClickListener { onItemClick(recipe) }

            deleteButton.setOnClickListener {
                onDeleteClick(recipe)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipeList[position])
    }

    override fun getItemCount(): Int = recipeList.size
}
