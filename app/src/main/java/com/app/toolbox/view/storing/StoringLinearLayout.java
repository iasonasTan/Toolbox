package com.app.toolbox.view.storing;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import java.util.List;

public class StoringLinearLayout extends LinearLayout {
    private List<String> mNames;

    public StoringLinearLayout(Context context) {
        super(context);
    }

    public StoringLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public StoringLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressWarnings("unused")
    public StoringLinearLayout(Context context, List<String> namesList) {
        this(context);
        setNamesList(namesList);
    }

    @SuppressWarnings("unused")
    public StoringLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setNamesList(List<String> namesList) {
        mNames = namesList;
    }

    private void check(View view) {
        if(mNames == null)
            throw new NullPointerException("Cannot store getPageName in list because list is null.");
        if (!(view instanceof Named)) {
            throw new IllegalArgumentException("View is not an implementation of interface Named.");
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        check(child);
        Named named = (Named)child;
        mNames.add(named.getName());
    }

    @Override
    public void removeView(View view) {
        super.removeView(view);
        check(view);
        Named named = (Named)view;
        mNames.add(named.getName());
    }
}
