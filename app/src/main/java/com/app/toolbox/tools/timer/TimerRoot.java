package com.app.toolbox.tools.timer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
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

public final class TimerRoot extends ParentPageFragment {
    public static final String STRING_ID              = "toolbox.page.TIMER_PAGE";
    static final String NOTIFICATION_CHANNEL_ID       = "toolbox.timer.notificationChannel";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timer_root, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NotificationChannel channel=new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Timer Notifications", NotificationManager.IMPORTANCE_LOW);
        requireContext()
                .getSystemService(NotificationManager.class)
                .createNotificationChannel(channel);
    }

    @Override
    protected List<Fragment> pages() {
        return List.of(
                new TimerHome(),
                new TimerEditor()
        );
    }

    @Override
    protected String defaultPageClassName() {
        return TimerHome.class.getName();
    }

    @Override
    protected int containerId() {
        return R.id.timer_container;
    }

    @Override
    protected NavigationItemView createNavigationItem(Context context) {
        return new NavigationItemView(context, R.drawable.timer_icon);
    }

    @Override
    protected String fragmentName() {
        return STRING_ID;
    }

}
