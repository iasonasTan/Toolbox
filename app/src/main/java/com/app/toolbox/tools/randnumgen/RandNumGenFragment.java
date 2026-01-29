package com.app.toolbox.tools.randnumgen;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.toolbox.R;
import com.app.toolbox.utils.PageFragment;
import com.app.toolbox.view.navigation.NavigationItemView;
import com.google.android.material.slider.Slider;

import java.util.Locale;

public class RandNumGenFragment extends PageFragment {
    public static final String STRING_ID = "toolbox.page.RANDOM_NUMBER_GENERATOR_PAGE";

    private static final String LAST_LIMIT_VALUE_NAME     = "toolbox.randnumgen.valueLimit";
    private static final String LAST_DISPLAYED_VALUE_NAME = "toolbox.randnumgen.lastDisplayedValue";

    private static final float DEFAULT_LIMIT = 25f;
    private static final String DEFAULT_DISPLAYED_TEXT = "0.0";

    private Slider mLimitSlider;
    private TextView mValueField;

    @Override
    protected String fragmentName() {
        return STRING_ID;
    }

    @Override
    protected NavigationItemView createNavigationItem(Context context) {
        return new NavigationItemView(context, R.drawable.rng_icon);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rand_num, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // initialize view fields
        mLimitSlider = view.findViewById(R.id.limitInput_slider);
        mValueField = view.findViewById(R.id.output_textview);

        // restore state
        float lastSelectedLimit;
        String lastDisplayedValue;
        if(savedInstanceState != null) {
            lastSelectedLimit = savedInstanceState.getFloat(LAST_LIMIT_VALUE_NAME);
            lastDisplayedValue= savedInstanceState.getString(LAST_DISPLAYED_VALUE_NAME);
        } else {
            // restore from shared preferences
            SharedPreferences preferences = requireContext().getSharedPreferences(STRING_ID, Context.MODE_PRIVATE);
            lastSelectedLimit = preferences.getFloat(LAST_LIMIT_VALUE_NAME, DEFAULT_LIMIT);
            lastDisplayedValue= preferences.getString(LAST_DISPLAYED_VALUE_NAME, DEFAULT_DISPLAYED_TEXT);
        }
        mLimitSlider.setValue(lastSelectedLimit);
        mValueField.setText(lastDisplayedValue);

        // add listeners
        view.findViewById(R.id.generate_button).setOnClickListener(v -> {
            float randomVal=(float)(Math.random()* mLimitSlider.getValue());
            mValueField.setText(String.format(Locale.getDefault(), "%.2f", randomVal));
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat(LAST_LIMIT_VALUE_NAME, mLimitSlider.getValue());
        outState.putString(LAST_DISPLAYED_VALUE_NAME, mValueField.getText().toString());
    }

    @Override
    public void onStop() {
        super.onStop();
        requireContext().getSharedPreferences(STRING_ID, Context.MODE_PRIVATE)
                .edit()
                .putFloat(LAST_LIMIT_VALUE_NAME, mLimitSlider.getValue())
                .putString(LAST_DISPLAYED_VALUE_NAME, mValueField.getText().toString())
                .apply();
    }
}
