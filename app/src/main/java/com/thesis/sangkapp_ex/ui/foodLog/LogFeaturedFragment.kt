    package com.thesis.sangkapp_ex.ui.foodLog

    import android.content.Context
    import android.os.Bundle
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import androidx.fragment.app.Fragment
    import androidx.navigation.fragment.findNavController
    import androidx.recyclerview.widget.GridLayoutManager
    import androidx.recyclerview.widget.RecyclerView
    import com.thesis.sangkapp_ex.R
    import com.thesis.sangkapp_ex.ui.foodLog.LogMyRecipeFragment.OnRecipeSelectedListener
    import com.thesis.sangkapp_ex.ui.recipe.Recipe

    class LogFeaturedFragment : Fragment() {

        interface OnFeaturedSelectedListener {
            fun onFeaturedSelected(recipe: Recipe)
        }


        private lateinit var foodItemsRecyclerView: RecyclerView
        private lateinit var logFoodAdapter: LogFoodAdapter
        private lateinit var foodList: List<Recipe>
        private var listener: OnFeaturedSelectedListener? = null


        override fun onAttach(context: Context) {
            super.onAttach(context)
            listener = parentFragment as? OnFeaturedSelectedListener
        }

        override fun onDetach() {
            super.onDetach()
            listener = null
        }

        // Dummy food list (replace with actual data)
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view = inflater.inflate(R.layout.fragment_log_featured, container, false)

            foodItemsRecyclerView = view.findViewById(R.id.foodItemsRecyclerView)
            foodItemsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

            foodList = loadFoodDensityFromAssets() // 🔥 Dynamic and complete
            logFoodAdapter = LogFoodAdapter(foodList) { selectedRecipe -> onFoodItemClick(selectedRecipe) }

            foodItemsRecyclerView.adapter = logFoodAdapter
            return view
        }



        private val drawableMap = mapOf(
            "arrozcaldo" to R.drawable.arrozcaldo,
            "balbacua" to R.drawable.balbacua,
            "bulalo" to R.drawable.bulalo,
            "champorado" to R.drawable.champorado,
            "chicken_adobo" to R.drawable.chicken_adobo,
            "chicken_inasal" to R.drawable.chicken_inasal,
            "chicken_tinola" to R.drawable.chicken_tinola,
            "daing_na_bangus" to R.drawable.daing_na_bangus,
            "dinuguan" to R.drawable.dinuguan,
            "halo_halo" to R.drawable.halo_halo,
            "kanin" to R.drawable.kanin,
            "kansi" to R.drawable.kansi,
            "kare_kare" to R.drawable.kare_kare,
            "kinilaw" to R.drawable.kinilaw,
            "kutsinta" to R.drawable.kutsinta,
            "la_paz_batchoy" to R.drawable.la_paz_batchoy,
            "lumpiang_shanghai" to R.drawable.lumpiang_shanghai,
            "pancit_bihon" to R.drawable.pancit_bihon,
            "pancit_molo" to R.drawable.pancit_molo,
            "pandesal" to R.drawable.pandesal,
            "pastil" to R.drawable.pastil,
            "pinakbet" to R.drawable.pinakbet,
            "pork_adobo" to R.drawable.pork_adobo,
            "pork_sinigang" to R.drawable.pork_sinigang,
            "pork_sisig" to R.drawable.pork_sisig,
            "pyanggang" to R.drawable.pyanggang,
            "satti" to R.drawable.satti,
            "shrimp_sinigang" to R.drawable.shrimp_sinigang,
            "tiyula_itum" to R.drawable.tiyula_itum,
            "tortang_talong" to R.drawable.tortang_talong
        )

        private fun loadFoodDensityFromAssets(): List<Recipe> {
            val json = requireContext().assets.open("dish_density.json")
                .bufferedReader().use { it.readText() }

            val gson = com.google.gson.Gson()
            val itemType = object : com.google.gson.reflect.TypeToken<List<FoodItem>>() {}.type
            val foodItems: List<FoodItem> = gson.fromJson(json, itemType)

            return foodItems.map { item ->
                val name = item.name
                val drawableKey = name.lowercase().replace(" ", "_")
                val imageResId = drawableMap[drawableKey] ?: run {
                    Log.w("DrawableMap", "Missing drawable for key: $drawableKey")
                    R.drawable.sangkapp_placeholder
                }

                Recipe(name = name, calories = item.calories, imageResId = imageResId)
            }

        }



        private fun onFoodItemClick(recipe: Recipe) {
            listener?.onFeaturedSelected(recipe)
        }



    }
