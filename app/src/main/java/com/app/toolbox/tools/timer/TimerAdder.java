package com.app.toolbox.tools.timer;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.app.toolbox.R;
import com.app.toolbox.view.TimeInputView;

public class TimerAdder implements View.OnClickListener {
    protected final Context context;
    private final TimeInputView mTimeInputView;
    private final TextView mNameInput;

    public TimerAdder(Context context, @NonNull TimeInputView inputView, @NonNull TextView nameInput) {
        this.context = context;
        mTimeInputView = inputView;
        mNameInput = nameInput;
    }

    public void addTimer() {
        long totalTime_millis = mTimeInputView.getTimeMillis();
        if (totalTime_millis == 0) {
            return;
        }
        Intent addTimerIntent = new Intent(TimerFragment.ACTION_NEW_TIMER).setPackage(context.getPackageName());
        addTimerIntent.putExtra(TimerFragment.TIMER_NAME_EXTRA, String.valueOf(mNameInput.getText()));
        addTimerIntent.putExtra(TimerFragment.TIME_MILLIS_EXTRA, totalTime_millis);
        context.sendBroadcast(addTimerIntent);
        Toast.makeText(context, ContextCompat.getString(context, R.string.timer_set), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View ignored) {
        addTimer();
    }
}
