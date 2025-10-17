package com.app.toolbox.fragment.stopwatch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import com.app.toolbox.R;

public class SettingsFragment extends Fragment {
    static final String SHOW_MILLIS = "toolbox.stopwatch.showMillis";

    // views
    private CheckBox mShowMillisCheckBox;

    public SettingsFragment() {
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mShowMillisCheckBox = view.findViewById(R.id.show_millis);
        view.findViewById(R.id.save_settings).setOnClickListener(this::saveAndExit);
        SharedPreferences preferences = requireContext().getSharedPreferences(StopwatchFragment.PREFERENCES_NAME, Context.MODE_PRIVATE);
        mShowMillisCheckBox.setChecked(preferences.getBoolean(StopwatchFragment.MILLIS_PREFERENCE, false));

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        saveAndExit(null);
                    }
                });
    }

    private void saveAndExit(View view) {
        boolean showMillis = mShowMillisCheckBox.isChecked();
        SharedPreferences preferences = requireContext().getSharedPreferences(StopwatchFragment.PREFERENCES_NAME, Context.MODE_PRIVATE);
        preferences.edit().putBoolean(StopwatchFragment.MILLIS_PREFERENCE, showMillis).apply();
        Toast.makeText(requireContext(), R.string.settings_udpated, Toast.LENGTH_SHORT);

        Intent exitIntent = new Intent(StopwatchRootFragment.ACTION_CHANGE_FRAGMENT).setPackage(requireContext().getPackageName());
        exitIntent.putExtra(StopwatchRootFragment.FRAGMENT_NAME_EXTRA, StopwatchRootFragment.FRAGMENT_MAIN_ID);
        requireContext().sendBroadcast(exitIntent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }
}