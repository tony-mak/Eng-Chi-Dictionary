package com.madeinhk.english_chinesedictionary.service;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.madeinhk.model.ECDictionary;
import com.madeinhk.model.Word;

public class ClipboardService extends Service {
    private boolean mRegistered = false;

    public ClipboardService() {
    }

    public void onCreate() {
    }

    public static void start(Context context) {
        context.startService(new Intent(context, ClipboardService.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        synchronized (mClipListener) {
            if (!mRegistered) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.addPrimaryClipChangedListener(mClipListener);
                mRegistered = true;
            }
        }
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
            ClipData.Item item = data.getItemAt(0);
            if (item == null) {
                return;
            }
            CharSequence text = item.getText();
            if (text != null) {
                ECDictionary dictionary = new ECDictionary(ClipboardService.this);
                Word word = dictionary.lookup(text.toString());
                if (word != null) {
                    Toast.makeText(ClipboardService.this, word.mTypeEntry.get(0).mMeaning, Toast.LENGTH_LONG).show();
                }
            }
        }
    };


}
