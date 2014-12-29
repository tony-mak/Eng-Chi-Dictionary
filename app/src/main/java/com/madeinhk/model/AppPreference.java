package com.madeinhk.model;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by tonymak on 29/12/14.
 */
public class AppPreference {
    private static final String PREF_NAME = "app_pref";

    private static SharedPreferences getSharedPreference(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private static final String KEY_LAST_WORD = "last_word";

    public static void saveLastWord(Context context, String word) {
        getSharedPreference(context).edit().putString(KEY_LAST_WORD, word).apply();
    }

    public static String getKeyLastWord(Context context) {
        return getSharedPreference(context).getString(KEY_LAST_WORD, null);
    }
}
