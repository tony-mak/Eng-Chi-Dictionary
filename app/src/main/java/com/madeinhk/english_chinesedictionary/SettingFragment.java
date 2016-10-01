package com.madeinhk.english_chinesedictionary;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.os.BuildCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.madeinhk.english_chinesedictionary.service.ECDictionaryService;

public class SettingFragment extends PreferenceFragmentCompat implements
        Preference.OnPreferenceChangeListener {
    public static final String KEY_COPY_TO_LOOKUP = "copy_lookup";
    public static final String KEY_QUICK_LOOKUP = "quick_lookup";

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.settings);
        findPreference(KEY_COPY_TO_LOOKUP).setOnPreferenceChangeListener(this);
        findPreference(KEY_QUICK_LOOKUP).setEnabled(BuildCompat.isAtLeastN());
        findPreference(KEY_QUICK_LOOKUP).setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        if (KEY_COPY_TO_LOOKUP.equals(preference.getKey())) {
            boolean newValue = (boolean) o;
            if (newValue) {
                ECDictionaryService.start(getActivity());
            }
            return true;
        } else if (KEY_QUICK_LOOKUP.equals(preference.getKey())) {
            Intent intent =
                    ECDictionaryService.getChangeForegroundIntent(getActivity(), (boolean) o);
            getActivity().startService(intent);
            return true;
        }
        return false;
    }
}
