package com.thesis.sangkapp_ex

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.activity.viewModels
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import com.thesis.sangkapp_ex.databinding.ActivityMainBinding
import com.thesis.sangkapp_ex.ui.photo.PhotoViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val photoViewModel: PhotoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // 🔐 Lock drawer for profile flow
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isInProfileFlow = destination.id == R.id.profile_input || destination.id == R.id.profile_input2
            binding.drawerLayout.setDrawerLockMode(
                if (isInProfileFlow) DrawerLayout.LOCK_MODE_LOCKED_CLOSED
                else DrawerLayout.LOCK_MODE_UNLOCKED
            )
        }

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_profile, R.id.nav_camera, R.id.nav_recipe, R.id.nav_log_food
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // 🔄 CAMERA CALIBRATION
        getCameraCalibrationParams(this)?.let { photoViewModel.setCameraParams(it) }

        // 🧠 USER PROFILE CHECK (first-time navigation)
        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val isProfileComplete = prefs.getBoolean("PROFILE_COMPLETE", false)

        if (savedInstanceState == null) {
            val fragmentToLoad = intent.getStringExtra("fragment")
            when {
                fragmentToLoad == "profile_details" -> {
                    navController.navigate(R.id.nav_profile)
                }
                fragmentToLoad == "profile_input" -> {
                    navController.navigate(R.id.profile_input)
                }
                isProfileComplete -> {
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.mobile_navigation, true)
                        .build()
                    navController.navigate(R.id.nav_home, null, navOptions)
                }
                else -> {
                    navController.navigate(R.id.profile_input)
                }
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun getCameraCalibrationParams(context: Context): CameraCalibrationParams? {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val backCameraId = cameraManager.cameraIdList.firstOrNull {
                cameraManager.getCameraCharacteristics(it)
                    .get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            }

            backCameraId?.let { cameraId ->
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                val focalLength = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)?.getOrNull(0)
                val sensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)

                if (focalLength != null && sensorSize != null) {
                    CameraCalibrationParams(focalLength, sensorSize.width, sensorSize.height)
                } else null
            }
        } catch (e: Exception) {
            Log.e("CameraParams", "Error retrieving camera params: ${e.message}")
            null
        }
    }
}
