package com.madeinhk.english_chinesedictionary;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

/**
 * Created by tonymak on 5/7/16.
 */
public class SettingFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.settings);
    }
}
