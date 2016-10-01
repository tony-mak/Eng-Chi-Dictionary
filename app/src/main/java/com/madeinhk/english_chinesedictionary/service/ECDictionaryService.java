package com.madeinhk.english_chinesedictionary.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.RemoteInput;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.madeinhk.utils.QuickLookupNotificationHelper;
import com.madeinhk.english_chinesedictionary.SettingFragment;
import com.madeinhk.model.ECDictionary;
import com.madeinhk.model.Word;
import com.madeinhk.utils.Stemmer;
import com.madeinhk.utils.StringUtils;

import org.jsoup.Jsoup;

public class ECDictionaryService extends Service {
    private static final String TAG = "ECDictionaryService";
    private boolean mRegistered = false;

    private String mLastWord;
    private long mTimestamp;

    private static int NOTIFICATION_ID = 1;

    public static final String KEY_TEXT_REPLY = "key_text_reply";
    public static final String ACTION_QUICK_LOOKUP = "quick_lookup";
    public static final String KEY_IS_FOREGROUND = "key_is_foreground";
    public static final String ACTION_CHANGE_FOREGROUND = "change_foreground";

    private QuickLookupNotificationHelper mQuickLookHelper;

    public ECDictionaryService() {
    }

    public void onCreate() {
        Log.d(TAG, "onCreate: " + mRegistered);
        if (isQuickLookupEnabled()) {
            mQuickLookHelper = new QuickLookupNotificationHelper(this);
            startForeground();
        }
        synchronized (mClipListener) {
            if (!mRegistered) {
                ClipboardManager clipboardManager =
                        (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.addPrimaryClipChangedListener(mClipListener);
                mRegistered = true;
            }
        }
    }

    private void startForeground() {
        Notification notification = mQuickLookHelper.buildInitialNotification();
        startForeground(NOTIFICATION_ID, notification);
    }

    public static void start(Context context) {
        context.startService(
                new Intent(context.getApplicationContext(), ECDictionaryService.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (isQuickLookupEnabled() && ACTION_QUICK_LOOKUP.equals(intent.getAction())) {
                String text = getMessageText(intent);
                if (text != null) {
                    text = text.trim();
                }
                Word word = lookupWord(text);
                Notification notification = mQuickLookHelper.buildNotificationForResult(word);
                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(NOTIFICATION_ID, notification);
            } else if (ACTION_CHANGE_FOREGROUND.equals(intent.getAction())) {
                boolean isForeground = intent.getBooleanExtra(KEY_IS_FOREGROUND, true);
                if (isForeground) {
                    startForeground();
                } else {
                    stopForeground(true);
                    stopSelf();
                }

            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw null;
    }

    ClipboardManager.OnPrimaryClipChangedListener mClipListener =
            new ClipboardManager.OnPrimaryClipChangedListener() {
                @Override
                public void onPrimaryClipChanged() {
                    if (!isCopyToLookupEnabled()) {
                        return;
                    }

                    final ClipboardManager clipboard =
                            (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData data = clipboard.getPrimaryClip();
                    if (data == null) {
                        return;
                    }

                    String text = extractTextFromClipData(data);
                    if (text != null) {
                        text = text.trim();
                    }
                    if (TextUtils.isEmpty(text) || isDuplicated(text)) {
                        return;
                    }
                    mLastWord = text;
                    mTimestamp = System.currentTimeMillis();
                    Word word = lookupWord(text);
                    if (word != null) {
                        DictionaryHeadService.show(ECDictionaryService.this, word);
                    }

                }

                // To tackle with the funny behaviour of clipboard listener
                private boolean isDuplicated(String text) {
                    return text.equals(mLastWord) && System.currentTimeMillis() - mTimestamp < 1000;
                }

                // It seems that the clipboard api is quite buggy, defensive coding here
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
                    } else if ("text/html".equals(mimeType) &&
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        String html = item.getHtmlText();
                        return Jsoup.parse(html).text();
                    }
                    return null;
                }
            };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRegistered) {
            ClipboardManager clipboardManager =
                    (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.removePrimaryClipChangedListener(mClipListener);
        }
    }

    private Word lookupWord(String text) {
        if (TextUtils.isEmpty(text)) {
            return null;
        }
        ECDictionary dictionary = new ECDictionary(ECDictionaryService.this);
        String str = text.toLowerCase();
        Word word = dictionary.lookup(str);
        if (word != null) {
            return word;
        }
        if (StringUtils.isEnglishWord(str)) {
            // Try to have stemming
            Stemmer stemmer = new Stemmer();
            stemmer.add(str.toCharArray(), str.length());
            stemmer.stem();
            return dictionary.lookup(stemmer.toString());
        }
        return null;
    }

    private boolean isCopyToLookupEnabled() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getBoolean(SettingFragment.KEY_COPY_TO_LOOKUP, true);
    }

    private boolean isQuickLookupEnabled() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getBoolean(SettingFragment.KEY_QUICK_LOOKUP, true);
    }

    private String getMessageText(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(KEY_TEXT_REPLY).toString();
        }
        return null;
    }

    public static Intent getQuickLookupIntent(Context context) {
        return new Intent(context, ECDictionaryService.class).setAction(ACTION_QUICK_LOOKUP);
    }

    public static Intent getChangeForegroundIntent(Context context, boolean isForeground) {
        Intent intent = new Intent(context, ECDictionaryService.class).
                setAction(ACTION_CHANGE_FOREGROUND);
        intent.putExtra(KEY_IS_FOREGROUND, isForeground);
        return intent;
    }
}
