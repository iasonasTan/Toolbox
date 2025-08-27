package com.app.toolbox.fragment.timer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.app.toolbox.R;
import com.app.toolbox.view.TimeInput;

public class TimerSetterFragment extends TimerFragment.InnerFragment {
    // views
    private TimeInput timeInput;
    private EditText name_input;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                exitSetter();
            }
        });

        view.findViewById(R.id.start_timer_button).setOnClickListener(v -> {
            long totalTime_millis = timeInput.getTimeMillis();

            if (totalTime_millis != 0) {
                getParentTimerFragment().addTimer(totalTime_millis, name_input.getText().toString());
                Toast.makeText(requireContext(), ContextCompat.getString(requireContext(), R.string.timer_set), Toast.LENGTH_SHORT).show();
            }

            exitSetter();
        });
    }

    private void exitSetter() {
        getParentTimerFragment().setFragment(getParentTimerFragment().timersFragment);
        timeInput.setToZero();
        name_input.setText("");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_timer_setter, container, false);
        name_input = rootView.findViewById(R.id.name_input);
        timeInput = rootView.findViewById(R.id.time_input);
        return rootView;
    }

}
