package com.madeinhk.english_chinesedictionary;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.madeinhk.english_chinesedictionary.service.ClipboardService;

public class SettingFragment extends PreferenceFragmentCompat implements
        Preference.OnPreferenceChangeListener {
    public static final String KEY_COPY_TO_LOOKUP = "copy_lookup";
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.settings);
        findPreference(KEY_COPY_TO_LOOKUP).setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        if (KEY_COPY_TO_LOOKUP.equals(preference.getKey())) {
            boolean newValue = (boolean) o;
            if (newValue) {
                ClipboardService.start(getActivity());
            } else {

            }
            return true;
        }
        return false;
    }
}
