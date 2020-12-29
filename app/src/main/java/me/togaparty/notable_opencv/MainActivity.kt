package me.togaparty.notable_opencv

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {
    private var imageCapture: ImageCapture? = null
    private lateinit var outDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        val camCaptureButton = findViewById<Button>(R.id.cam_capture_button)
        camCaptureButton.setOnClickListener{takePhoto()}
        outDirectory = getOutDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
        // Example of a call to a native method
        //findViewById<EditText>(R.id.editText).text = stringFromJNI()
    }
    private fun startCamera() {
        val cameraFutureProvider = ProcessCameraProvider.getInstance(this)
        cameraFutureProvider.addListener({
            val cameraProvider: ProcessCameraProvider = cameraFutureProvider.get()

            val preview = Preview.Builder()
                    .build()
                    .also {
                        val imagePreview = findViewById<PreviewView>(R.id.camera_view)
                        it.setSurfaceProvider(imagePreview.surfaceProvider)
                    }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector,preview)
            } catch (ex: Exception) {
                Log.e(TAG, "Use case binding failed", ex)
            }
        }, ContextCompat.getMainExecutor(this)
        )
    }
    private fun takePhoto() {

        val imageCapture = imageCapture ?: return //

        val imageFile = File(outDirectory,
                SimpleDateFormat(DATE_FORMAT, Locale.TAIWAN)
                        .format(System.currentTimeMillis()) + ".jpg")
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(imageFile).build()

        imageCapture.takePicture(outputFileOptions,
                ContextCompat.getMainExecutor(this),
                object: ImageCapture.OnImageSavedCallback{
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        val savedURI = Uri.fromFile(imageFile)
                        val msg = "Photo capture succeeded: $savedURI"
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                        Log.d(TAG, msg)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e(TAG, "Photo capture failed: ${exception.message}")
                    }

                }
        )
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
        private const val DATE_FORMAT = "EEE dd_MM_yyyy HH:mm:ss"
        private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}