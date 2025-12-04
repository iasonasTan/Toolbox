package com.app.toolbox.tools.randnumgen;

import android.content.Context;
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

        final Slider slider=view.findViewById(R.id.limitInput_slider);
        final TextView output=view.findViewById(R.id.output_textview);
        view.findViewById(R.id.generate_button).setOnClickListener(v -> {
            float randomVal=(float)(Math.random()*slider.getValue());
            output.setText(String.format(Locale.getDefault(), "%.2f", randomVal));
        });
        slider.setValue(20);
    }

}
