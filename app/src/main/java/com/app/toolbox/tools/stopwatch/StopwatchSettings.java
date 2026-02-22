package com.app.toolbox.tools.stopwatch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.app.toolbox.R;
import com.app.toolbox.utils.ParentPageFragment;

public class StopwatchSettings extends Fragment {
    static final String SHOW_MILLIS = "toolbox.stopwatch.showMillis";
    private CheckBox mShowMillisCheckBox;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mShowMillisCheckBox = view.findViewById(R.id.show_millis);
        view.findViewById(R.id.save_settings).setOnClickListener(ignored -> saveAndExit());
        SharedPreferences preferences = requireContext().getSharedPreferences(StopwatchHome.PREFERENCES_NAME, Context.MODE_PRIVATE);
        mShowMillisCheckBox.setChecked(preferences.getBoolean(StopwatchHome.MILLIS_PREFERENCE, false));

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressed());
    }

    private final class OnBackPressed extends OnBackPressedCallback {
        public OnBackPressed() {
            super(true);
        }

        @Override
        public void handleOnBackPressed() {
            saveAndExit();
        }
    }

    private void saveAndExit() {
        boolean showMillis = mShowMillisCheckBox.isChecked();
        requireContext().getSharedPreferences(StopwatchHome.PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(StopwatchHome.MILLIS_PREFERENCE, showMillis).apply();
        Toast.makeText(requireContext(), R.string.settings_udpated, Toast.LENGTH_SHORT).show();

        Intent exitIntent = new Intent(ParentPageFragment.actionChangePage(StopwatchRoot.STRING_ID)).setPackage(requireContext().getPackageName());
        exitIntent.putExtra(ParentPageFragment.PAGE_CLASSNAME_EXTRA, StopwatchHome.class.getName());
        requireContext().sendBroadcast(exitIntent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }
}
