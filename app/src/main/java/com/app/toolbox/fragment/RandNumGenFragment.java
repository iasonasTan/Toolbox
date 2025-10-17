package com.app.toolbox.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.toolbox.R;
import com.app.toolbox.utils.ToolFragment;
import com.app.toolbox.view.navigation.NavigationItemView;

import java.util.Locale;

public class RandNumGenFragment extends ToolFragment {

    @Override
    protected String fragmentName() {
        return "toolbox.page.RANDOM_NUMBER_GENERATOR_PAGE";
    }

    @Override
    protected NavigationItemView createNavigationItem(Context context) {
        return new NavigationItemView(context, R.drawable.rng_icon);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_random_number_generator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TextView curr=view.findViewById(R.id.current_limit_textview);
        final SeekBar bar=view.findViewById(R.id.limitInput_seekbar);
        final TextView output=view.findViewById(R.id.output_textview);
        view.findViewById(R.id.generate_button).setOnClickListener(v -> {
            float randomVal=(float)(Math.random()*bar.getProgress());
            output.setText(String.format(Locale.getDefault(), "%.4f", randomVal));
        });

        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //noinspection all
                curr.setText(""+progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        bar.setProgress(20);

    }

}
