package com.madeinhk.english_chinesedictionary;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.madeinhk.utils.Obfuscator;

/**
 * Created by tonymak on 8/12/14.
 */
public class DictionaryApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Obfuscator.init(base);
    }
}
