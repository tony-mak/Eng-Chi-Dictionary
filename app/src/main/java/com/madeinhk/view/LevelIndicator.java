package com.madeinhk.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

import com.madeinhk.english_chinesedictionary.R;


public class LevelIndicator extends View {
    private int[] mColors = new int[0];
    private int mLevel = 0;

    public LevelIndicator(Context context) {
        this(context, null);
    }

    public LevelIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.LevelIndicatorStyle);
    }

    public LevelIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LevelIndicator,
                defStyleAttr, R.style.LevelIndicatorStyle);
        try {
            final int id = a.getResourceId(R.styleable.LevelIndicator_levels, 0);
            if (id != 0) {
                final int[] colors = a.getResources().getIntArray(id);
                mColors = colors;
            }
        } finally {
            a.recycle();
        }
    }

    public void setLevel(int level) {
        mLevel = level;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int minh = getSuggestedMinimumHeight() + getPaddingBottom() + getPaddingTop();

        setMeasuredDimension(
                resolveSize(minw, widthMeasureSpec),
                resolveSize(minh, heightMeasureSpec));
    }

    protected void onDraw(Canvas canvas) {
        if (mColors != null) {
            final int numberOfLevel = mColors.length;
            final int widgetWidth = getWidth() - ViewCompat.getPaddingStart(this) - ViewCompat
                    .getPaddingEnd(this);
            final int widgetHeight = getHeight() - getPaddingTop() - getPaddingBottom();
            final int levelWidth = widgetWidth / numberOfLevel;
            Rect progressRect = new Rect();
            Paint progressPaint = new Paint();
            for (int i = 0; i < numberOfLevel; i++) {
                progressPaint.setColor(mColors[i]);
                if (i <= mLevel) {
                    progressPaint.setAlpha(255);
                } else {
                    progressPaint.setAlpha(10);
                }
                final int levelLeftOffset = ViewCompat.getPaddingStart(this) + levelWidth * i;
                final int levelRightOffset = (i < numberOfLevel - 1) ? levelLeftOffset +
                        levelWidth : widgetWidth + ViewCompat.getPaddingEnd(this);

                progressRect.set(levelLeftOffset, getPaddingTop(),
                        levelRightOffset, widgetHeight);
                canvas.drawRect(progressRect, progressPaint);
            }
        }
    }


}