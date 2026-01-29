package com.app.toolbox.tools.timer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.app.toolbox.R;
import com.app.toolbox.view.TimeInputView;
import com.google.android.material.textfield.TextInputEditText;

public class AddTimerFragment extends Fragment {
    private TimeInputView mTimeInput;
    private TextInputEditText mNameInput;
    private Intent mShowHomeIntent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.timer_setter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressed());
        initViews(view);

        mShowHomeIntent = new Intent(TimerFragment.ACTION_CHANGE_FRAGMENT)
                .setPackage(requireContext().getPackageName())
                .putExtra(TimerFragment.FRAGMENT_NAME_EXTRA, TimerFragment.HOME_FRAGMENT);
    }

    private void initViews(View view) {
        mNameInput = view.findViewById(R.id.name_input);
        mTimeInput = view.findViewById(R.id.time_input);
        TimerAdder timerAdder = new TimerAdder(requireContext(), mTimeInput, mNameInput) {
            @Override public void addTimer() {
                super.addTimer();
                context.sendBroadcast(mShowHomeIntent);
            }
        };
        view.findViewById(R.id.start_timer_button).setOnClickListener(timerAdder);
        mNameInput.setOnEditorActionListener((v, actionID, event) -> {
            if (actionID == EditorInfo.IME_ACTION_DONE)
                timerAdder.addTimer();
            return false;
        });
    }

    private final class OnBackPressed extends OnBackPressedCallback {
        public OnBackPressed() {
            super(true);
        }

        @Override
        public void handleOnBackPressed() {
            showOther();
        }
    }

    private void showOther() {
        requireContext().sendBroadcast(mShowHomeIntent);
        mTimeInput.resetTime();
        mNameInput.setText("");
    }

    public static class TimerAdder implements View.OnClickListener {
        protected final Context context;
        private final TimeInputView mTimeInputView;
        private final TextView mNameInput;

        public TimerAdder(Context context, @NonNull TimeInputView inputView, @NonNull TextView nameInput) {
            this.context = context;
            mTimeInputView = inputView;
            mNameInput = nameInput;
        }

        public void addTimer() {
            long totalTime_millis = mTimeInputView.getTimeMillis();
            if (totalTime_millis == 0) {
                return;
            }
            Intent addTimerIntent = new Intent(TimerFragment.ACTION_NEW_TIMER).setPackage(context.getPackageName());
            addTimerIntent.putExtra(TimerFragment.TIMER_NAME_EXTRA, String.valueOf(mNameInput.getText()));
            addTimerIntent.putExtra(TimerFragment.TIME_MILLIS_EXTRA, totalTime_millis);
            context.sendBroadcast(addTimerIntent);
            Toast.makeText(context, ContextCompat.getString(context, R.string.timer_set), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onClick(View ignored) {
            addTimer();
        }
    }
}
