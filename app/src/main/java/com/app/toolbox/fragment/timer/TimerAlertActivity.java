package com.app.toolbox.fragment.timer;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.toolbox.R;
import com.app.toolbox.utils.IntentContentsMissingException;

/*
 * REMOVED FROM MANIFEST
 * REMOVED USAGES
 * XML STILL EXISTS
 */

@Deprecated(forRemoval = true)
@SuppressWarnings("all")
public class TimerAlertActivity extends AppCompatActivity {
    static final String ACTION_SHOW_MESSAGE = "toolbox.timer.showMessageActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_timer_alert);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // initialize views
        TextView textView = findViewById(R.id.timer_view);
        Button stopButton = findViewById(R.id.stop_button);

        // allways starts with intent
        Intent intent = getIntent();
        String action = intent.getAction(), message = intent.getStringExtra("message");
        int timerID = intent.getIntExtra("timerID", -4);
        if(action==null||message==null||timerID==-4) throw new IntentContentsMissingException();
        if(action.equals(ACTION_SHOW_MESSAGE)) {
            textView.setText(message);
        }

        // add listeners
        stopButton.setOnClickListener(l -> {
            TimerService.Timer.sRingtone.stop();
            Intent intent2 = new Intent(getApplicationContext(), StopTimerReceiver.class);
            intent2.setAction("STOP_TIMER");
            intent2.putExtra("timer_id", timerID);
            getApplicationContext().sendBroadcast(intent2);
            Log.d("action_spoil", "Stop timer with ID="+timerID+" after it ends.");
            finish();
        });
    }

    public static PendingIntent createPIntent(Context context, String message, int timerID) {
        Intent intent = new Intent(context, TimerAlertActivity.class);
        intent.setAction(ACTION_SHOW_MESSAGE);
        intent.putExtra("message", message);
        intent.putExtra("timerID", timerID);
        return PendingIntent.getActivity(context, 13, intent, PendingIntent.FLAG_IMMUTABLE);
    }
}