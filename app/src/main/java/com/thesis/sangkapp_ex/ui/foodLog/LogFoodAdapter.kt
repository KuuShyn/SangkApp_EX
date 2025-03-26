package com.thesis.sangkapp_ex.ui.foodLog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.thesis.sangkapp_ex.R
import com.thesis.sangkapp_ex.ui.recipe.Recipe
import java.util.Locale

class LogFoodAdapter(
    private val foodList: List<Recipe>, // List of recipes or food items
    private val onItemClick: (Recipe) -> Unit // Callback for handling clicks on food items
) : RecyclerView.Adapter<LogFoodAdapter.FoodViewHolder>() {

    // ViewHolder for each food item
    inner class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val foodImageView: ShapeableImageView = itemView.findViewById(R.id.foodImage)
        private val foodNameTextView: TextView = itemView.findViewById(R.id.foodName)
        private val foodCaloriesTextView: TextView = itemView.findViewById(R.id.foodCalories)

        // Bind data to each view
        fun bind(foodItem: Recipe) {
            foodNameTextView.text = foodItem.name

            val caloriesDouble = foodItem.calories // safely convert Number to Double
            val caloriesValueFormatted = String.format(Locale.getDefault(), "%.0f kcal", caloriesDouble)
            foodCaloriesTextView.text = caloriesValueFormatted
            // Set the image from drawable resources (resource ID stored in Recipe model)
            foodItem.imageResId?.let { foodImageView.setImageResource(it) }

            // Handle click on each item
            itemView.setOnClickListener {
                onItemClick(foodItem)
            }
        }
    }

    // Inflate the layout for each food item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.food_item_card, parent, false)
        return FoodViewHolder(view)
    }

    // Bind the data for each food item
    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val foodItem = foodList[position]
        holder.bind(foodItem)
    }

    // Return the total number of food items
    override fun getItemCount(): Int = foodList.size
}
