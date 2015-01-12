package com.madeinhk.english_chinesedictionary;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.ExceptionReporter;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.madeinhk.utils.Obfuscator;

/**
 * Created by tonymak on 8/12/14.
 */
public class DictionaryApplication extends Application {
    private Tracker mTracker;
    private static final String DEV_GA = "UA-51112217-4";
    private static final String GA = "UA-51112217-3";


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Obfuscator.init(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setupCrashlytics();
        setupTracker();
    }

    private void setupCrashlytics() {
        if (!BuildConfig.DEBUG) {
            Crashlytics.start(this);
        }
    }

    private void setupTracker() {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        mTracker = analytics.newTracker((BuildConfig.DEBUG) ? DEV_GA : GA);
        mTracker.enableAdvertisingIdCollection(true);
    }

    public Tracker getTracker() {
        return mTracker;
    }
}
