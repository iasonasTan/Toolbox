package com.app.toolbox.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.app.toolbox.R;
import com.app.toolbox.SettingsActivity;

public class AnimatedButton extends AppCompatButton {
    public static int sBackground = -1;
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
//        if(sBackground==-1) { // not existing color
//            SharedPreferences preferences = context.getSharedPreferences(SettingsActivity.PREFERENCES_NAME, Context.MODE_PRIVATE);
//            sBackground = preferences.getInt(SettingsActivity.BUTTON_BACKGROUND_PREF, Color.BLUE);
//        }
        //setBackgroundColor(sBackground);
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
