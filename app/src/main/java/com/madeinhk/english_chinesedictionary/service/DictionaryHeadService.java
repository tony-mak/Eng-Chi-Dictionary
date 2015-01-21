package com.madeinhk.english_chinesedictionary.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.madeinhk.english_chinesedictionary.DictionaryActivity;
import com.madeinhk.english_chinesedictionary.R;
import com.madeinhk.model.Favourite;
import com.madeinhk.model.Word;

public class DictionaryHeadService extends Service {

    private WindowManager mWindowManager;
    private View mDictionaryHead;
    private TextView mTextView;
    private Word mWord;
    private ToggleButton mFavButton;
    private Handler mHandler;
    private static final String KEY_WORD = "key_word";

    @Override
    public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        mWord = intent.getParcelableExtra(KEY_WORD);
        if (mDictionaryHead == null) {
            LayoutInflater inflate = (LayoutInflater)
                    this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mDictionaryHead = inflate.inflate(R.layout.dictionary_head, null);
            mTextView = (TextView) mDictionaryHead.findViewById(R.id.message);
            mFavButton = (ToggleButton) mDictionaryHead.findViewById(R.id.fav_button);

            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            params.windowAnimations = android.R.style.Animation_Toast;
            params.type = WindowManager.LayoutParams.TYPE_TOAST;
            params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            params.x = 0;
            params.y = 100;

            mWindowManager.addView(mDictionaryHead, params);

            mDictionaryHead.setOnTouchListener(new View.OnTouchListener() {
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    restartDismissTimer();
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = params.x;
                            initialY = params.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            return true;
                        case MotionEvent.ACTION_UP:
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            params.x = initialX + (int) (event.getRawX() - initialTouchX);
                            params.y = initialY - (int) (event.getRawY() - initialTouchY);
                            mWindowManager.updateViewLayout(mDictionaryHead, params);
                            return true;
                    }
                    return false;
                }
            });
        }

        mTextView.setText(mWord.mTypeEntry.get(0).mMeaning);
        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = DictionaryActivity.getIntent(DictionaryHeadService.this, mWord.mWord);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                mHandler.removeCallbacksAndMessages(null);
                stopSelf();
            }
        });

        final Favourite favourite = Favourite.fromWord(mWord);
        boolean alreadyMarked = favourite.isExists(this);
        mFavButton.setChecked(alreadyMarked);

        mFavButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    favourite.save(DictionaryHeadService.this);
                } else {
                    favourite.delete(DictionaryHeadService.this);
                }
            }
        });
        restartDismissTimer();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeView();
    }

    private void restartDismissTimer() {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopSelf();
            }
        }, 5000);
    }

    private void removeView() {
        if (mDictionaryHead != null) {
            mWindowManager.removeView(mDictionaryHead);
        }
    }


    public static Intent createIntent(Context context, Word word) {
        Intent intent = new Intent(context, DictionaryHeadService.class);
        intent.putExtra(KEY_WORD, word);
        return intent;
    }

}