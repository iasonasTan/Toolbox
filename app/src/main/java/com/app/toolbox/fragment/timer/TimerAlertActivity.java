package com.app.toolbox.fragment.timer;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.toolbox.R;
import com.app.toolbox.utils.IntentContentsMissingException;

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
        findViewById(R.id.stop_button).setOnClickListener(l -> {
            TimerService.Timer.sRingtone.stop();
            finish();
        });

        // allways starts with intent
        Intent intent = getIntent();
        String action = intent.getAction(), message = intent.getStringExtra("message");
        if(action==null||message==null) throw new IntentContentsMissingException();
        if(action.equals(ACTION_SHOW_MESSAGE)) {
            textView.setText(message);
        }
    }

    public static PendingIntent createPIntent(Context context, String message) {
        Intent intent = new Intent(context, TimerAlertActivity.class);
        intent.setAction(ACTION_SHOW_MESSAGE);
        intent.putExtra("message", message);
        return PendingIntent.getActivity(context, 13, intent, PendingIntent.FLAG_IMMUTABLE);
    }
}