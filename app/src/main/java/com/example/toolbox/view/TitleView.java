package com.example.toolbox.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.game.toolbox.R;

public class TitleView extends androidx.appcompat.widget.AppCompatTextView {
    public TitleView(Context context) {
        super(context);
        init();
    }

    public TitleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TitleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
//        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams();
//        params.setMargins(25,25,0,0);
//        setPadding(15,5,15,10);
//        setLayoutParams(params);
        setPadding(20,22,0,0);
        setTextSize(50);
        setTypeface(Typeface.DEFAULT, Typeface.BOLD);
//        setBackgroundResource(R.drawable.round_shape);
    }

}
