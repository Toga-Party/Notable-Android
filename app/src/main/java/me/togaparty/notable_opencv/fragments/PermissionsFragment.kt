package me.togaparty.notable_opencv.fragments

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment

class PermissionsFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            if (!allPermissionsGranted(requireContext())) {
                requestPermissions(
                        REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
            } else {
                NavHostFragment.findNavController(this)
                        .navigate(PermissionsFragmentDirections.actionPermissionsFragmentToCameraFragment())
            }
        }

    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if(requestCode == REQUEST_CODE_PERMISSIONS &&
            PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()) {
            Toast.makeText(requireContext(), "Permission request granted", Toast.LENGTH_SHORT).show()
            NavHostFragment.findNavController(this)
                    .navigate(PermissionsFragmentDirections.actionPermissionsFragmentToCameraFragment())
        } else {
            Toast.makeText(requireContext(),
                "Permissions not granted by the user.",
                Toast.LENGTH_SHORT).show()
        }
    }
    companion object {

        const val REQUEST_CODE_PERMISSIONS = 10
        val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
        fun allPermissionsGranted(context: Context) = REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}