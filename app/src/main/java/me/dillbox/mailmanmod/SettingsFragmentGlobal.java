package me.dillbox.mailmanmod;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragmentGlobal extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.global_preferences, rootKey);
    }
}