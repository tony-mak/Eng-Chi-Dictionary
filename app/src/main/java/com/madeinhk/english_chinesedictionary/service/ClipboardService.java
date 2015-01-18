package com.madeinhk.english_chinesedictionary.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.madeinhk.model.ECDictionary;
import com.madeinhk.model.Word;
import com.madeinhk.utils.Stemmer;

import org.jsoup.Jsoup;

public class ClipboardService extends Service {
    private boolean mRegistered = false;

    private String mLastWord;
    private long mTimestamp;

    public ClipboardService() {
    }

    public void onCreate() {
        synchronized (mClipListener) {
            if (!mRegistered) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.addPrimaryClipChangedListener(mClipListener);
                mRegistered = true;
            }
        }
    }

    public static void start(Context context) {
        context.startService(new Intent(context, ClipboardService.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    ClipboardManager.OnPrimaryClipChangedListener mClipListener = new ClipboardManager.OnPrimaryClipChangedListener() {
        @Override
        public void onPrimaryClipChanged() {
            final ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData data = clipboard.getPrimaryClip();
            if (data == null) {
                return;
            }

            String text = extractTextFromClipData(data);
            if (!TextUtils.isEmpty(text) && !isDuplicated(text)) {
                ECDictionary dictionary = new ECDictionary(ClipboardService.this);
                String str = text.toLowerCase().trim();
                mLastWord = str;
                mTimestamp = System.currentTimeMillis();
                Word word = dictionary.lookup(str);
                if (word == null && isEnglishWord(str)) {
                    // Try to have stemming
                    Stemmer stemmer = new Stemmer();
                    stemmer.add(str.toCharArray(), str.length());
                    stemmer.stem();
                    word = dictionary.lookup(stemmer.toString());
                }
                if (word != null) {
                    showToast(word);
                }
            }
        }

        // To tackle with the funny behaviour of clipboard listener
        private boolean isDuplicated(String text) {
            return text.equals(mLastWord) && System.currentTimeMillis() - mTimestamp < 1000;
        }

        // It seems that the clipboard api is quite buggy, defensive coding here
        @SuppressLint("NewApi")
        private String extractTextFromClipData(ClipData clipData) {
            if (clipData == null) {
                return null;
            }

            ClipData.Item item = clipData.getItemAt(0);
            if (item == null) {
                return null;
            }

            ClipDescription description = clipData.getDescription();
            if (description == null) {
                return null;
            }
            String mimeType = description.getMimeType(0);
            if ("text/plain".equals(mimeType)) {
                CharSequence text = item.getText();
                if (text != null) {
                    return text.toString();
                }
            } else if ("text/html".equals(mimeType) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                String html = item.getHtmlText();
                return Jsoup.parse(html).text();
            }
            return null;
        }

        private void showToast(Word word) {
            startService(DictionaryHeadService.createIntent(ClipboardService.this, word));
        }

        private boolean isEnglishWord(String text) {
            char[] chars = text.toCharArray();
            boolean isEnglish = true;
            for (int i = 0; i < chars.length; i++) {
                if (!Character.isLetter(chars[i])) {
                    isEnglish = false;
                    break;
                }
            }
            return isEnglish;
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRegistered) {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.removePrimaryClipChangedListener(mClipListener);
        }
    }
}
