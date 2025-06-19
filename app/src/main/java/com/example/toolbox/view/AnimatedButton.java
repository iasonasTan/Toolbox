package com.example.toolbox.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.ScaleAnimation;

import androidx.annotation.Nullable;

import com.game.toolbox.R;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AnimatedButton extends androidx.appcompat.widget.AppCompatButton {
    public AnimatedButton(Context context) {
        super(context);
        init();
    }

    public AnimatedButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setClickable(true);
        setFocusable(true);
        setBackgroundResource(R.drawable.round_border);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
            animateThis();

        return super.onTouchEvent(event);
    }

//    @Override
//    public void setVisibility(int visibility) {
//        if(visibility==View.GONE||visibility==View.INVISIBLE) {
//            animate().cancel();
//            clearAnimation();
//        }
//
//        super.setVisibility(visibility);
//    }

    //    @Override
//    public void setOnClickListener(@Nullable final OnClickListener l) {
//        if(l!=null) {
//            OnClickListener l2 = v -> {
//                clearAnimation();
//                l.onClick(v);
//            };
//            super.setOnClickListener(l2);
//        }
//    }

    private void animateThis() {
        final long DURATION=140;
        animateThis(0.96f, DURATION);
        var scheduledExecutor= Executors.newSingleThreadScheduledExecutor();
        scheduledExecutor.schedule(() -> {
            animateThis(1f, DURATION);
        }, DURATION, TimeUnit.MILLISECONDS);
        scheduledExecutor.shutdown();
    }

    private void animateThis(float scale, long duration) {
//        ScaleAnimation anim=new ScaleAnimation(
//                getScaleX(), scale,
//                getScaleY(), scale,
//                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
//                ScaleAnimation.RELATIVE_TO_SELF, 0.5f
//        );
//        anim.setFillAfter(true);
//        anim.setDuration(duration);
//        startAnimation(anim);

        animate()
                .scaleX(scale)
                .scaleY(scale)
                .setDuration(duration)
                .start();
    }
}
