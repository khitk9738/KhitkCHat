package com.akki.khitkchat.ui.fragment

import android.os.Bundle
import android.preference.PreferenceFragment
import com.akki.khitkchat.R

class SettingsFragment : PreferenceFragment() {

    override fun onCreate(paramBundle: Bundle?) {
        super.onCreate(paramBundle)
        addPreferencesFromResource(R.xml.preferences)
    }
}
