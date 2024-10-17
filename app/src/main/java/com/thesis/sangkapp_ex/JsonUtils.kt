// File: JsonUtils.kt
package com.thesis.sangkapp_ex

import android.content.Context
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.lang.reflect.Type

object JsonUtils {

    // Custom deserializer for Float to handle non-numeric values like "-"
    class SafeFloatDeserializer : JsonDeserializer<Float?> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Float? {
            return try {
                json.asFloat // Parse the value as a float
            } catch (e: NumberFormatException) {
                null // Return null if parsing fails (e.g., for "-")
            } catch (e: IllegalStateException) {
                null // Return null if the value is not a valid float
            }
        }
    }

    // Custom deserializer for Double to handle non-numeric values like "-"
    class SafeDoubleDeserializer : JsonDeserializer<Double?> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Double? {
            return try {
                json.asDouble // Parse the value as a double
            } catch (e: NumberFormatException) {
                null // Return null if parsing fails (e.g., for "-")
            } catch (e: IllegalStateException) {
                null // Return null if the value is not a valid double
            }
        }
    }

    private val gson: Gson = GsonBuilder()
        // Register for boxed Float (java.lang.Float)
        .registerTypeAdapter(java.lang.Float::class.java, SafeFloatDeserializer())
        // Register for boxed Double (java.lang.Double)
        .registerTypeAdapter(java.lang.Double::class.java, SafeDoubleDeserializer())
        .create()

    /**
     * Loads and parses the food nutrients data from JSON.
     *
     * @param context The application context.
     * @param fileName The JSON file name.
     * @return A list of FoodNutrients objects.
     */
    fun loadFoodNutrients(context: Context, fileName: String = "dish_nutrients.json"): List<FoodNutrients> {
        val jsonString = loadJSONFromAsset(context, fileName)
        val listFoodNutrientsType = object : TypeToken<List<FoodNutrients>>() {}.type
        return gson.fromJson(jsonString, listFoodNutrientsType)
    }

    /**
     * Loads and parses the food density data from JSON.
     *
     * @param context The application context.
     * @param fileName The JSON file name.
     * @return A list of FoodDensity objects.
     */
    fun loadFoodDensity(context: Context, fileName: String = "dish_density.json"): List<FoodDensity> {
        val jsonString = loadJSONFromAsset(context, fileName)
        val listFoodDensityType = object : TypeToken<List<FoodDensity>>() {}.type
        return gson.fromJson(jsonString, listFoodDensityType)
    }


    fun loadPhilfctDb(context: Context, fileName: String = "philfct_ingredients.json"): List<FoodNutrients> {
        val jsonString = loadJSONFromAsset(context, fileName)
        val listFoodNutrientsType = object : TypeToken<List<FoodNutrients>>() {}.type
        return gson.fromJson(jsonString, listFoodNutrientsType)
    }

    /**
     * Generic method to load JSON from the assets folder.
     *
     * @param context The application context.
     * @param fileName The JSON file name.
     * @return The JSON content as a String.
     */
    private fun loadJSONFromAsset(context: Context, fileName: String): String {
        return try {
            val inputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            ""
        }
    }
}
