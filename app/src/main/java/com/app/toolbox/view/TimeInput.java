package com.app.toolbox.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.app.toolbox.R;

public class TimeInput extends LinearLayout {
    private final NumberPicker hours_input, mins_input, secs_input;

    public TimeInput(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.time_input, this);
        hours_input=findViewById(R.id.hours_picker);
        mins_input=findViewById(R.id.minutes_picker);
        secs_input=findViewById(R.id.seconds_picker);
        TextView title_textview = findViewById(R.id.title_tv);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TimeInput);
        String title = a.getString(R.styleable.TimeInput_title);
        a.recycle();

        title_textview.setText(title);

        hours_input.setMaxValue(23);
        mins_input.setMaxValue(59);
        secs_input.setMaxValue(59);

    }

    public long getTimeMillis() {
        return secs_input.getValue()*1000L+
                mins_input.getValue()*1000*60L+
                hours_input.getValue()*1000*60*60L;
    }

    public void setToZero() {
        hours_input.setValue(0);
        mins_input.setValue(0);
        secs_input.setValue(0);
    }
}
