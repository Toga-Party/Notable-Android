package me.togaparty.notable_opencv.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import me.togaparty.notable_opencv.R

class PermissionsFragment : Fragment() {


    private var actionHandler = ArrayList<String>()
    private val rootView by lazy { FrameLayout(requireContext()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("PERMISSIONSDEBUG", "Permissions Fragment created")
        setFragmentResultListener("requestKey") { _ , bundle ->
            Log.d("PERMISSIONSDEBUG", "Bundle retrieved.")
            bundle.getString("cameraFragment")?.let { actionHandler.add(it) }
            bundle.getString("filesFragment")?.let { actionHandler.add(it) }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return rootView
    }

    override fun onResume() {
        super.onResume()
        if (!allPermissionsGranted(requireContext())) {
            requestPermissions(
                    REQUIRED_PERMISSIONS.toTypedArray(), REQUEST_CODE_PERMISSIONS)
        } else {
            if (actionHandler.toString().equals("[CameraFragment]")) {
                NavHostFragment.findNavController(this)
                        .navigate(PermissionsFragmentDirections.actionPermissionsFragmentToCameraFragment())
            } else {
                NavHostFragment.findNavController(this)
                        .navigate(PermissionsFragmentDirections.actionPermissionsFragmentToFilesFragment())
            }

        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        actionHandler.clear()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if(requestCode == REQUEST_CODE_PERMISSIONS) {
            if(grantResults.isNotEmpty()) {
                val index = grantResults.indexOfFirst { it != PackageManager.PERMISSION_GRANTED }
                if (index < 0) {
                    Toast.makeText(requireContext(), "Permission request granted", Toast.LENGTH_SHORT).show()
                    if (actionHandler.toString().equals("[CameraFragment]")) {
                        NavHostFragment.findNavController(this)
                                .navigate(PermissionsFragmentDirections.actionPermissionsFragmentToCameraFragment())
                    } else {
                        NavHostFragment.findNavController(this)
                                .navigate(PermissionsFragmentDirections.actionPermissionsFragmentToFilesFragment())
                    }
                }else {
                    Log.i("PermissionFragment", "Permission to record denied")
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        val builder = AlertDialog.Builder(requireContext())
                        builder.setMessage("Permission to access the camera and file are required for this " +
                            "app to capture and process music sheets.")
                            .setTitle("Permission required")
                            .setPositiveButton("Re-try") { _, _ ->
                                Log.i("PermissionFragment", "Clicked")
                                requestPermissions(
                                        REQUIRED_PERMISSIONS.toTypedArray(), REQUEST_CODE_PERMISSIONS)
                            }
                        val dialog = builder.create()
                        dialog.show()
                    } else {
                        val builder = AlertDialog.Builder(requireContext())
                        builder.setMessage("Permission to access the camera  is required for this " +
                                "app to capture and process music sheets.")
                                .setTitle("Permission denied")
                                .setPositiveButton("Re-try") { _, _ ->
                                    Log.i("PermissionFragment", "Clicked")
                                    requestPermissions(
                                            REQUIRED_PERMISSIONS.toTypedArray(), REQUEST_CODE_PERMISSIONS)
                                }
                        val dialog = builder.create()
                        dialog.show()
                        NavHostFragment.findNavController(this)
                                .navigate(PermissionsFragmentDirections.actionPermissionsFragmentPop())
                    }
                }
            }
        } else {
        Toast.makeText(requireContext(),
            "Permissions not granted by the user.",
            Toast.LENGTH_SHORT).show()
        }
    }


    companion object {

        const val REQUEST_CODE_PERMISSIONS = 110
        val REQUIRED_PERMISSIONS = mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(Manifest.permission.ACCESS_MEDIA_LOCATION)
            }
        }

        fun allPermissionsGranted(context: Context) = REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}