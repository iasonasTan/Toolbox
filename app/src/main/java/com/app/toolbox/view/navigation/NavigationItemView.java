package com.app.toolbox.view.navigation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.app.toolbox.ReceiverOwner;
import com.app.toolbox.utils.Utils;

public final class NavigationItemView extends LinearLayout implements ReceiverOwner {
    public static final String NAME_EXTRA     = "toolbox.view.broadcast.extra.CURRENT";
    public static final String ACTION_CURRENT = "toolbox.view.broadcast.action.ACTION";

    private final ImageView imageView = new ImageView(getContext());
    private final Vibrator mVibrator  = ContextCompat.getSystemService(getContext(), Vibrator.class);
    private final VibrationEffect mVibEffect = VibrationEffect.createOneShot(45, VibrationEffect.EFFECT_HEAVY_CLICK);
    private final BroadcastReceiver mStateUpdatesReceiver = new StateUpdateReceiver();
    private String mName = "UNKNOWN";

    public NavigationItemView(Context context, int srcID) {
        this(context, null);
        configImage(srcID);
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
        ContextCompat.registerReceiver(context, mStateUpdatesReceiver, new IntentFilter(ACTION_CURRENT), ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    public boolean performClick() {
        mVibrator.vibrate(mVibEffect);
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN) {
            performClick();
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void setCurrent(boolean curr) {
        final float SCALE=curr?1.2f:1f;
        imageView.animate().scaleX(SCALE).scaleY(SCALE).setDuration(125).start();
        Utils.execute(() -> {
            myInvalidate();
            myRequestLayout();
        });
    }

    private void initImage() {
        int size = Utils.dpToPx(getContext(), 30);
        LayoutParams par=new LayoutParams(size+25, size);
        par.gravity= Gravity.CENTER;
        par.setMargins(5,2,5,0);
        imageView.setLayoutParams(par);
    }

    private void myInvalidate() {
        super.invalidate();
        imageView.invalidate();
    }

    private void myRequestLayout() {
        super.requestLayout();
        imageView.requestLayout();
    }

    public void configImage(int imageID) {
        imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), imageID));
    }

    public void setName(String pageName) {
        mName = pageName;
    }

    public String getName() {
        return mName;
    }

    private final class StateUpdateReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            boolean current = mName.equals(intent.getStringExtra(NAME_EXTRA));
            setCurrent(current);
        }
    }

    @Override
    public void unregisterReceivers(Context context) {
        context.unregisterReceiver(mStateUpdatesReceiver);
    }
}
