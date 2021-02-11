package me.togaparty.notable_opencv.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.extensions.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.common.util.concurrent.ListenableFuture
import me.togaparty.notable_opencv.MainActivity
import me.togaparty.notable_opencv.R
import org.opencv.android.InstallCallbackInterface
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.android.OpenCVLoader.initAsync
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import kotlin.reflect.KClass

class CameraFragment : Fragment() {


    private lateinit var outputDirectory: File

    private lateinit var camera: Camera

    private lateinit var cameraExecutor: Executor
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraSelector: CameraSelector
    private lateinit var cameraProvider: ProcessCameraProvider

    private lateinit var previewView: PreviewView
    private lateinit var preview: Preview

    private lateinit var useCases: MutableList<UseCase>
    private lateinit var imageCapture: ImageCapture

    private lateinit var container: ConstraintLayout
    private lateinit var navController: NavController

    private val result = "CameraFragment"



    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("CAMERADEBUG", "Camera Fragment created")
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            initializeOpenCV()
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!")
            loader.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!PermissionsFragment.allPermissionsGranted(requireContext())) {
            Log.d("Camera Debug", "Called to navigate to PermissionsFragment")
            setFragmentResult("requestKey", bundleOf("cameraFragment" to result))
            navController.navigate(CameraFragmentDirections.actionCameraFragmentToPermissionsFragment())
        }
        container = view as ConstraintLayout
        navController = this.findNavController()
        previewView = container.findViewById(R.id.view_finder)
        outputDirectory = MainActivity.getOutputDirectory(requireContext())
        container.findViewById<Button>(R.id.cam_capture_button).setOnClickListener{takePhoto()}
    }

    override fun onStart() {
        super.onStart()
        previewView.let {
            it.post {
                startCamera()
            }
        }
        orientationEventListener.enable()
    }
    
    @SuppressLint("RestrictedApi")
    override fun onStop() {
        orientationEventListener.disable()
        cameraProvider.shutdown()
        super.onStop()
    }
    private fun initializeOpenCV() {
        val isInitialized = OpenCVLoader.initDebug()
        if (isInitialized){
            Log.d(TAG, "The OpenCV was successfully initialized in debug mode using .so libs.")
        } else {
            initAsync(OpenCVLoader.OPENCV_VERSION, requireContext(), loader)
        }
    }


    private val orientationEventListener by lazy {
        object : OrientationEventListener(requireContext()) {
            override fun onOrientationChanged(orientation: Int) {
                val rotation = when (orientation) {
                    in 45 until 135 -> Surface.ROTATION_270
                    in 135 until 225 -> Surface.ROTATION_180
                    in 225 until 315 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
                if (::imageCapture.isInitialized) {
                    imageCapture.targetRotation = rotation
                }
            }
        }
    }
    private fun startCamera() {
        val imageCaptureExtenders = listOf(
                HdrImageCaptureExtender::class,
                AutoImageCaptureExtender::class
        )

        val previewExtenders = listOf(
                HdrPreviewExtender::class,
                AutoPreviewExtender::class
        )
        fun getImageCaptureExtender(type: KClass<*>):
                ImageCaptureExtender? {
            val imageCaptureBuilder = ImageCapture.Builder()
            return when (type) {
                AutoImageCaptureExtender::class ->
                    AutoImageCaptureExtender.create(imageCaptureBuilder)
                HdrImageCaptureExtender::class ->
                    HdrImageCaptureExtender.create(imageCaptureBuilder)
                else -> null
            }
        }
        fun enableImageCaptureExtension() {
            imageCaptureExtenders.forEach imageCaptureExtenders@{
                val imageCaptureExtender = getImageCaptureExtender(it)
                if (imageCaptureExtender != null) {
                    if (imageCaptureExtender.isExtensionAvailable(cameraSelector)) {
                        imageCaptureExtender.enableExtension(cameraSelector)
                        return@imageCaptureExtenders
                    }
                }
            }
        }

        fun getPreviewExtender(type: KClass<*>):
                PreviewExtender? {
            val previewBuilder = Preview.Builder()
            return when (type) {
                AutoPreviewExtender::class ->
                    AutoPreviewExtender.create(previewBuilder)
                HdrPreviewExtender::class ->
                    HdrPreviewExtender.create(previewBuilder)
                else -> null
            }
        }
        fun enablePreviewExtension() {
            previewExtenders.forEach previewExtenders@{
                val previewExtender = getPreviewExtender(it)
                if (previewExtender != null) {
                    if (previewExtender.isExtensionAvailable(cameraSelector)) {
                        previewExtender.enableExtension(cameraSelector)
                    }
                    return@previewExtenders
                }
            }
        }

        fun setupCameraSelector() {
            cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()
        }

        fun initCamera() {
            cameraExecutor = ContextCompat.getMainExecutor(requireContext())
            cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        }
        fun setupPreview() {
            val metrics = DisplayMetrics().also { previewView.display.getRealMetrics(it) }
            val screenSize = Size(metrics.widthPixels, metrics.heightPixels)
            val rotation = previewView.display.rotation
            enablePreviewExtension()
            preview = Preview.Builder()
                    .setTargetRotation(rotation)
                    .setTargetResolution(screenSize)
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
        }
        fun setupImageCapture() {
            useCases = mutableListOf()
            enableImageCaptureExtension()
            imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
            useCases.add(imageCapture)
        }
        fun bindPreview(cameraProvider: ProcessCameraProvider) {
            cameraProvider.unbindAll()

            camera = cameraProvider.bindToLifecycle(
                    this as LifecycleOwner,
                    cameraSelector,
                    *useCases.toTypedArray(),
                    preview
            )
            preview.setSurfaceProvider(previewView.surfaceProvider)
        }
        setupCameraSelector()
        initCamera()
        setupPreview()
        cameraProviderFuture.addListener ({
            setupImageCapture()
            cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, cameraExecutor)
    }
    private fun takePhoto() {
        Log.d(TAG, "TakePhoto method is called")
        val photoName = SimpleDateFormat(FILENAME_FORMAT, Locale.US
        ).format(System.currentTimeMillis()) + PHOTO_EXTENSION
        val photoFile = File(outputDirectory, photoName)
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()


        imageCapture.takePicture(outputFileOptions, cameraExecutor,
                object : ImageCapture.OnImageSavedCallback {

                    override fun onError(error: ImageCaptureException) {
                        val message = "Image captured successfully"
                        Log.d(TAG, message)
                        previewView.post {
                            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        val message = "Image captured successfully"
                        setFragmentResult("requestKey",
                                bundleOf("photoPath" to photoName))
                        Log.d(TAG, message)
                        previewView.post {
                            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                        }
                        navController.navigate(CameraFragmentDirections.actionCameraFragmentToPreviewImage())
                    }
                })
    }


    companion object {
        private val loader = object: LoaderCallbackInterface {
            override fun onManagerConnected(status: Int) {
                when(status) {
                    LoaderCallbackInterface.SUCCESS ->
                        Log.d(TAG,"OpenCV successfully started.")
                    LoaderCallbackInterface.INIT_FAILED ->
                        Log.d(TAG,"Failed to start OpenCV.")
                    LoaderCallbackInterface.INSTALL_CANCELED ->
                        Log.d(TAG,"OpenCV installation has been cancelled by the user.")
                    LoaderCallbackInterface.INCOMPATIBLE_MANAGER_VERSION ->
                        Log.d(TAG,"This version of OpenCV Manager is incompatible. Possibly, a service update is required.")
                }
            }

            override fun onPackageInstall(operation: Int, callback: InstallCallbackInterface?) {
                Log.d(TAG,"OpenCV Manager successfully installed")
            }
        }

        private const val TAG = "Notable:CameraX"
        private const val FILENAME_FORMAT = "EEE_dd_MM_yyyy_HHmmss"
        private const val PHOTO_EXTENSION = ".jpg"
    }


}
