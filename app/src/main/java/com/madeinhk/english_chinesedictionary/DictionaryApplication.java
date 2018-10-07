package com.madeinhk.english_chinesedictionary;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.core.CrashlyticsCore;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.madeinhk.utils.DatabaseMigrationUtil;

import io.fabric.sdk.android.Fabric;

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
        DatabaseMigrationUtil.maybeMigrate(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setupCrashlytics();
        setupTracker();
    }

    private void setupCrashlytics() {
        CrashlyticsCore crashlytics =
                new CrashlyticsCore.Builder()
                        .disabled(BuildConfig.DEBUG)
                        .build();
        Fabric.with(this, crashlytics);
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
