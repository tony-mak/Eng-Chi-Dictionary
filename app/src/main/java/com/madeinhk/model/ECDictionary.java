package com.madeinhk.model;

import android.content.ContentProviderClient;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tonymak on 10/16/14.
 */
public class ECDictionary {
    public static final String TABLE_NAME = "ecdict";
    private static final Uri sBaseUri = Uri.parse("content://" + DictionaryContentProvider.AUTHORITY);
    private static final Uri sEngToChiUri = sBaseUri.buildUpon().appendPath("by_word").build();
    private static final Uri sWordFromIdUri = sBaseUri.buildUpon().appendPath("by_id").build();
    private static final Uri sAutoCompleteUri = sBaseUri.buildUpon().appendPath("autocomplete").build();
    private static final Uri FAVOURITE_WORD_URI = sBaseUri.buildUpon().appendPath("favourite_words").build();

    private Context mContext;
    private ContentProviderClient mProviderClient;

    public ECDictionary(Context context) {
        mContext = context.getApplicationContext();
        mProviderClient = mContext.getContentResolver().acquireContentProviderClient(DictionaryContentProvider.AUTHORITY);
    }

    public static interface COLUMNS {
        public static final String _ID = BaseColumns._ID;
        public static final String WORD = "word";
        public static final String MEANING = "meaning";
        public static final String PHONETIC_STRING = "phonetic_string";
        public static final String EXAMPLE = "example";
    }

    public static String[] ALL_COLUMNS = {COLUMNS._ID, COLUMNS.WORD, COLUMNS.MEANING, COLUMNS.PHONETIC_STRING, COLUMNS.EXAMPLE};

    public Word lookupFromId(String id) {
        Word word = null;
        try {
            Uri uri = sWordFromIdUri.buildUpon().appendPath(id).build();
            Cursor cursor = mProviderClient.query(uri, null, null, null, null);
            boolean valid = cursor.moveToFirst();
            if (!valid) {
                return null;
            }
            word = fromCursor(cursor);
            cursor.close();
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
        return word;
    }

    private Word fromCursor(Cursor cursor) {
        String dictWord = cursor.getString(1);
        String meaning = cursor.getString(2);
        String phoneticString = cursor.getString(3);
        String example = cursor.getString(4);
        return Word.fromLookupResult(new LookupResult(dictWord, meaning, phoneticString, example));
    }

    public Word lookup(String word) {
        Word wordRet = null;
        try {
            Uri uri = sEngToChiUri.buildUpon().appendPath(word.toLowerCase()).build();
            Cursor cursor = mProviderClient.query(uri, null, null, null, null);
            boolean valid = cursor.moveToFirst();
            if (!valid) {
                return null;
            }
            wordRet = fromCursor(cursor);
            cursor.close();
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
        return wordRet;
    }

    public List<Word> getAllFavouriteWords() {
        Cursor cursor = mContext.getContentResolver().query(FAVOURITE_WORD_URI, null, null, null, null);
        List<Word> wordList = new ArrayList<Word>();
        while (cursor.moveToNext()) {
            wordList.add(fromCursor(cursor));
        }
        cursor.close();
        return wordList;
    }

}
