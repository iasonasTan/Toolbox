package com.app.toolbox.view.navigation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class NavigationItemView extends LinearLayout {
    private final ImageView imageView=new ImageView(getContext());
    private final Vibrator mVibrator;
    private int mImageSrcId;

    public NavigationItemView(Context context, int srcID) {
        this(context);
        this.mImageSrcId=srcID;
        config(srcID);
    }

    public NavigationItemView(Context context) {
        this(context, null);
    }

    public NavigationItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutParams params= new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(6,1,6,1);
        setLayoutParams(params);
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER);
        initImage();
        addView(imageView);
        mVibrator= ContextCompat.getSystemService(context, Vibrator.class);
        mImageSrcId = 0;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN) {
            mVibrator.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.EFFECT_HEAVY_CLICK));
        }
        return super.onTouchEvent(event);
    }

    public void setCurrent(boolean curr) {
        final float SCALE=curr?1.2f:1f;
        imageView.animate()
                .scaleX(SCALE).scaleY(SCALE)
                .setDuration(125).start();
        try {
            myInvalidate();
            myRequestLayout();
        } catch (NullPointerException e) {
            throw new RuntimeException(e);
        }
    }

    public final int getImageSrcId() {
        return mImageSrcId;
    }

    private void initImage() {
        float scale = getContext().getResources().getDisplayMetrics().density;
        int sizeInDp = 30;
        int sizeInPx = (int) (sizeInDp * scale + 0.10f);
        LayoutParams par=new LayoutParams(sizeInPx+25, sizeInPx);
        par.gravity= Gravity.CENTER;
        par.setMargins(5,2,5,0);
        imageView.setLayoutParams(par);
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
