package com.example.toolbox.view.navigation;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.game.toolbox.R;

public class NavigationItemView extends LinearLayout {
    private final ImageView imageView=new ImageView(getContext());
    private boolean current=false;
    private int sizeInDp=30;

    public NavigationItemView(Context context, int srcID) {
        super(context);
        init();
        config(srcID);
    }

    public NavigationItemView(Context context) {
        super(context);
        init();
    }

    public NavigationItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setCurrent(boolean s) {
        current=s;
        // update
        if(current) {
            System.out.println(this+" set as current");
            sizeInDp=37;
        } else {
            sizeInDp=30;
        }

        initImage();

        try {
            myInvalidate();
            myRequestLayout();
        } catch (NullPointerException e) {
            throw new IllegalStateException(e);
        }
    }

    private void initImage() {
        float scale = getContext().getResources().getDisplayMetrics().density;
        int sizeInPx = (int) (sizeInDp * scale + 0.10f);
        LayoutParams par=new LayoutParams(sizeInPx+25, sizeInPx);
        par.gravity= Gravity.CENTER;
        par.setMargins(5,2,5,0);
        imageView.setLayoutParams(par);
    }

    private void init() {
        LinearLayout.LayoutParams params= new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(6,1,6,1);
        setLayoutParams(params);
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER);

        initImage();

        addView(imageView);
    }

    public void myInvalidate() {
        super.invalidate();
        imageView.invalidate();
    }

    public void myRequestLayout() {
        super.requestLayout();
        imageView.requestLayout();
    }

    public void config(int imageID) {
        imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), imageID));
    }

}
