package com.app.toolbox.tools.timer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.app.toolbox.R;
import com.app.toolbox.utils.ParentPageFragment;
import com.app.toolbox.view.TimeInputView;
import com.google.android.material.textfield.TextInputEditText;

public final class TimerEditor extends Fragment {
    private TimeInputView mTimeInput;
    private TextInputEditText mNameInput;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                // Show home
                Intent showHomeIntent = new Intent(ParentPageFragment.actionChangePage(TimerRoot.STRING_ID)).setPackage(requireContext().getPackageName());
                showHomeIntent.putExtra(ParentPageFragment.PAGE_CLASSNAME_EXTRA, TimerHome.class.getName());
                requireContext().sendBroadcast(showHomeIntent);
            }
        });
        mNameInput = view.findViewById(R.id.name_input);
        mTimeInput = view.findViewById(R.id.time_input);

        Button doneButton = view.findViewById(R.id.start_timer_button);
        doneButton.setOnClickListener(v -> {
            // And intent
            Intent timerAddIntent = new Intent(requireContext(), TimerService.class);
            timerAddIntent.setAction(TimerService.ACTION_ADD_TIMER);
            timerAddIntent.putExtra(TimerService.TIMER_NAME_EXTRA, String.valueOf(mNameInput.getText()));
            timerAddIntent.putExtra(TimerService.TIME_DELTA_EXTRA, mTimeInput.getTimeMillis());
            requireContext().startForegroundService(timerAddIntent);
            // Show Home
            Intent showHomeIntent = new Intent(ParentPageFragment.actionChangePage(TimerRoot.STRING_ID)).setPackage(requireContext().getPackageName());
            showHomeIntent.putExtra(ParentPageFragment.PAGE_CLASSNAME_EXTRA, TimerHome.class.getName());
            requireContext().sendBroadcast(showHomeIntent);
            // Reset views now to prevent android default auto-save thing
            mNameInput.setText("");
            mTimeInput.reset();
        });
        mNameInput.setOnEditorActionListener((v, actionID, event) -> {
            if (actionID == EditorInfo.IME_ACTION_DONE)
                doneButton.performClick();
            return false;
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.timer_setter, container, false);
    }
}
