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
import android.util.SparseArray;

import com.madeinhk.utils.ArrayUtils;
import com.madeinhk.utils.EditDistanceCalculator;
import com.madeinhk.utils.SimilarWordGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        int code = sUriMatcher.match(uri);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String table = TABLE_NAMES.get(code);

        switch (code) {
            case BY_WORD: {

                String word = uri.getLastPathSegment();
                return db.query(table, projection, ECDictionary.COLUMNS.WORD + "=?",
                        new String[]{word}, null, null, sortOrder);
            }
            case BY_ID: {
                String id = uri.getLastPathSegment();
                return db
                        .query(table, projection, ECDictionary.COLUMNS._ID + "=?", new String[]{id},
                                null, null, sortOrder);
            }
            case FAVOURITE: {
                return db.query(table, projection, selection, selectionArgs, null, null, sortOrder);
            }
            case WORD_SUGGESTION: {
                // Ignore the limit parameter and hardcode to be five.
                String query = selectionArgs[0];
                MatrixCursor mc = new MatrixCursor(
                        new String[]{"_id", "suggest_intent_data_id", "suggest_text_1",
                                "suggest_text_2"});
                if (TextUtils.isEmpty(query)) {
                    return mc;
                }
                SimilarWordGenerator generator = new SimilarWordGenerator();
                Set<String> stringList = generator.generate(query);
                String similarWordQuery = QueryBuilderUtil.buildWhereClauseWithIn("word",
                        stringList.size());
                String[] likeArgs = new String[]{query + "%"};
                String[] args = ArrayUtils.concatenate(likeArgs, stringList.toArray(new String[0]));

                final String limit = "20";
                Cursor cursor = db.query(table, new String[]{"_id", "word", "meaning"},
                        "word LIKE ? OR " + similarWordQuery, args, null, null, sortOrder, limit);

                try {
                    List<Suggestion> suggestions = new ArrayList<>();
                    EditDistanceCalculator calculator = new EditDistanceCalculator();
                    while (cursor.moveToNext()) {
                        String meaningString = cursor.getString(2);
                        if (!TextUtils.isEmpty(meaningString)) {
                            String id = cursor.getString(0);
                            String word = cursor.getString(1);
                            String strippedMeaning = meaningString.split("\\|")[1];
                            int editDistance = calculator.getEditDistance(query, word);
                            Suggestion suggestion =
                                    new Suggestion(id, word, strippedMeaning, editDistance);
                            suggestions.add(suggestion);
                        }
                    }
                    Collections.sort(suggestions);
                    for (Suggestion suggestion : suggestions) {
                        mc.addRow(new Object[]{suggestion.id, suggestion.id, suggestion.word,
                                suggestion.meaning});
                        if (mc.getCount() >= 10) {
                            break;
                        }
                    }
                } finally {
                    cursor.close();
                }
                return mc;
            }
            case FAVOURITE_WORDS: {
                return db.query(table, projection, selection, selectionArgs, null, null, sortOrder);
            }
        }
        throw new IllegalArgumentException("Do not support uri: " + uri);
    }

    static class Suggestion implements Comparable<Suggestion> {
        String id;
        String word;
        String meaning;
        int editDistance;

        public Suggestion(String id, String word, String meaning, int editDistance) {
            this.id = id;
            this.word = word;
            this.meaning = meaning;
            this.editDistance = editDistance;
        }

        @Override
        public int compareTo(Suggestion another) {
            return this.editDistance - another.editDistance;
        }

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
                long id = db.insertWithOnConflict(table, null, contentValues,
                        SQLiteDatabase.CONFLICT_REPLACE);
                if (id != -1) {
                    Uri ret = Favourite.CONTENT_URI.buildUpon().appendPath(String.valueOf(id))
                            .build();
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


    private static class QueryBuilderUtil {
        public static String buildWhereClauseWithIn(String column, int numberOfParameters) {
            StringBuilder sb = new StringBuilder();
            sb.append(column);
            sb.append(" IN (");
            boolean firstItem = true;
            for (int i = 0; i < numberOfParameters; i++) {
                if (!firstItem) {
                    sb.append(",");
                } else {
                    firstItem = false;
                }
                sb.append("?");
            }
            sb.append(")");
            return sb.toString();
        }
    }

}
