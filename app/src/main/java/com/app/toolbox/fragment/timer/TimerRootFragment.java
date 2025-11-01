package com.app.toolbox.fragment.timer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.app.toolbox.R;
import com.app.toolbox.utils.IllegalIntentContentsException;
import com.app.toolbox.utils.IntentContentsMissingException;
import com.app.toolbox.utils.ToolFragment;
import com.app.toolbox.view.ItemView;
import com.app.toolbox.view.navigation.NavigationItemView;

import java.util.Objects;

public class TimerRootFragment extends ToolFragment {
    public static final String STRING_ID              = "toolbox.page.TIMER_PAGE";
    public static final String ACTION_CHANGE_FRAGMENT = "toolbox.timer.changeFragment";
    public static final String FRAGMENT_NAME_EXTRA    = "toolbox.timer.fragmentName";
    public static final String HOME_FRAGMENT          = "toolbox.timer.showHome";
    public static final String SETTER_FRAGMENT        = "toolbox.timer.showSetter";
    static final String NOTIFICATION_CHANNEL_ID       = "toolbox.timer.notificationChannel";

    private final TimerSetterFragment timerSetterFragment =new TimerSetterFragment();
    private final ActiveTimersFragment timersFragment=new ActiveTimersFragment();

    private final BroadcastReceiver mChangeFragmentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Fragment fragment;
            String action = Objects.requireNonNull(intent.getAction());
            if(!action.equals(ACTION_CHANGE_FRAGMENT)) throw new IllegalIntentContentsException();
            String fragmentName = Objects.requireNonNull(intent.getStringExtra(FRAGMENT_NAME_EXTRA));
            switch(fragmentName) {
                case HOME_FRAGMENT: fragment = timersFragment; break;
                case SETTER_FRAGMENT: fragment = timerSetterFragment; break;
                default: throw new IllegalIntentContentsException();
            }
            setFragment(fragment);
        }
        private void setFragment(Fragment fragment) {
            FragmentManager manager=getChildFragmentManager();
            FragmentTransaction transaction= manager.beginTransaction();
            for (Fragment frag: manager.getFragments()) {
                transaction.hide(frag);
            }
            transaction.show(fragment);
            transaction.commit();
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ContextCompat.registerReceiver(requireContext(), mChangeFragmentReceiver, new IntentFilter(ACTION_CHANGE_FRAGMENT), ContextCompat.RECEIVER_NOT_EXPORTED);
        NotificationChannel channel=new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Timer Notifications", NotificationManager.IMPORTANCE_LOW);
        NotificationManager manager= requireContext().getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
        getChildFragmentManager().beginTransaction().hide(timerSetterFragment).show(timersFragment).commit();

        Intent startTimerServiceIntent = new Intent(requireContext(), TimerService.class);
        startTimerServiceIntent.setAction(TimerService.UPDATE_TIMERS);
        requireContext().startForegroundService(startTimerServiceIntent);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentTransaction transaction= getChildFragmentManager().beginTransaction();
        transaction.add(R.id.timer_container, timerSetterFragment).hide(timerSetterFragment);
        transaction.add(R.id.timer_container, timersFragment).hide(timersFragment);
        transaction.commit();
        return inflater.inflate(R.layout.fragment_timer_root, container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requireContext().unregisterReceiver(mChangeFragmentReceiver);
    }

    @Override
    protected String fragmentName() {
        return STRING_ID;
    }

    @Override
    protected NavigationItemView createNavigationItem(Context context) {
        return new NavigationItemView(context, R.drawable.timer_icon);
    }

    public void addTimer(long time, String name) {
        long endTime=System.currentTimeMillis()+time;
        ItemView itemView=new ItemView(requireContext());
        itemView.setFont(requireContext(), R.font.poppins_bold);
        TimerService.Timer timer=new TimerService.Timer(requireContext(), itemView, endTime, name);
        TimerService.addTimer(timer);
        Intent intent = new Intent(requireContext(), TimerService.class);
        intent.setAction(TimerService.UPDATE_TIMERS);
        requireContext().startForegroundService(intent);
    }

    static abstract class InnerFragment extends Fragment {
        private TimerRootFragment parentFragment;

        protected TimerRootFragment getParentTimerFragment() {
            return parentFragment;
        }

        @Override
        public void onAttach(@NonNull Context context) {
            super.onAttach(context);

            Fragment parent = getParentFragment();
            if (parent instanceof TimerRootFragment) {
                this.parentFragment = (TimerRootFragment) parent;
            } else {
                throw new IllegalStateException("Parent fragment is not TimerFragment");
            }
        }
    }
}
