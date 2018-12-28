package com.madeinhk.english_chinesedictionary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.madeinhk.english_chinesedictionary.service.DictionaryHeadService;
import com.madeinhk.model.ECDictionary;
import com.madeinhk.model.Word;
import com.madeinhk.utils.Analytics;
import com.madeinhk.utils.Stemmer;
import com.madeinhk.utils.StringUtils;

import java.nio.charset.Charset;

public class TranslateActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        processIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        processIntent(intent);
    }

    private void processIntent(Intent intent) {
        String action = intent.getAction();

        CharSequence text = null;
        if (Intent.ACTION_PROCESS_TEXT.equals(action)) {
            // Text shared with app via Intent
            text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
        }
        if ("android.intent.action.DEFINE".equals(action)) {
            text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
        }
        if (!TextUtils.isEmpty(text)) {
            processText(text);
        }
        finish();
    }

    private void processText(CharSequence text) {
        String query = text.toString();
        ECDictionary dictionary = new ECDictionary(TranslateActivity.this);
        String str = query.toLowerCase();
        Word word = dictionary.lookup(str);
        if (word == null && StringUtils.isEnglishWord(str)) {
            // Try to have stemming
            Stemmer stemmer = new Stemmer();
            stemmer.add(str.toCharArray(), str.length());
            stemmer.stem();
            word = dictionary.lookup(stemmer.toString());
        }
        if (word != null) {
            Analytics.trackFoundWord(this, word.mWord);
            DictionaryHeadService.show(this, word);
        } else {
            Analytics.trackNotFoundWord(this, query);
        }
    }
}
