package me.togaparty.notable_android.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import me.togaparty.notable_android.R
import me.togaparty.notable_android.data.ImageListProvider
import me.togaparty.notable_android.utils.*
import me.togaparty.notable_android.utils.Constants.Companion.TAG

class DashboardFragment : Fragment(), View.OnClickListener {
    private lateinit var navController: NavController
    private lateinit var checkPermissions: ActivityResultLauncher<Array<String>>
    private var navDirections: NavDirections? = null
    private lateinit var model: ImageListProvider
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
        model = ViewModelProvider(requireActivity()).get(ImageListProvider::class.java)
        view.findViewById<CardView>(R.id.camera_cardview).setOnClickListener(this)
        view.findViewById<CardView>(R.id.files_cardview).setOnClickListener(this)
        view.findViewById<CardView>(R.id.settings_cardview).setOnClickListener(this)
        view.findViewById<CardView>(R.id.glossary_cardview).setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.camera_cardview ->
                when {
                    permissionsGranted(requireContext(), ALL_REQUIRED_PERMISSIONS) -> {
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
                            Log.d(TAG, "Dashboard: Launching required camera permissions.")
                            navDirections =
                                    DashboardFragmentDirections.actionDashboardFragmentToCameraFragment()
                            checkPermissions.launch(ALL_REQUIRED_PERMISSIONS.toTypedArray())
                        }
                    }
                    else -> {
                        Log.d(TAG, "Dashboard: Launching required camera permissions.")
                        navDirections =
                                DashboardFragmentDirections.actionDashboardFragmentToCameraFragment()
                        checkPermissions.launch(ALL_REQUIRED_PERMISSIONS.toTypedArray())
                    }
                }
            R.id.files_cardview ->
                when {
                    permissionsGranted(requireContext(), FILE_REQUIRED_PERMISSIONS) -> {
                        navController.navigate(
                                DashboardFragmentDirections.actionDashboardFragmentToGalleryFragment())
                    }
                    shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)-> {
                        requireContext().showPermissionRequestDialog(
                            "Permission Required",
                            "File access is required to use this feature."
                        ) {
                            Log.d(TAG, "Dashboard: Launching required file permissions.")
                            navDirections =
                                    DashboardFragmentDirections.actionDashboardFragmentToGalleryFragment()
                            checkPermissions.launch(FILE_REQUIRED_PERMISSIONS.toTypedArray())
                        }
                    }
                    else -> {
                        Log.d(TAG, "Dashboard: Launching required file permissions.")
                        navDirections =
                                DashboardFragmentDirections.actionDashboardFragmentToGalleryFragment()
                        checkPermissions.launch(FILE_REQUIRED_PERMISSIONS.toTypedArray())
                    }
                }
            R.id.settings_cardview -> navController.navigate(
                    DashboardFragmentDirections.actionDashboardFragmentToSettingsFragment())
            R.id.glossary_cardview -> navController.navigate(
                    DashboardFragmentDirections.actionDashboardFragmentToGlossaryFragment())
        }
    }

    override fun onPause() {
        super.onPause()
        navDirections = null
    }

    private fun navigateToFragment() {
        navDirections?.let { navController.navigate(it) }
    }

    private fun setPermissions() {

        checkPermissions =
            registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) {
                permissions ->
                if (permissions[Manifest.permission.CAMERA] == true ||
                    permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true){
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            if(permissions[Manifest.permission.ACCESS_MEDIA_LOCATION] == true) {
                                navigateToFragment()
                            } else {
                                requireContext().showDeniedDialog(
                                        "Access denied",
                                        "You can accept the permissions needed in the Setting page")
                            }
                        } else {
                            navigateToFragment()
                        }
                } else {
                    requireContext().showDeniedDialog(
                             "Access denied",
                            "You can accept the permissions needed in the Setting page")
                }
            }
    }

}