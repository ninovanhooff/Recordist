package com.ninovanhooff.recordist.presentation.recording

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.ninovanhooff.recordist.R

class RecordingPreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.recording_preferences, rootKey)
    }
}