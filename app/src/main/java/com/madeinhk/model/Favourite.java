package com.madeinhk.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by tonymak on 24/11/14.
 */
public class Favourite {
    private static final Uri sBaseUri = Uri.parse("content://" + DictionaryContentProvider.AUTHORITY);
    public static final Uri CONTENT_URI = sBaseUri.buildUpon().appendPath("favourite").build();
    private static final int NOT_SAVED = -1;
    public int mId = NOT_SAVED;
    public String mWord;

    public static final String TABLE_NAME = "favourite";

    public static interface COLUMNS {
        public static final String ID = "_id";
        public static final String WORD = "word";
    }


    private Favourite(int id, String word) {
        mId = id;
        mWord = word;
    }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(COLUMNS.WORD, mWord);
        return cv;
    }

    public Uri save(Context context) {
        return context.getContentResolver().insert(CONTENT_URI, toContentValues());
    }

    public boolean delete(Context context) {
        return context.getContentResolver().delete(CONTENT_URI, COLUMNS.WORD + "=?", new String[]{mWord}) == 1;
    }

    public boolean isExists(Context context) {
        Cursor cursor = context.getContentResolver().query(CONTENT_URI, new String[]{COLUMNS.ID}, COLUMNS.WORD + "=?", new String[]{mWord}, null, null);
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }


    public static Favourite fromWord(Word word) {
        return new Favourite(NOT_SAVED, word.mWord);
    }


}
