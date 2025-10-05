package com.app.toolbox.fragment.timer;

import android.content.Intent;
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
    private TimeInput mTimeInput;
    private EditText mNameInput;
    private Intent mShowTimeHomeIntent;

    public final class OnStartTimerHandler implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            long totalTime_millis = mTimeInput.getTimeMillis();
            if (totalTime_millis==0) {
                showOther();
                return;
            }
            getParentTimerFragment().addTimer(totalTime_millis, mNameInput.getText().toString());
            Toast.makeText(requireContext(), ContextCompat.getString(requireContext(), R.string.timer_set), Toast.LENGTH_SHORT).show();
            showOther();
        }
    }

    public final class OnBackPressed extends OnBackPressedCallback {
        public OnBackPressed() {
            super(true);
        }

        @Override
        public void handleOnBackPressed() {
            showOther();
        }
    }

    private void showOther() {
        requireContext().sendBroadcast(mShowTimeHomeIntent);
        mTimeInput.setToZero();
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
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressed());
        view.findViewById(R.id.start_timer_button).setOnClickListener(new OnStartTimerHandler());
        mNameInput = view.findViewById(R.id.name_input);
        mTimeInput = view.findViewById(R.id.time_input);
        mShowTimeHomeIntent = new Intent(TimerFragment.ACTION_CHANGE_FRAGMENT).setPackage(requireContext().getPackageName());
        mShowTimeHomeIntent.putExtra("fragmentName", TimerFragment.HOME_FRAGMENT);
    }

}
