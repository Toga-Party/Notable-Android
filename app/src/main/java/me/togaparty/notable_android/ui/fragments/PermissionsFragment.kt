package me.togaparty.notable_android.ui.fragments

import android.Manifest
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import me.togaparty.notable_android.utils.ALL_REQUIRED_PERMISSIONS
import me.togaparty.notable_android.utils.Constants.Companion.TAG
import me.togaparty.notable_android.utils.FILE_REQUIRED_PERMISSIONS
import me.togaparty.notable_android.utils.permissionsGranted
import me.togaparty.notable_android.utils.toast

class PermissionsFragment: PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var checkPermissions: ActivityResultLauncher<Array<String>>
    private var volatileKey: String? = null
    private lateinit var navController: NavController


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        updatePreferences()
        setPermissions()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = this.findNavController()
    }

    private fun updatePreferences() {
        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context).apply {
            val cameraPreference = SwitchPreferenceCompat(context).apply {
                key = "camera_permissions"
                title = "Camera Permissions"
                switchTextOff = "Allow this app to use the Camera"
                switchTextOff = "Camera permission is enabled"
            }

            if (permissionsGranted(requireContext(), listOf(Manifest.permission.CAMERA))) {
                cameraPreference.setDefaultValue(true)
                cameraPreference.isEnabled = false
            }
            val filePreference = SwitchPreferenceCompat(context).apply {
                key = "file_permissions"
                title = "File Permissions"
                switchTextOff = "Allow this app to save and load files"
                switchTextOn = "File permission is enabled"
            }
            if (permissionsGranted(requireContext(), FILE_REQUIRED_PERMISSIONS)) {
                filePreference.setDefaultValue(true)
                filePreference.isEnabled = false
            }
            addPreference(filePreference)
            addPreference(cameraPreference)
        }
        preferenceScreen = screen
        if (permissionsGranted(requireContext(), ALL_REQUIRED_PERMISSIONS)) {
            navController.navigate(PermissionsFragmentDirections.actionPermissionsFragmentPop())
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(
            sharedPreferences: SharedPreferences?,
            key: String?
    ) {
        when (key) {
            "camera_permissions" ->
                if (sharedPreferences != null) {
                    Log.i(
                            "Settings",
                            "Camera: ${sharedPreferences.getBoolean(key, false)}"
                    )
                    checkPermissions.launch(arrayOf(Manifest.permission.CAMERA))
                    volatileKey = key
                }
            "file_permissions" ->
                if (sharedPreferences != null) {
                    Log.i(
                            TAG,
                            "File: ${sharedPreferences.getBoolean(key, false)}"
                    )
                    checkPermissions.launch(
                            FILE_REQUIRED_PERMISSIONS.toTypedArray()
                    )
                    volatileKey = key
                }
        }
    }

    private fun setPermissions() {

        checkPermissions =
            registerForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->

                if (permissions[Manifest.permission.CAMERA] == true ||
                        permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (permissions[Manifest.permission.ACCESS_MEDIA_LOCATION] == false) {
                            toast("Permission denied")
                        }
                    } else {
                        if (permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == false) {
                            toast("Permission denied")
                        }
                    }

                } else {
                    toast("Permission denied")
                }
                preferenceScreen.sharedPreferences.edit().clear().apply()
                updatePreferences()
            }
    }

}