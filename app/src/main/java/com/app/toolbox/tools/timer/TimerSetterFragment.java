package com.app.toolbox.tools.timer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.app.toolbox.R;
import com.app.toolbox.view.TimeInput;
import com.google.android.material.textfield.TextInputEditText;

public class TimerSetterFragment extends Fragment {
    private TimeInput mTimeInput;
    private TextInputEditText mNameInput;
    private Intent mShowHomeIntent;

    public final class OnStartTimerHandler implements View.OnClickListener {
        @Override public void onClick(View v) {
            long totalTime_millis = mTimeInput.getTimeMillis();
            if (totalTime_millis==0) {
                showOther();
                return;
            }
            Intent addTimerIntent = new Intent(TimerRootFragment.ACTION_NEW_TIMER).setPackage(requireContext().getPackageName());
            addTimerIntent.putExtra(TimerRootFragment.TIMER_NAME_EXTRA, mNameInput.getText().toString());
            addTimerIntent.putExtra(TimerRootFragment.TIME_MILLIS_EXTRA, totalTime_millis);
            requireContext().sendBroadcast(addTimerIntent);

            Toast.makeText(requireContext(), ContextCompat.getString(requireContext(), R.string.timer_set), Toast.LENGTH_SHORT).show();
            showOther();
        }
    }

    private void showOther() {
        requireContext().sendBroadcast(mShowHomeIntent);
        mTimeInput.resetTime();
        mNameInput.setText("");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timer_setter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                showOther();
            }
        });
        final Button startTimerButton = view.findViewById(R.id.start_timer_button);
        startTimerButton.setOnClickListener(new OnStartTimerHandler());

        mNameInput = view.findViewById(R.id.name_input);
        mNameInput.setOnEditorActionListener((v, actionID, event) -> {
            if(actionID == EditorInfo.IME_ACTION_DONE) {
                new OnStartTimerHandler().onClick(startTimerButton);
            }
            return false;
        });
        mTimeInput = view.findViewById(R.id.time_input);
        mShowHomeIntent = new Intent(TimerRootFragment.ACTION_CHANGE_FRAGMENT).setPackage(requireContext().getPackageName());
        mShowHomeIntent.putExtra(TimerRootFragment.FRAGMENT_NAME_EXTRA, TimerRootFragment.HOME_FRAGMENT);
    }

}
