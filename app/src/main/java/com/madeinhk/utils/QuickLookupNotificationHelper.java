package com.madeinhk.utils;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;

import com.madeinhk.english_chinesedictionary.DictionaryActivity;
import com.madeinhk.english_chinesedictionary.R;
import com.madeinhk.english_chinesedictionary.service.ECDictionaryService;
import com.madeinhk.model.Word;

public class QuickLookupNotificationHelper {
    private Context mContext;

    public QuickLookupNotificationHelper(
            Context context) {
        mContext = context;
    }

    public Notification buildInitialNotification() {
        return buildNotification(false, null);
    }

    public Notification buildNotificationForResult(Word result) {
        return buildNotification(true, result);
    }

    @TargetApi(Build.VERSION_CODES.N)
    private Notification buildNotification(boolean showResult, Word result) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        builder.setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(mContext.getString(R.string.quick_lookup_notification_title))
                .setOngoing(true)
                .setDefaults(0)  // please be quiet
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setColor(mContext.getColor(R.color.colorPrimaryDark));
        if (showResult) {
            String response = mContext.getString(R.string.quick_lookup_notification_not_found);
            if (result != null) {
                response = result.buildMeaningSummary();
            }
            response = String.format("%s %s", result.mWord, response);

            builder.setRemoteInputHistory(new String[]{response});
        }
        String replyLabel = mContext.getString(R.string.quick_lookup_notification_reply_label);
        RemoteInput remoteInput = new RemoteInput.Builder(ECDictionaryService.KEY_TEXT_REPLY)
                .setLabel(replyLabel)
                .build();
        PendingIntent lookupIntent = PendingIntent.getService(mContext, 1, ECDictionaryService
                .getQuickLookupIntent(mContext), PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action lookupAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_search,
                        mContext.getString(R.string.quick_lookup_notification_action),
                        lookupIntent)
                        .addRemoteInput(remoteInput)
                        .build();
        builder.addAction(lookupAction);
        PendingIntent settingsIntent = PendingIntent.getActivity(mContext, 2, getSettingsIntent()
                , PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action settingsAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_settings_grey600_24dp,
                        mContext.getString(R.string.action_settings), settingsIntent)
                        .build();
        builder.addAction(settingsAction);
        return builder.build();
    }

    private Intent getSettingsIntent() {
        Intent intent = new Intent(mContext, DictionaryActivity.class);
        intent.setAction(DictionaryActivity.ACTION_SETTINGS);
        return intent;
    }
}
