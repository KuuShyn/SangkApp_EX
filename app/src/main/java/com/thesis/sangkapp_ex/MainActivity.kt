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

        // Get camera calibration parameters
        val cameraParams = getCameraCalibrationParams(this)
        if (cameraParams != null) {
            // Set camera parameters in ViewModel
            photoViewModel.setCameraParams(cameraParams)
        }

        setSupportActionBar(binding.appBarMain.toolbar)

//        binding.appBarMain.fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null)
//                .setAnchorView(R.id.fab).show()
//        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,  R.id.nav_profile, R.id.nav_camera, R.id.nav_recipe
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Handle fragment navigation based on intent extras
        val fragmentToLoad = intent.getStringExtra("fragment")
        if (fragmentToLoad == "profile_details") {
            navController.navigate(R.id.nav_profile)  // Navigate to ProfileDetailsFragment
        } else if (fragmentToLoad == "profile_input") {
            navController.navigate(R.id.profile_input)  // Navigate to ProfileInputFragment
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.main, menu)
//        return true
//    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }



    // Function to get camera calibration parameters
    private fun getCameraCalibrationParams(context: Context): CameraCalibrationParams? {
        try {
            // Access the camera manager
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraIdList = cameraManager.cameraIdList

            // Loop through available cameras (back camerwa preferred)
            for (cameraId in cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)

                // We are interested in the back-facing camera
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                    // Get the focal length
                    val focalLengths = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                    val focalLength = focalLengths?.get(0) ?: return null // Focal length in mm

                    // Get the sensor physical size
                    val sensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
                    val sensorWidth = sensorSize?.width ?: return null // Sensor width in mm
                    val sensorHeight = sensorSize.height // Sensor height in mm

                    // Return the calibration parameters
                    return CameraCalibrationParams(focalLength, sensorWidth, sensorHeight)
                }
            }
        } catch (e: Exception) {
            Log.e("CameraParams", "Error retrieving camera calibration parameters: ${e.localizedMessage}")
        }
        return null
    }

}