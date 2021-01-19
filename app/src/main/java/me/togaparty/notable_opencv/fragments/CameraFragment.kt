package me.togaparty.notable_opencv.fragments
import android.graphics.Bitmap
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
import androidx.camera.extensions.HdrImageCaptureExtender
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.NavHostFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.togaparty.notable_opencv.MainActivity
import me.togaparty.notable_opencv.R
import me.togaparty.notable_opencv.utils.convertImageProxyToBitmap
import me.togaparty.notable_opencv.utils.otsuThreshold
import me.togaparty.notable_opencv.utils.toMat
import org.opencv.android.InstallCallbackInterface
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.android.OpenCVLoader.initAsync
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

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
        initOpenCV()
    }
    private fun initOpenCV() {

        val isInitialized = OpenCVLoader.initDebug()

        if (isInitialized){
            Log.d(TAG, "The OpenCV was successfully initialized in debug mode using .so libs.")
        } else {
            initAsync(OpenCVLoader.OPENCV_VERSION, requireContext(), loader)
        }

    }
    override fun onResume() {
        super.onResume()
        if (!PermissionsFragment.allPermissionsGranted(requireContext())) {
            NavHostFragment.findNavController(this)
                    .navigate(CameraFragmentDirections.actionCameraFragmentToPermissionsFragment())
        }
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            initAsync(OpenCVLoader.OPENCV_VERSION, requireContext(), loader)
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!")
            loader.onManagerConnected(LoaderCallbackInterface.SUCCESS)
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
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
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

    }

    private fun takePhoto() {
        Log.d(TAG, "TakePhoto method is called")
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
                outputDirectory,
                SimpleDateFormat(FILENAME_FORMAT, Locale.US
                ).format(System.currentTimeMillis()) + PHOTO_EXTENSION)
        photoFile.createNewFile()
        // Create output options object which contains file (Used in another takePicture method)
            //val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has been taken

        imageCapture.takePicture(cameraExecutor, object: ImageCapture.OnImageCapturedCallback(){

            override fun onCaptureSuccess(image: ImageProxy) {
                //rudimentary off loading the work
                GlobalScope.launch(Dispatchers.IO) {
                    val bitmap = withContext(Dispatchers.IO) { image.convertImageProxyToBitmap() }
                    withContext(Dispatchers.IO) {
                        bitmap.toMat().otsuThreshold(bitmap) {
                            val byteArrayOutputStream = ByteArrayOutputStream()
                            it.compress(Bitmap.CompressFormat.JPEG, 0, byteArrayOutputStream)
                            val data = byteArrayOutputStream.toByteArray()

                            val fileOutputStream = FileOutputStream(photoFile)
                            fileOutputStream.write(data)
                            fileOutputStream.flush()
                            fileOutputStream.close()
                        }

                    }

                }

                val msg = "Photo capture succeeded: "
                viewFinder.post{
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
                Log.d(TAG, msg)
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
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
        private const val FILENAME_FORMAT = "EEE dd_MM_yyyy HH:mm:ss"
        private const val PHOTO_EXTENSION = ".jpg"
    }


}
