package me.togaparty.notable_opencv.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import me.togaparty.notable_opencv.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_screen, rootKey);
    }
    companion object


}