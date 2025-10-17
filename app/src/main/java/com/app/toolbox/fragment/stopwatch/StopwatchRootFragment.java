package com.app.toolbox.fragment.stopwatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.app.toolbox.R;
import com.app.toolbox.utils.IllegalIntentContentsException;
import com.app.toolbox.utils.IntentContentsMissingException;
import com.app.toolbox.utils.ToolFragment;
import com.app.toolbox.view.navigation.NavigationItemView;

public class StopwatchRootFragment extends ToolFragment {
    public static final String STRING_ID        = "toolbox.page.STOPWATCH_PAGE";
    static final String NOTIFICATION_CHANNEL_ID = "toolbox.stopwatch.notificationChannel";
    static final String ACTION_CHANGE_FRAGMENT  = "toolbox.stopwatch.changeFragment";
    static final String FRAGMENT_NAME_EXTRA     = "toolbox.stopwatch.fragmentName";
    static final String FRAGMENT_MAIN_ID        = "toolbox.stopwatch.fragmentMain";
    static final String FRAGMENT_SETTINGS_ID    = "toolbox.stopwatch.fragmentSettings";

    // fragments
    private final SettingsFragment mSettingsFragment   = new SettingsFragment();
    private final StopwatchFragment mStopwatchFragment = new StopwatchFragment();

    private final BroadcastReceiver mChangeFragmentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String fragmentName = intent.getStringExtra(FRAGMENT_NAME_EXTRA);
            Fragment fragmentToSet;
            if(fragmentName==null) {
                throw new IntentContentsMissingException();
            } else if (fragmentName.equals(FRAGMENT_MAIN_ID)) {
                fragmentToSet = mStopwatchFragment;
            } else if (fragmentName.equals(FRAGMENT_SETTINGS_ID)) {
                fragmentToSet = mSettingsFragment;
            } else {
                throw new IllegalIntentContentsException();
            }
            getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, fragmentToSet).commit();
        }
    };

    @Override
    protected NavigationItemView createNavigationItem(Context context) {
        return new NavigationItemView(context, R.drawable.stopwatch_icon);
    }

    @Override
    protected String fragmentName() {
        return STRING_ID;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stopwatch_root, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ContextCompat.registerReceiver(requireContext(), mChangeFragmentReceiver, new IntentFilter(ACTION_CHANGE_FRAGMENT), ContextCompat.RECEIVER_NOT_EXPORTED);
        getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, mStopwatchFragment).commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requireContext().unregisterReceiver(mChangeFragmentReceiver);
    }
}
