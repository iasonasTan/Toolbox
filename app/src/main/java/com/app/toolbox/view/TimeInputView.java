package com.app.toolbox.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.app.toolbox.R;

public class TimeInputView extends LinearLayout {
    private final NumberPicker mHoursInput, mMinsInput, mSecsInput;

    public TimeInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_time_input, this);
        mHoursInput =findViewById(R.id.hours_picker);
        mMinsInput =findViewById(R.id.minutes_picker);
        mSecsInput =findViewById(R.id.seconds_picker);
        TextView title_textview = findViewById(R.id.title_tv);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TimeInputView);
        String title = a.getString(R.styleable.TimeInputView_title);
        a.recycle();

        title_textview.setText(title);

        mHoursInput.setMaxValue(23);
        mMinsInput.setMaxValue(59);
        mSecsInput.setMaxValue(59);

    }

    public long getTimeMillis() {
        return mSecsInput.getValue()*1000L+
               mMinsInput.getValue()*1000*60L+
              mHoursInput.getValue()*1000*60*60L;
    }

    public void reset() {
        mHoursInput.setValue(0);
        mMinsInput.setValue(0);
        mSecsInput.setValue(0);
    }
}
