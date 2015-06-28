/**
 * Created by tonymak on 10/9/14.
 */

package com.madeinhk.model;


import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.List;


public class DictionaryDatabaseHelper extends SQLiteAssetHelper {
    private static final String DATABASE_NAME = "dict.db";
    private static final int DATABASE_VERSION = 2;
    private FavouriteMigrationTool mFavouriteMigrationTool;
    private Context mContext;

    public DictionaryDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        setForcedUpgrade();
        mContext = context;
    }

    @Override
    protected void onPreReplaceDatabase(SQLiteDatabase db) {
        mFavouriteMigrationTool = new FavouriteMigrationTool();
        mFavouriteMigrationTool.exportFav(db);
    }

    @Override
    protected void onPostReplaceDatabase(SQLiteDatabase db) {
        mFavouriteMigrationTool.importFav(db);
    }

    static class FavouriteMigrationTool {

        private List<Favourite> mFavouriteList = new ArrayList<>();

        public void exportFav(SQLiteDatabase db) {
            Cursor c = db.query(Favourite.TABLE_NAME, null, null, null, null, null, null);
            try {
                c.getColumnIndex(Favourite.COLUMNS.WORD);
                while (c.moveToNext()) {
                    mFavouriteList.add(Favourite.fromCursor(c));
                }
            } finally {
                c.close();
            }
        }

        public void importFav(SQLiteDatabase db) {
            db.beginTransaction();
            try {
                for (Favourite fav : mFavouriteList) {
                    db.insert(Favourite.TABLE_NAME, null, fav.toContentValues());
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            mFavouriteList.clear();
        }
    }
}
