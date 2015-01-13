package com.madeinhk.model;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

/**
 * Created by tonymak on 10/9/14.
 */
public class DictionaryContentProvider extends ContentProvider {
    private DictionaryDatabaseHelper mDbHelper;
    public static final String AUTHORITY = "com.madeinhk.dictionary";


    private static final int BY_ID = 1;
    private static final int BY_WORD = 2;
    private static final int WORD_SUGGESTION = 3;
    private static final int FAVOURITE = 4;
    private static final int FAVOURITE_WORDS = 5;
    private static final UriMatcher sUriMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);


    static {
        sUriMatcher.addURI(AUTHORITY, "by_word/*", BY_WORD);
        sUriMatcher.addURI(AUTHORITY, "by_id/#", BY_ID);
        sUriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, WORD_SUGGESTION);
        sUriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", WORD_SUGGESTION);
        sUriMatcher.addURI(AUTHORITY, "favourite", FAVOURITE);
        sUriMatcher.addURI(AUTHORITY, "favourite_words", FAVOURITE_WORDS);
    }

    private static final SparseArray<String> TABLE_NAMES;

    static {
        SparseArray<String> array = new SparseArray<String>(11);
        array.put(BY_WORD, ECDictionary.TABLE_NAME);
        array.put(BY_ID, ECDictionary.TABLE_NAME);
        array.put(WORD_SUGGESTION, ECDictionary.TABLE_NAME);
        array.put(FAVOURITE, Favourite.TABLE_NAME);
        array.put(FAVOURITE_WORDS, "favourite_word");
        TABLE_NAMES = array;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new DictionaryDatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int code = sUriMatcher.match(uri);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String table = TABLE_NAMES.get(code);

        switch (code) {
            case BY_WORD:
                String word = uri.getLastPathSegment();
                return db.query(table, projection, ECDictionary.COLUMNS.WORD + "=?", new String[]{word}, null, null, sortOrder);
            case BY_ID:
                String id = uri.getLastPathSegment();
                return db.query(table, projection, ECDictionary.COLUMNS._ID + "=?", new String[]{id}, null, null, sortOrder);
            case FAVOURITE:
                return db.query(table, projection, selection, selectionArgs, null, null, sortOrder);
            case WORD_SUGGESTION:
                String limit = uri.getQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT);
                if (TextUtils.isEmpty(limit)) {
                    limit = "5";
                }
                Cursor cursor = db.query(table, new String[]{"_id", "word", "meaning"},
                        "word LIKE ?", new String[]{selectionArgs[0] +
                                "%"},
                        null, null,
                        sortOrder, limit);
                MatrixCursor mc = new MatrixCursor(new String[]{"_id", "suggest_intent_data_id", "suggest_text_1", "suggest_text_2"});
                try {
                    while (cursor.moveToNext()) {
                        String strippedMeaning = cursor.getString(2).replaceAll("&&[0-9a-z]", "");
                        mc.addRow(new Object[]{cursor.getString(0), cursor.getString(0), cursor.getString(1), strippedMeaning});
                    }
                } finally {
                    cursor.close();
                }
                return mc;
            case FAVOURITE_WORDS:
                return db.query(table, projection, selection, selectionArgs, null, null, sortOrder);
        }
        throw new IllegalArgumentException("Do not support uri: " + uri);
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        int code = sUriMatcher.match(uri);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String table = TABLE_NAMES.get(code);
        switch (code) {
            case FAVOURITE:
                long id = db.insertWithOnConflict(table, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
                if (id != -1) {
                    Uri ret = Favourite.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
                    return ret;
                }
        }
        throw new IllegalArgumentException("Do not support uri: " + uri);
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        int code = sUriMatcher.match(uri);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String table = TABLE_NAMES.get(code);
        switch (code) {
            case FAVOURITE:
                return db.delete(table, s, strings);
        }
        throw new IllegalArgumentException("Do not support uri: " + uri);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
