package com.app.toolbox.tools.stopwatch;

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
import com.app.toolbox.utils.IntentContentsMissingException;
import com.app.toolbox.utils.PageFragment;
import com.app.toolbox.view.navigation.NavigationItemView;

import java.util.Objects;

public class StopwatchRoot extends PageFragment {
    public static final String STRING_ID               = "toolbox.page.STOPWATCH_PAGE";
    static final String NOTIFICATION_CHANNEL_ID        = "toolbox.stopwatch.notificationChannel";
    public static final String ACTION_CHANGE_FRAGMENT  = "toolbox.stopwatch.changeFragment";
    public static final String FRAGMENT_NAME_EXTRA     = "toolbox.stopwatch.fragmentName";
    public static final String FRAGMENT_MAIN_ID        = "toolbox.stopwatch.fragmentMain";
    public static final String FRAGMENT_SETTINGS_ID    = "toolbox.stopwatch.fragmentSettings";

    private final StopwatchSettings mStopwatchSettings = new StopwatchSettings();
    private final StopwatchHome mStopwatchHome = new StopwatchHome();

    private final BroadcastReceiver mChangeFragmentReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            Fragment fragmentToSet = switch(Objects.requireNonNull(intent.getStringExtra(FRAGMENT_NAME_EXTRA))) {
                case FRAGMENT_MAIN_ID ->  mStopwatchHome;
                case FRAGMENT_SETTINGS_ID -> mStopwatchSettings;
                default -> throw new IntentContentsMissingException();
            };
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragmentToSet)
                    .commit();
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ContextCompat.registerReceiver(requireContext(), mChangeFragmentReceiver, new IntentFilter(ACTION_CHANGE_FRAGMENT), ContextCompat.RECEIVER_NOT_EXPORTED);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mStopwatchHome)
                .commit();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stopwatch_root, container, false);
    }

    @Override
    protected NavigationItemView createNavigationItem(Context context) {
        return new NavigationItemView(context, R.drawable.stopwatch_icon);
    }

    @Override
    protected String fragmentName() {
        return STRING_ID;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requireContext().unregisterReceiver(mChangeFragmentReceiver);
    }

}
