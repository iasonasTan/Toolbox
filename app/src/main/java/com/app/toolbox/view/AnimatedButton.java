package com.app.toolbox.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.app.toolbox.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;

/**
 * Configured material button with animations, vibration effects and custom appearance.
 */
public class AnimatedButton extends MaterialButton {
    public static int sBackground = -1;
    private final Vibrator mVibrator;
    private final VibrationEffect mClickVibrationEffect =VibrationEffect.createOneShot(40, VibrationEffect.EFFECT_TICK);

    public AnimatedButton(@NonNull Context context) {
        this(context, null);
    }

    public AnimatedButton (@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AnimatedButton);
            boolean highlighted = a.getBoolean(R.styleable.AnimatedButton_highlighted, false);
            a.recycle();
            if(highlighted)
                setBackgroundResource(R.drawable.red_background_with_border);
            else
                setBackgroundResource(R.drawable.background_with_border);
        } else {
            setBackgroundResource(R.drawable.background_with_border);
        }
        Typeface typeface = ResourcesCompat.getFont(context, R.font.zalando_sans_bold);
        setTypeface(typeface);
        setClickable(true);
        setFocusable(true);
        setTextColor(MaterialColors.getColor(context, android.R.attr.textColorPrimary, context.getColor(R.color.white)));
        mVibrator=ContextCompat.getSystemService(context, Vibrator.class);
    }

    @Override
    public boolean performClick() {
        animateThis();
        mVibrator.vibrate(mClickVibrationEffect);
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            performClick();
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void animateThis() {
        final float DURATION=140f, SCALE=0.96f;
        Runnable removeAnim=()-> animate().scaleX(1f).scaleY(1f).setDuration((long)DURATION).start();
        animate().scaleX(SCALE).scaleY(SCALE).setDuration((long)DURATION).withEndAction(removeAnim).start();
    }

    public void setHighlighted(boolean h) {
        setBackgroundResource(h?R.drawable.red_background_with_border:R.drawable.background_with_border);
    }
}
