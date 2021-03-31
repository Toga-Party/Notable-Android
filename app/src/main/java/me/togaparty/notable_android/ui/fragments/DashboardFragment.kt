package me.togaparty.notable_android.ui.fragments

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import me.togaparty.notable_android.R
import me.togaparty.notable_android.data.ImageListProvider
import me.togaparty.notable_android.databinding.FragmentDashboardBinding
import me.togaparty.notable_android.utils.*
import me.togaparty.notable_android.utils.Constants.Companion.TAG


class DashboardFragment : Fragment(R.layout.fragment_dashboard), View.OnClickListener {

    private val binding by viewBinding(FragmentDashboardBinding::bind)

    private lateinit var navController: NavController
    private lateinit var checkPermissions: ActivityResultLauncher<Array<String>>

    private var navDirections: NavDirections? = null
    private lateinit var model: ImageListProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(permissionsGranted(requireContext(), FILE_REQUIRED_PERMISSIONS)){
            model = ViewModelProvider(requireActivity()).get(ImageListProvider::class.java)
            model.getList().observe(viewLifecycleOwner, {
                activity?.let {
                    when(model.getProcessingStatus()) {
                        Status.FAILED -> toast("Upload failed")
                        Status.SUCCESSFUL-> toast("Upload successful")
                        else -> Unit
                    }.also {
                        model.setProcessingStatus(Status.AVAILABLE)
                    }
                }
            })
        }
        setPermissions()
        navController = this.findNavController()
        binding.cameraCardview.setOnClickListener(this)
        binding.filesCardview.setOnClickListener(this)
        binding.glossaryCardview.setOnClickListener(this)
        binding.settingsCardview.setOnClickListener(this)
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
                            navDirections =
                                    DashboardFragmentDirections.actionDashboardFragmentToGalleryFragment()
                            checkPermissions.launch(FILE_REQUIRED_PERMISSIONS.toTypedArray())
                        }
                    }
                    else -> {
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
                    permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true||
                    permissions[Manifest.permission.ACCESS_MEDIA_LOCATION] == true){
                        navigateToFragment()

                } else {
                    requireContext().showDeniedDialog(
                             "Access denied",
                            "You can accept the permissions needed in the Setting page")
                }
            }
    }

}