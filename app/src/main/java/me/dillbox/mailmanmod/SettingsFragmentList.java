package me.dillbox.mailmanmod;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

public class SettingsFragmentList extends PreferenceFragmentCompat {

    private String mList;

    private static final String ARG_LIST_NAME = "list_name";

    public static SettingsFragmentList newInstance(String list) {
        SettingsFragmentList fragment = new SettingsFragmentList();
        Log.i("skeet", "newInstance: "+list);
        Bundle bundle = new Bundle();
        bundle.putString(ARG_LIST_NAME, list);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.empty_preferences, rootKey);

        if (getArguments() != null) {
            mList = getArguments().getString(ARG_LIST_NAME);
        } else {
            Log.i("skeet", "onCreatePreferences: well fuck");
            return;
        }

        PreferenceScreen ps = this.getPreferenceScreen();

        Preference pref = new EditTextPreference(ps.getContext());
        pref.setKey(mList+"_display_name");
        pref.setTitle(R.string.display_name);
        ((EditTextPreference)pref).setOnBindEditTextListener(
                new EditTextPreference.OnBindEditTextListener() {
                    @Override
                    public void onBindEditText(@NonNull EditText editText) {
                        editText.setInputType(InputType.TYPE_CLASS_TEXT);
                    }
                }
        );
        ((EditTextPreference) pref).setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
        ps.addPreference(pref);

        pref = new EditTextPreference(ps.getContext());
        pref.setKey(mList+"_url");
        pref.setTitle(R.string.url);
        ((EditTextPreference) pref).setOnBindEditTextListener(
                new EditTextPreference.OnBindEditTextListener() {
                    @Override
                    public void onBindEditText(@NonNull EditText editText) {
                        editText.setInputType(InputType.TYPE_CLASS_TEXT);
                    }
                }
        );
        ((EditTextPreference) pref).setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
        ps.addPreference(pref);

        pref = new EditTextPreference(ps.getContext());
        pref.setKey(mList+"_password");
        pref.setTitle(R.string.password);
        ((EditTextPreference) pref).setOnBindEditTextListener(
                new EditTextPreference.OnBindEditTextListener() {
                    @Override
                    public void onBindEditText(@NonNull EditText editText) {
                        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    }
                }
        );
        ps.addPreference(pref);

        pref = new ListPreference(ps.getContext());
        pref.setKey(mList+"_interval");
        pref.setTitle(R.string.interval);
        ((ListPreference) pref).setEntries(R.array.interval_entries);
        ((ListPreference) pref).setEntryValues(R.array.interval_values);
        ((ListPreference) pref).setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
        ps.addPreference(pref);
    }
}