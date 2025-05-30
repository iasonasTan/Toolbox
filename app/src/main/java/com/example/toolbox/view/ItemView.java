package com.example.toolbox.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.game.toolbox.R;

public class ItemView extends LinearLayout {
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

//        TypedArray a = context.getTheme().obtainStyledAttributes(
//                attrs,
//                R.styleable.CustomView,
//                0, 0
//        );
//
//        try {
//            String customText = a.getString(R.styleable.CustomView_customText);
//            int customColor = a.getColor(R.styleable.CustomView_customColor, Color.BLACK);
//        } finally {
//            a.recycle();
//        }
    }

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
}
