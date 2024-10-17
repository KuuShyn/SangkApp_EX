package com.thesis.sangkapp_ex.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    // Backing field for food items
    private val _foodItems = MutableLiveData<MutableList<FoodItem>>(mutableListOf())

    // Public LiveData for observing food items
    val foodItems: LiveData<MutableList<FoodItem>> = _foodItems

    // Function to add a food item to the list
    fun addFoodItem(foodName: String, foodQuantity: String, calories: String) {
        val updatedList = _foodItems.value ?: mutableListOf()
        updatedList.add(FoodItem(foodName, foodQuantity, calories))
        _foodItems.value = updatedList
    }
}