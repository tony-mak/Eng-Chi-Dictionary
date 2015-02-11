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
        return getSharedPreference(context).getString(KEY_LAST_WORD, "welcome");
    }


    private static final String KEY_SHOWED_TUTORIAL = "showed_tutorial";

    public static void saveShowedTutorial(Context context, boolean showed) {
        getSharedPreference(context).edit().putBoolean(KEY_SHOWED_TUTORIAL, showed).apply();
    }

    public static boolean getShowedTutorial(Context context) {
        return getSharedPreference(context).getBoolean(KEY_SHOWED_TUTORIAL, false);
    }
}
