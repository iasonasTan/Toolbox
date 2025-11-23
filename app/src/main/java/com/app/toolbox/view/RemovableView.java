package com.app.toolbox.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.app.toolbox.R;
import com.app.toolbox.view.storing.Named;

public class RemovableView extends LinearLayout implements Named {
    private TextView mTitleView, mContentPreviewView;
    private ImageButton mDeleteButton;

    public RemovableView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.view_removable_item, this, true);
        mTitleView =findViewById(R.id.title_view);
        mContentPreviewView =findViewById(R.id.content_preview_view);
        mDeleteButton =findViewById(R.id.deleteNote_button);
        setFont(context, R.font.zalando_sans_bold);
    }

    public RemovableView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setFont(Context context, int font) {
        Typeface typeface = ResourcesCompat.getFont(context, font);
        mTitleView.setTypeface(typeface);
        mContentPreviewView.setTypeface(typeface);
    }

    public void setOnDeleteListener(OnClickListener li){
        mDeleteButton.setOnClickListener(li);
    }

    public void setTitle (String title) {
        mTitleView.setText(title);
    }

    public void setContent (String content) {
        mContentPreviewView.setText(content);
    }

    public String getTitle () {
        return mTitleView.getText().toString();
    }

    public String getContent() {
        return mContentPreviewView.getText().toString();
    }

    @Override
    public String getName() {
        return getTitle();
    }
}
