package com.app.toolbox.tools.stopwatch;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.app.toolbox.R;
import com.app.toolbox.utils.ParentPageFragment;
import com.app.toolbox.view.navigation.NavigationItemView;

import java.util.List;

public class StopwatchRoot extends ParentPageFragment {
    public static final String STRING_ID               = "toolbox.page.STOPWATCH_PAGE";
    static final String NOTIFICATION_CHANNEL_ID        = "toolbox.stopwatch.notificationChannel";

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
    protected List<Fragment> pages() {
        return List.of(
                new StopwatchSettings(),
                new StopwatchHome()
        );
    }

    @Override
    protected String defaultPageClassName() {
        return StopwatchHome.class.getName();
    }

    @Override
    protected int containerId() {
        return R.id.fragment_container;
    }
}
