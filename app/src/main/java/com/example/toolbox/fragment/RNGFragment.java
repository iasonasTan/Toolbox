package com.example.toolbox.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.toolbox.MainActivity;
import com.example.toolbox.view.navigation.NavigationItemView;
import com.game.toolbox.R;

public class RNGFragment extends ToolFragment {

    public RNGFragment(Context context) {
        super(new NavigationItemView(context, R.drawable.random_number));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

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
            output.setText(String.format("%.4f", randomVal));
        });

        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                curr.setText(""+progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

}
