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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController

class PermissionsFragment : Fragment() {

    private var actionDirection : String? = null
    private var actionHandler = ArrayList<String>()
    private val rootView by lazy { FrameLayout(requireContext()) }
    private lateinit var navController: NavController
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setFragmentResultListener("requestKey") { _, bundle ->
            Log.d("Permissions Debug", "Bundle retrieved.")
            actionDirection = bundle.getString("actionDirection")
        }
        navController = this.findNavController()
        return rootView
    }

    override fun onResume() {
        super.onResume()
        if (!allPermissionsGranted(requireContext())) {
            callRequestPermissions()
        } else {
            navController.navigate(PermissionsFragmentDirections.actionPermissionsFragmentPop())
        }
    }
    private fun callRequestPermissions() = requestPermissions(
            REQUIRED_PERMISSIONS.toTypedArray(), REQUEST_CODE_PERMISSIONS)

    override fun onDestroyView() {
        super.onDestroyView()
        actionHandler.clear()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if(requestCode == REQUEST_CODE_PERMISSIONS) {
            if(grantResults.isNotEmpty()) {
                if (grantResults.indexOfFirst { it != PackageManager.PERMISSION_GRANTED } > 0) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        val builder = AlertDialog.Builder(requireContext())
                        builder.setMessage("You need to accept required permissions to use this feature.")
                            .setTitle("Permission required")
                            .setPositiveButton("Re-try") { _, _ ->
                                Log.i("PermissionFragment", "Clicked")
                                callRequestPermissions()
                            }
                            .setNegativeButton("Cancel") {_, _ ->
                                navController.navigate(PermissionsFragmentDirections.actionPermissionsFragmentToDashboardFragment())
                            }
                        val dialog = builder.create()
                        dialog.show()
                    } else {
                        val builder = AlertDialog.Builder(requireContext())
                        builder.setMessage("You need to accept required permissions to use this feature.")
                                .setTitle("Permission denied")
                                .setPositiveButton("Re-try") { _, _ ->
                                    Log.i("PermissionFragment", "Clicked")
                                    callRequestPermissions()
                                }
                                .setNegativeButton("Cancel") {_, _ ->
                                    navController.navigate(PermissionsFragmentDirections.actionPermissionsFragmentToDashboardFragment())
                                }

                        val dialog = builder.create()
                        dialog.show()
                        navController.navigate(PermissionsFragmentDirections.actionPermissionsFragmentPop())
                    }
                }
                Toast.makeText(requireContext(), "Permission request granted", Toast.LENGTH_SHORT).show()
                navController.navigate(PermissionsFragmentDirections.actionPermissionsFragmentPop())
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