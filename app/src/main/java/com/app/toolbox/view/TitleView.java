package com.app.toolbox.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

public class TitleView extends androidx.appcompat.widget.AppCompatTextView {
    public TitleView(Context context) {
        this(context, null);
    }

    public TitleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public TitleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTextSize(50);
        setTypeface(Typeface.DEFAULT, Typeface.BOLD);
    }

}
