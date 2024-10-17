package com.thesis.sangkapp_ex.ui.recipe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thesis.sangkapp_ex.R

class IngredientsAdapter(private val ingredients: List<Ingredient>) : RecyclerView.Adapter<IngredientsAdapter.IngredientViewHolder>() {

    inner class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.ingredientName)
        val quantityTextView: TextView = itemView.findViewById(R.id.ingredientQuantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ingredient_card, parent, false)
        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val ingredient = ingredients[position]
        holder.nameTextView.text = ingredient.name
        holder.quantityTextView.text = ingredient.quantity
    }

    override fun getItemCount() = ingredients.size
}
