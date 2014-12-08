/**
 * Created by tonymak on 10/9/14.
 */

package com.madeinhk.model;


import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;


public class DictionaryDatabaseHelper extends SQLiteAssetHelper {
    private static final String DATABASE_NAME = "dict.db";
    private static final int DATABASE_VERSION = 1;

    public DictionaryDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}
