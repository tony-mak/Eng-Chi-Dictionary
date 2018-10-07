package com.madeinhk.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.core.view.ViewCompat;

import com.madeinhk.english_chinesedictionary.R;

public class LevelIndicator extends View {
    private int[] mColors = new int[0];
    private int mLevel = 0;
    private int mBorderColor;
    private int mBorderWidth;

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
                mBorderColor = getColorInt(context, R.attr.colorPrimaryDark);
            }
        } finally {
            a.recycle();
        }
        mBorderWidth = getResources().getDimensionPixelSize(R.dimen.level_indicator_stroke_width);
    }

    public void setLevel(int level) {
        mLevel = level;
        invalidate();
    }

    public int getLevel() {
        return mLevel;
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
        if (mColors == null) {
            return;
        }
        final int numberOfLevel = mColors.length;
        final int widgetWidth = getWidth() - ViewCompat.getPaddingStart(this)
                - ViewCompat.getPaddingEnd(this);
        final int widgetHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        final int levelWidth = (widgetWidth - 2 * mBorderWidth) / numberOfLevel;
        Rect progressRect = new Rect();
        Paint progressPaint = new Paint();
        for (int i = 0; i < numberOfLevel; i++) {
            progressPaint.setStyle(Paint.Style.FILL);
            progressPaint.setColor(mColors[i]);
            progressPaint.setStrokeWidth(mBorderWidth);
            final int levelLeftOffset =
                    (int) (ViewCompat.getPaddingStart(this) + levelWidth * i
                            + progressPaint.getStrokeWidth());
            final int levelRightOffset = levelLeftOffset + levelWidth;
            progressRect.set(levelLeftOffset,
                    (int) (getPaddingTop() + progressPaint.getStrokeWidth()),
                    levelRightOffset, (int) (widgetHeight - progressPaint.getStrokeWidth()));
            if (i <= mLevel) {
                canvas.drawRect(progressRect, progressPaint);
            }
            progressPaint.setStyle(Paint.Style.STROKE);
            progressPaint.setColor(mBorderColor);
            canvas.drawRect(progressRect, progressPaint);
        }
    }

    @ColorInt
    public static int getColorInt(Context context, @AttrRes int resId) {
        TypedValue colorValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        if (theme.resolveAttribute(resId, colorValue, /* resolveRefs= */ true)
                && TypedValue.TYPE_FIRST_COLOR_INT <= colorValue.type
                && colorValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            return colorValue.data;
        }
        throw new IllegalArgumentException("Theme is missing expected color " + resId);
    }
}