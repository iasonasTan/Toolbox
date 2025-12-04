package com.app.toolbox.tools.timer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.app.toolbox.R;
import com.app.toolbox.view.TimeInputView;

/**
 * This activity is used with shortcuts.<br>
 * The In-Application way is to use {@link com.app.toolbox.tools.timer.TimerFragment.TimerSetterFragment}.
 */
public class AddTimerActivity extends AppCompatActivity {

    // TODO Do something with this class

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timer_setter);
        TimeInputView timeInput = findViewById(R.id.time_input);
        TextView nameInput = findViewById(R.id.name_input);
        findViewById(R.id.start_timer_button).setOnClickListener(new TimerAdder(this, timeInput, nameInput){
            @Override public void addTimer() {
                super.addTimer();
                Intent intent = new Intent(getApplicationContext(), TimerService.class);
                intent.setAction(TimerService.ACTION_UPDATE_TIMERS);
                startForegroundService(intent);
                finish();
            }
        });
        Intent startTimerServiceIntent = new Intent(this, TimerService.class).setAction(TimerService.ACTION_UPDATE_TIMERS);
        startForegroundService(startTimerServiceIntent);
    }

}
