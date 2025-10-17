package com.app.toolbox.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.app.toolbox.R;

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
        Typeface typeface = ResourcesCompat.getFont(context, R.font.dm_serif_display_regular);
        setTypeface(typeface);
    }

}
