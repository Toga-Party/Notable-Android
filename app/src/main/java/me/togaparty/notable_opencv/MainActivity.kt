package me.togaparty.notable_opencv

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

//typealias LumaListener = (luma: Double) -> Unit

class MainActivity : AppCompatActivity(), CameraXConfig.Provider {

    private lateinit var container: ConstraintLayout

    private lateinit var previewFinder: PreviewView

    private lateinit var outputDirectory: File

    private lateinit var cameraExecutor: ExecutorService

    private var imageCapture: ImageCapture? = null

    //private var cameraProvider: ProcessCameraProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        container = findViewById(R.id.constraint_layout)
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        val camCaptureButton = findViewById<Button>(R.id.cam_capture_button)
        camCaptureButton.setOnClickListener{takePhoto()}
        previewFinder = findViewById(R.id.camera_view)
        outputDirectory = getOutDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()
        // Example of a call to a native method
        //findViewById<EditText>(R.id.editText).text = stringFromJNI()
    }
    override fun getCameraXConfig(): CameraXConfig {
        return Camera2Config.defaultConfig()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener( {
            val cameraProvider = cameraProviderFuture.get()
            val rotation = previewFinder.display.rotation
            val metrics = DisplayMetrics().also { previewFinder.display.getRealMetrics(it) }
            Log.d(TAG, "Screen metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")

            val preview : Preview = Preview.Builder()
                    .setTargetRotation(rotation)
                    .build()
                    .also {

                        it.setSurfaceProvider(previewFinder.surfaceProvider)
                    }
            val cameraSelector : CameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()
            imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setTargetRotation(rotation)
                    .build()

            cameraProvider.unbindAll()
            try {
                cameraProvider.bindToLifecycle(
                        this as LifecycleOwner, cameraSelector, preview, imageCapture)
                preview.setSurfaceProvider(previewFinder.surfaceProvider)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))

    }

    private fun takePhoto() {

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
        imageCapture.takePicture(
                outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                val msg = "Photo capture succeeded: $savedUri"
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                Log.d(TAG, msg)
            }
        })

    }

    private fun getOutDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let{
            File(it, resources.getString(R.string.app_name)).apply { mkdirs()}}
        return if (mediaDir !=null && mediaDir.exists()) mediaDir else filesDir
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE_PERMISSIONS && allPermissionsGranted()) {
            startCamera()
        } else {
            Toast.makeText(this,
                            "Permissions not granted by the user.",
                                   Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    private external fun stringFromJNI(): Editable?

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
        private const val REQUEST_CODE_PERMISSIONS = 12
        private const val TAG = "Notable:CameraX"
        private const val FILENAME_FORMAT = "EEE dd_MM_yyyy HH:mm:ss"
        private const val PHOTO_EXTENSION = ".jpg"

        private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}