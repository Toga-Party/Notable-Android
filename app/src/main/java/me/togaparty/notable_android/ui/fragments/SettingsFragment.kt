package me.togaparty.notable_android.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import me.togaparty.notable_android.R
import me.togaparty.notable_android.utils.ALL_REQUIRED_PERMISSIONS
import me.togaparty.notable_android.utils.Constants.Companion.GITHUB
import me.togaparty.notable_android.utils.Constants.Companion.TAG
import me.togaparty.notable_android.utils.permissionsGranted

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener {
    private lateinit var navController: NavController
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.header_preferences, rootKey)
        preferenceScreen
                .findPreference<Preference>("support_preference")
                ?.onPreferenceClickListener = this
        preferenceScreen
                .findPreference<Preference>("developer_header")
                ?.onPreferenceClickListener = this
        preferenceScreen
                .findPreference<Preference>("permissions_header")
                ?.onPreferenceClickListener = this
        preferenceScreen
                .findPreference<Preference>("sync_header")
                ?.onPreferenceClickListener = this
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = this.findNavController()
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
//        val i = Intent(Intent.ACTION_VIEW).apply {
//            data = Uri.parse(GITHUB)
//        }
//        requireActivity().startActivity(i)
        if(preference != null) {
            when(preference.key) {
                "sync_header" -> Log.d(TAG, "Sync preference is called")
                "permissions_header" -> navController.navigate(SettingsFragmentDirections.actionSettingsFragmentToPermissionsFragment())
                "support_preference" -> {
                    val i = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(GITHUB)
                    }
                    requireActivity().startActivity(i)
                }
                "developer_header" -> navController.navigate(SettingsFragmentDirections.actionSettingsFragmentToDevelopersFragment())
            }
        }




        return true
    }
}


