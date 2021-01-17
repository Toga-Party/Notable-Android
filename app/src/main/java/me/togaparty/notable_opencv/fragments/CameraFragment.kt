package me.togaparty.notable_opencv.fragments
import android.graphics.*
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.NavHostFragment
import me.togaparty.notable_opencv.MainActivity
import me.togaparty.notable_opencv.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.camera.extensions.HdrImageCaptureExtender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.launch

class CameraFragment : Fragment(), CameraXConfig.Provider {



    private lateinit var container: ConstraintLayout
    private lateinit var viewFinder: PreviewView
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            if (!PermissionsFragment.allPermissionsGranted(requireContext())) {
                NavHostFragment.findNavController(this)
                        .navigate(CameraFragmentDirections.actionCameraFragmentToPermissionsFragment())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!PermissionsFragment.allPermissionsGranted(requireContext())) {
            NavHostFragment.findNavController(this)
                    .navigate(CameraFragmentDirections.actionCameraFragmentToPermissionsFragment())
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_camera, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        container = view as ConstraintLayout
        viewFinder = container.findViewById(R.id.view_finder)
        cameraExecutor = Executors.newSingleThreadExecutor()
        outputDirectory = MainActivity.getOutputDirectory(requireContext())
        container.findViewById<Button>(R.id.cam_capture_button).setOnClickListener{takePhoto()}
        viewFinder.post {
            startCamera()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }

    override fun getCameraXConfig(): CameraXConfig {
        return Camera2Config.defaultConfig()
    }
    private fun startCamera() {
        //GlobalScope.launch(Dispatchers.IO) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
            cameraProviderFuture.addListener( {
                val cameraProvider = cameraProviderFuture.get()
                val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
                val screenSize = Size(metrics.widthPixels, metrics.heightPixels)
                val rotation = viewFinder.display.rotation

                preview = Preview.Builder()
                        .setTargetRotation(rotation)
                        .setTargetResolution(screenSize)
                        .build()
                        .also {
                            it.setSurfaceProvider(viewFinder.surfaceProvider)

                        }


                val cameraSelector : CameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()

                val builder = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .setTargetRotation(rotation)


                val hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder)

                if(hdrImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
                    hdrImageCaptureExtender.enableExtension(cameraSelector)
                }

                imageCapture = builder.build()


                cameraProvider.unbindAll()
                try {
                    camera = cameraProvider.bindToLifecycle(
                            this as LifecycleOwner, cameraSelector, preview, imageCapture)
                    preview?.setSurfaceProvider(viewFinder.surfaceProvider)

                } catch(exc: Exception) {
                    Log.e(TAG, "Use case binding failed", exc)
                }

            }, ContextCompat.getMainExecutor(requireContext()))
       // }
    }

    private fun takePhoto() {
        Log.d(TAG, "TakePhoto method is called")
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
                outputDirectory,
                SimpleDateFormat(FILENAME_FORMAT, Locale.US
                ).format(System.currentTimeMillis()) + PHOTO_EXTENSION)


        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        /*
        imageCapture.takePicture(
                outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {


            override fun onError(exc: ImageCaptureException) {
                Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                val msg = "Photo capture succeeded: $savedUri"
                viewFinder.post {
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
                Log.d(TAG, msg)
            }
        })*/
        imageCapture.takePicture(cameraExecutor, object: ImageCapture.OnImageCapturedCallback(){
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)

                //val bitmap = image.toBitmap()
                //TODO
                // To actually implement this bit for OpenCV.

                val msg = "Photo capture succeeded"
                viewFinder.post{
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
                Log.d(TAG, msg)
                image.close()
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
            }
        })
    }
    fun ImageProxy.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer // Y
        val vuBuffer = planes[2].buffer // VU

        val ySize = yBuffer.remaining()
        val vuSize = vuBuffer.remaining()

        val nv21 = ByteArray(ySize + vuSize)

        yBuffer.get(nv21, 0, ySize)
        vuBuffer.get(nv21, ySize, vuSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    companion object {
        private const val TAG = "Notable:CameraX"
        private const val FILENAME_FORMAT = "EEE dd_MM_yyyy HH:mm:ss"
        private const val PHOTO_EXTENSION = ".jpg"
    }


}