package com.madeinhk.utils;

import android.content.Context;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.madeinhk.english_chinesedictionary.DictionaryApplication;

/**
 * Created by tonymak on 29/12/14.
 */
public class Analytics {

    private static Tracker getTracker(Context context) {
        DictionaryApplication application = (DictionaryApplication) context.getApplicationContext();
        return application.getTracker();
    }

    private static void track(Tracker tracker, String screenName) {
        tracker.setScreenName(screenName);
        tracker.send(new HitBuilders.AppViewBuilder().build());
    }

    public static void trackAppLaunch(Context context) {
        Tracker tracker = getTracker(context);
        track(tracker, "app_launch");
    }
}
