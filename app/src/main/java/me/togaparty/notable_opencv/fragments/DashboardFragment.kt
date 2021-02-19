package me.togaparty.notable_opencv.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import me.togaparty.notable_opencv.R
import me.togaparty.notable_opencv.utils.showDeniedDialog
import me.togaparty.notable_opencv.utils.showPermissionRequestDialog

class DashboardFragment : Fragment(), View.OnClickListener {
    private lateinit var navController: NavController
    private lateinit var checkPermissions: ActivityResultLauncher<Array<String>>
    private var navDirections: NavDirections? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPermissions()
        navController = this.findNavController()
        view.findViewById<CardView>(R.id.camera_cardview).setOnClickListener(this)
        view.findViewById<CardView>(R.id.files_cardview).setOnClickListener(this)
        view.findViewById<CardView>(R.id.settings_cardview).setOnClickListener(this)
        view.findViewById<CardView>(R.id.glossary_cardview).setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.camera_cardview ->
                when {
                    permissionsGranted(requireContext(),
                        mutableListOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE).apply {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                                    add(Manifest.permission.ACCESS_MEDIA_LOCATION)
                                }
                        }) -> {
                        navController.navigate(
                            DashboardFragmentDirections.actionDashboardFragmentToCameraFragment()
                        )
                    }
                    shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ||
                    shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)-> {
                        requireContext().showPermissionRequestDialog(
                            "Permission Required",
                            "Camera access and File access is required to use this feature."
                        ) {
                            checkPermissions.launch(CAMERA_REQUIRED_PERMISSIONS.toTypedArray())
                            navDirections =
                                    DashboardFragmentDirections.actionDashboardFragmentToCameraFragment()
                        }
                    }
                    else -> {
                        checkPermissions.launch(CAMERA_REQUIRED_PERMISSIONS.toTypedArray())
                        navDirections =
                                DashboardFragmentDirections.actionDashboardFragmentToCameraFragment()
                    }
                }
            R.id.files_cardview ->
                when {
                    permissionsGranted(requireContext(),
                            mutableListOf(
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE).apply {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                                    add(Manifest.permission.ACCESS_MEDIA_LOCATION)
                                }
                            }) -> {
                        navController.navigate(
                                DashboardFragmentDirections.actionDashboardFragmentToGalleryFragment())
                    }
                    shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)-> {
                        requireContext().showPermissionRequestDialog(
                            "Permission Required",
                            "File access is required to use this feature."
                        ) {
                            checkPermissions.launch(FILE_REQUIRED_PERMISSIONS.toTypedArray())
                            navDirections =
                                    DashboardFragmentDirections.actionDashboardFragmentToGalleryFragment()
                        }
                    }
                    else -> {
                        checkPermissions.launch(FILE_REQUIRED_PERMISSIONS.toTypedArray())
                        navDirections =
                                DashboardFragmentDirections.actionDashboardFragmentToGalleryFragment()
                    }
                }
            R.id.settings_cardview -> navController.navigate(
                    DashboardFragmentDirections.actionDashboardFragmentToSettingsFragment())
            R.id.glossary_cardview -> navController.navigate(
                    DashboardFragmentDirections.actionDashboardFragmentToGlossaryFragment())
        }
    }
    private fun navigateToFragment() {
        navController.navigate(navDirections!!)
    }

    private fun setPermissions() {

        checkPermissions =
            registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) {
                permissions ->
                if (permissions[Manifest.permission.CAMERA] == true &&
                    permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true &&
                    permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true){
                        navigateToFragment()
                } else {
                    requireContext().showDeniedDialog(
                     "Access denied",
                    "You can accept the permissions needed in the Setting page")
                }
            }
    }


    companion object {
        fun permissionsGranted(context: Context, permissions: List<String>) = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        val CAMERA_REQUIRED_PERMISSIONS = mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(Manifest.permission.ACCESS_MEDIA_LOCATION)
            }
        }

        val FILE_REQUIRED_PERMISSIONS = mutableListOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(Manifest.permission.ACCESS_MEDIA_LOCATION)
            }
        }
    }
}