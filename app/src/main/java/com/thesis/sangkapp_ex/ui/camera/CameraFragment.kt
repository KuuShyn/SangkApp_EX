package com.thesis.sangkapp_ex.ui.camera

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.thesis.sangkapp_ex.BoundingBox
import com.thesis.sangkapp_ex.Constants.LABELS_PATH
import com.thesis.sangkapp_ex.Constants.MODEL_PATH
import com.thesis.sangkapp_ex.Detector
import com.thesis.sangkapp_ex.databinding.FragmentCameraBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment(), Detector.DetectorListener {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private var isFrontCamera = false
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var detector: Detector? = null
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var pickPhotoLauncher: ActivityResultLauncher<Intent>

    private var isCameraStarting = false
    private var isCameraActive = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        detector?.close()
        cameraExecutor.shutdown()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Initialize the detector on a background thread
        lifecycleScope.launch(Dispatchers.IO) {
            detector = Detector(requireContext(), MODEL_PATH, LABELS_PATH, this@CameraFragment) {
                toast(it)
            }
        }

        // Request camera permissions if not already granted
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }

        // Initialize the photo picker
        pickPhotoLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                uri?.let {
                    // Navigate to PhotoFragment and pass the Uri
                    navigateToPhotoFragment(it)
                }
            }
        }

        bindListeners()
    }

    private fun bindListeners() {
        binding.apply {
            uploadButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                pickPhotoLauncher.launch(intent)
            }

            cbGPU.setOnCheckedChangeListener { _, isChecked ->
                cameraExecutor.submit {
                    detector?.restart(isGpu = isChecked)
                }
            }

            captureButton.setOnClickListener {
                imageCapture?.takePicture(
                    ContextCompat.getMainExecutor(requireContext()),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            lifecycleScope.launch(Dispatchers.IO) {
                                val uri = saveImageToUri(image)
                                if (uri != null) {
                                    navigateToPhotoFragment(uri)
                                } else {
                                    toast("Failed to save image")
                                }
                            }
                            image.close()
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                            toast("Photo capture failed")
                        }
                    }
                )
            }

            switchCamButton.setOnClickListener {
                isFrontCamera = !isFrontCamera
                bindCameraUseCases()
            }
        }
    }

    private fun saveImageToUri(image: ImageProxy): Uri? {
        val bitmap = imageProxyToBitmap(image)

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "captured_image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/sangkapp")
        }

        val uri = requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            requireContext().contentResolver.openOutputStream(it).use { outputStream ->
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
            }
        }

        return uri
    }

    private fun navigateToPhotoFragment(imageUri: Uri) {
        requireActivity().runOnUiThread {
            val action = CameraFragmentDirections
                .actionCameraFragmentToPhotoFragment(
                    imageUri // Pass the URI instead of the Bitmap
                )
            findNavController().navigate(action)
        }
    }

    private fun startCamera() {
        if (isCameraStarting || isCameraActive) return
        isCameraStarting = true
        Log.d(TAG, "Starting camera...")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                Log.d(TAG, "Camera provider obtained")
                bindCameraUseCases()
                isCameraActive = true
            } catch (e: Exception) {
                Log.e(TAG, "Error starting camera: ${e.message}", e)
                toast("Failed to start camera")
            } finally {
                isCameraStarting = false
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        // Convert YUV to Bitmap (implement this based on your ImageAnalysis format)
        // For OUTPUT_IMAGE_FORMAT_YUV_420_888, use the following conversion
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        // U and V are swapped
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = android.graphics.YuvImage(nv21, android.graphics.ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(android.graphics.Rect(0, 0, image.width, image.height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")
        val rotation = binding.viewFinder.display.rotation

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(if (isFrontCamera) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK)
            .build()

        preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(rotation)
            .build()

        imageCapture = ImageCapture.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetRotation(rotation)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetRotation(binding.viewFinder.display.rotation)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .build()

        imageAnalyzer?.setAnalyzer(cameraExecutor) { imageProxy ->
            try {
                val bitmap = imageProxyToBitmap(imageProxy)
                detector?.detect(bitmap)
            } catch (e: Exception) {
                Log.e(TAG, "Image analysis failed: ${e.message}", e)
            } finally {
                imageProxy.close()
            }
        }

        cameraProvider.unbindAll()

        try {
            camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageCapture,
                imageAnalyzer
            )
            preview?.surfaceProvider = binding.viewFinder.surfaceProvider
            Log.d(TAG, "Camera bound to lifecycle")
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
            toast("Failed to bind camera use cases")
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        var granted = true
        permissions.entries.forEach {
            if (!it.value) {
                granted = false
            }
        }
        if (granted) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
            // Optionally, navigate back or disable camera functionality
        }
    }

    override fun onEmptyDetect() {
        if (view != null && isAdded) {
            requireActivity().runOnUiThread {
                binding.overlay.clear()
            }
        }
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
        if (view != null && isAdded) {
            requireActivity().runOnUiThread {
                "${inferenceTime}ms".also { binding.inferenceTime.text = it }
                binding.overlay.apply {
                    setResults(boundingBoxes)
                    invalidate()
                }
            }
        }
    }

    private fun toast(message: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val TAG = "CameraFragment"
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA
            // Add other permissions if necessary
        )
    }
}
