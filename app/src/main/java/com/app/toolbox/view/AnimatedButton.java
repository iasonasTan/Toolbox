package com.app.toolbox.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.app.toolbox.R;

public class AnimatedButton extends AppCompatButton {
    private final Vibrator mVibrator;
    private final VibrationEffect mClickVibrationEffect =VibrationEffect.createOneShot(40, VibrationEffect.EFFECT_TICK);

    public AnimatedButton(Context context) {
        this(context, null);
    }

    public AnimatedButton (Context context, AttributeSet attrs) {
        super(context, attrs);
        Typeface typeface = ResourcesCompat.getFont(context, R.font.zalando_sans_bold);
        setTypeface(typeface);
        setClickable(true);
        setFocusable(true);
        setBackgroundResource(R.drawable.background_with_border);
        mVibrator=ContextCompat.getSystemService(context, Vibrator.class);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            animateThis();
            mVibrator.vibrate(mClickVibrationEffect);
        }
        return super.onTouchEvent(event);
    }

    private void animateThis() {
        final float DURATION=140f, SCALE=0.96f;
        Runnable removeAnim=()-> animate().scaleX(1f).scaleY(1f).setDuration((long)DURATION).start();
        animate().scaleX(SCALE).scaleY(SCALE).setDuration((long)DURATION).withEndAction(removeAnim).start();
    }
}
