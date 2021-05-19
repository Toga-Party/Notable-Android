package me.togaparty.notable_android.ui.fragments

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.common.util.concurrent.ListenableFuture
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.togaparty.notable_android.MainActivity
import me.togaparty.notable_android.R
import me.togaparty.notable_android.databinding.FragmentCameraBinding
import me.togaparty.notable_android.utils.ALL_REQUIRED_PERMISSIONS
import me.togaparty.notable_android.utils.Constants.Companion.TAG
import me.togaparty.notable_android.utils.permissionsGranted
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors


class CameraFragment : Fragment(R.layout.fragment_camera) {

    private val binding by viewBinding(FragmentCameraBinding::bind)
    private lateinit var outputDirectory: File

    private lateinit var processCameraProvider: ProcessCameraProvider
    private lateinit var processCameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    internal lateinit var imageCapture: ImageCapture
    internal lateinit var navController: NavController

    private lateinit var orientationEventListener: OrientationEventListener



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = this.findNavController()
        if(!permissionsGranted(requireContext(), ALL_REQUIRED_PERMISSIONS)) {
            navController.navigate(CameraFragmentDirections.actionCameraFragmentToDashboardFragment())
        }
        outputDirectory = MainActivity.getOutputCacheDirectory(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.camCaptureButton.setOnClickListener{takePhoto()}

        lifecycleScope.launch {
            processCameraProviderFuture = ProcessCameraProvider.getInstance(requireContext()).apply {
                addListener({
                    processCameraProvider = processCameraProviderFuture.get()
                    startCamera()
                    orientationEventListener = object : OrientationEventListener(requireContext()) {
                        override fun onOrientationChanged(orientation: Int) {
                            val rotation = when (orientation) {
                                in 45 until 135 -> Surface.ROTATION_270
                                in 135 until 225 -> Surface.ROTATION_180
                                in 225 until 315 -> Surface.ROTATION_90
                                else -> Surface.ROTATION_0
                            }
                            imageCapture.targetRotation = rotation
                        }
                    }
                    orientationEventListener.enable()
                }, ContextCompat.getMainExecutor(requireContext()))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::processCameraProvider.isInitialized) {
            processCameraProvider.unbindAll()
        }
        orientationEventListener.disable()

    }

    private fun startCamera() {
        processCameraProvider.unbindAll()
        fun previewUseCase() : Preview {
            val display = binding.viewFinder.display
            val metrics = DisplayMetrics().also { display.getRealMetrics(it) }
            return Preview.Builder()
                    .setTargetRotation(display.rotation)
                    .setTargetResolution(Size(metrics.widthPixels, metrics.heightPixels))
                    .build()
                    .also {
                        it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                    }
        }
        fun captureUseCase(): ImageCapture {
            val display = binding.viewFinder.display
            val metrics = DisplayMetrics().also { display.getRealMetrics(it) }
            imageCapture = ImageCapture.Builder()
                    .setTargetRotation(display.rotation)
                    .setTargetResolution(Size(metrics.widthPixels, metrics.heightPixels))
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
            return imageCapture
        }

            processCameraProvider.bindToLifecycle(
                    this as LifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    previewUseCase(),
                    captureUseCase())
    }

    private fun takePhoto() {
        Log.d(TAG, "TakePhoto method is called")
        val photoName = SimpleDateFormat(FILENAME_FORMAT, Locale.US
        ).format(System.currentTimeMillis()) + PHOTO_EXTENSION
        val photoFile = File(outputDirectory, photoName)

        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(outputFileOptions, Executors.newSingleThreadExecutor(),
                object : ImageCapture.OnImageSavedCallback {

                    override fun onError(error: ImageCaptureException) {
                        val message = "Image captured successfully"
                        lifecycleScope.launch(Dispatchers.Main) {
                            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        val message = "Image captured successfully"
                        setFragmentResult("requestKey",
                                bundleOf("photoPath" to photoName))
                        lifecycleScope.launch(Dispatchers.Main){
                            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                            navController.navigate(CameraFragmentDirections.actionCameraFragmentToPreviewImage())
                        }
                    }
                }
        )
    }

    companion object {
        private const val FILENAME_FORMAT = "EEE_dd_MM_yyyy_HHmmss"
        private const val PHOTO_EXTENSION = ".png"
    }


}
