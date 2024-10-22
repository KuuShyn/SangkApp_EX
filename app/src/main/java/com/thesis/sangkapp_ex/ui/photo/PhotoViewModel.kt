// com.thesis.sangkapp_ex.ui.photo.PhotoViewModel.kt
package com.thesis.sangkapp_ex.ui.photo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.thesis.sangkapp_ex.CameraCalibrationParams

class PhotoViewModel : ViewModel() {
    private val _cameraParams = MutableLiveData<CameraCalibrationParams?>()
    val cameraParams: LiveData<CameraCalibrationParams?> = _cameraParams


    fun setCameraParams(params: CameraCalibrationParams) {
        _cameraParams.value = params
        Log.d(
            "PhotoViewModel",
            "Camera Params Set: Focal Length=${params.focalLength}, Sensor Width=${params.sensorWidth}, Sensor Height=${params.sensorHeight}"
        )
    }


}
