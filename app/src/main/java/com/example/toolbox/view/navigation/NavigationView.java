package com.example.toolbox.view.navigation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.game.toolbox.R;

public class NavigationView extends LinearLayout {

    public NavigationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NavigationView(Context context) {
        super(context);
        init();
    }

    private void init() {
        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER);
        int heightInDp = 30;
        float scale = getContext().getResources().getDisplayMetrics().density;
        int heightInPx = (int) (heightInDp * scale + 0.10f);
        ViewGroup.LayoutParams params=new LayoutParams(LayoutParams.MATCH_PARENT, heightInPx);
        setLayoutParams(params);

        setPadding(10, 0, 10, 5);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public void addItem(NavigationItemView item) {
        addView(item);
    }

}
