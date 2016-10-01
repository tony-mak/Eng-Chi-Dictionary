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
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public static void trackAppLaunch(Context context) {
        Tracker tracker = getTracker(context);
        track(tracker, "app_launch");
    }

    public static void trackPopup(Context context) {
        Tracker tracker = getTracker(context);
        track(tracker, "popup");
    }

    public static void trackFoundWord(Context context, String word) {
        Tracker tracker = getTracker(context);
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("lookup")
                .setAction("found")
                .setLabel(word)
                .build());
    }

    public static void trackNotFoundWord(Context context, String word) {
        Tracker tracker = getTracker(context);
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("lookup")
                .setAction("not_found")
                .setLabel(word)
                .build());
    }
}
