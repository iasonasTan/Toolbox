package com.app.toolbox.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.toolbox.R;
import com.app.toolbox.view.storing.Named;

public class ItemView extends LinearLayout implements Named {
    private TextView title_view;
    private TextView contentPreview_view;
    private ImageButton delete_button;

    public ItemView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.view_item, this, true);
        title_view=findViewById(R.id.title_view);
        contentPreview_view=findViewById(R.id.content_preview_view);
        delete_button =findViewById(R.id.deleteNote_button);
    }

    public ItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

//    @SuppressWarnings("unused")
//    public void focus() {
//        ScheduledExecutorService executor= Executors.newSingleThreadScheduledExecutor();
//        setBackgroundResource(R.drawable.round_focused_border);
//        executor.schedule(() -> setBackgroundResource(R.drawable.background_with_border), 400, TimeUnit.MILLISECONDS);
//        executor.shutdown();
//    }

    public void setOnDeleteListener(OnClickListener li){
        delete_button.setOnClickListener(li);
    }

    public void setTitle (String title) {
        title_view.setText(title);
    }

    public void setContent (String content) {
        contentPreview_view.setText(content);
    }

    public String getTitle () {
        return title_view.getText().toString();
    }

    public String getContent() {
        return contentPreview_view.getText().toString();
    }

    @Override
    public String getName() {
        return getTitle();
    }
}
