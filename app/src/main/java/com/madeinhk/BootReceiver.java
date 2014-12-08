package com.madeinhk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.madeinhk.english_chinesedictionary.service.ClipboardService;

/**
 * Created by tony on 8/11/14.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            ClipboardService.start(context);
        }
    }
}
