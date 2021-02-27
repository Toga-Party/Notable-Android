package me.togaparty.notable_android

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import me.togaparty.notable_android.utils.ALL_REQUIRED_PERMISSIONS
import me.togaparty.notable_android.utils.FILE_REQUIRED_PERMISSIONS
import me.togaparty.notable_android.utils.permissionsGranted
import me.togaparty.notable_android.utils.toast


private const val TITLE_TAG = "settingsActivityTitle"

class SettingsActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, HeaderFragment())
                .commit()
        } else {
            title = savedInstanceState.getCharSequence(TITLE_TAG)
        }
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                setTitle(R.string.title_activity_settings)
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, title)
    }

    override fun onSupportNavigateUp(): Boolean {
        Log.d("Settings", "Support Navigate Up")
        if (supportFragmentManager.popBackStackImmediate()) {
            return true
        }
        return super.onSupportNavigateUp()
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {

        val args: Bundle = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader,
            pref.fragment
        )
        fragment.arguments = args
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings, fragment)
            .addToBackStack(null)
            .commit()

        title = pref.title
        return true
    }

    class HeaderFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.header_preferences, rootKey)
            preferenceScreen
                .findPreference<Preference>("support_preference")
                ?.onPreferenceClickListener = this
        }

        override fun onResume() {
            super.onResume()
            if (permissionsGranted(requireContext(), ALL_REQUIRED_PERMISSIONS)) {
                preferenceScreen
                    .findPreference<Preference>("permissions_header")
                    ?.isEnabled = false
            }
        }

        override fun onPreferenceClick(preference: Preference?): Boolean {
            val i = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://github.com/Mino-dev/Notable-OpenCV/issues")
            }
            requireActivity().startActivity(i)
            return true
        }


    }

    class PermissionsFragment : PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener {
        private lateinit var checkPermissions: ActivityResultLauncher<Array<String>>
        private var volatileKey: String? = null

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

            updatePreferences()
            setPermissions()
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
                            "Settings",
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

    class SyncFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.sync_preferences, rootKey)
        }
    }

    class DevelopersFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.developers, rootKey)
        }
    }

}