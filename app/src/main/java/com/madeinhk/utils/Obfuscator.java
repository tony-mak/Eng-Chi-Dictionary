package com.madeinhk.utils;

import android.content.Context;


/**
 * Created by tonymak on 8/12/14.
 */
public class Obfuscator {
    static {
        System.loadLibrary("obfuscator");
    }

    public static native void init(Context context);
}
