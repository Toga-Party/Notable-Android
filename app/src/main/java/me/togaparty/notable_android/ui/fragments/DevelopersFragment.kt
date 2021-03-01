package me.togaparty.notable_android.ui.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import me.togaparty.notable_android.R

class DevelopersFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.developers, rootKey)
    }
}