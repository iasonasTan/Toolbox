package com.app.toolbox.tools.timer;

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

import com.app.toolbox.MainActivity;
import com.app.toolbox.R;
import com.app.toolbox.ReceiverOwner;
import com.app.toolbox.utils.IllegalIntentContentsException;
import com.app.toolbox.utils.PageFragment;
import com.app.toolbox.utils.Utils;
import com.app.toolbox.view.RemovableView;
import com.app.toolbox.view.navigation.NavigationItemView;

public class TimerFragment extends PageFragment implements ReceiverOwner {
    public static final String STRING_ID              = "toolbox.page.TIMER_PAGE";
    public static final String ACTION_CHANGE_FRAGMENT = "toolbox.timer.changeFragment";
    public static final String FRAGMENT_NAME_EXTRA    = "toolbox.timer.fragmentName";
    public static final String HOME_FRAGMENT          = "toolbox.timer.showHome";
    public static final String SETTER_FRAGMENT        = "toolbox.timer.showSetter";
    public static final String ACTION_NEW_TIMER       = "toolbox.timer.newTimer";
    public static final String TIMER_NAME_EXTRA       = "toolbox.timer.timeName";
    public static final String TIME_MILLIS_EXTRA      = "toolbox.timer.timeMillis";
    static final String NOTIFICATION_CHANNEL_ID       = "toolbox.timer.notificationChannel";

    private final AddTimerFragment addTimerFragment = new AddTimerFragment();
    private final TimersListFragment timersFragment     = new TimersListFragment();

    private final BroadcastReceiver mAddTimerReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            Utils.checkIntent(intent, TIMER_NAME_EXTRA, TIME_MILLIS_EXTRA);
            addTimer(intent.getLongExtra(TIME_MILLIS_EXTRA, /*fallback time*/2000),
                    intent.getStringExtra(TIMER_NAME_EXTRA));
        }
    };

    private final BroadcastReceiver mChangeFragmentReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            Utils.checkIntent(intent, FRAGMENT_NAME_EXTRA);
            String fragmentName = intent.getStringExtra(FRAGMENT_NAME_EXTRA);
            //noinspection all
            Fragment fragment = switch(fragmentName) {
                case HOME_FRAGMENT -> timersFragment;
                case SETTER_FRAGMENT -> addTimerFragment;
                default -> throw new IllegalIntentContentsException();
            };
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
        ContextCompat.registerReceiver(requireContext(), mAddTimerReceiver, new IntentFilter(ACTION_NEW_TIMER), ContextCompat.RECEIVER_NOT_EXPORTED);
        MainActivity.sReceiverOwners.add(this);

        NotificationChannel channel=new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Timer Notifications", NotificationManager.IMPORTANCE_LOW);
        requireContext().getSystemService(NotificationManager.class).createNotificationChannel(channel);
        getChildFragmentManager().beginTransaction().hide(addTimerFragment).show(timersFragment).commit();

        Intent startTimerServiceIntent = new Intent(requireContext(), TimerService.class).setAction(TimerService.ACTION_UPDATE_TIMERS);
        requireContext().startForegroundService(startTimerServiceIntent);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentTransaction transaction= getChildFragmentManager().beginTransaction();
        transaction.add(R.id.timer_container, addTimerFragment).hide(addTimerFragment);
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
        RemovableView removableView =new RemovableView(requireContext());
        removableView.setFont(requireContext(), R.font.poppins_bold);
        TimerService.Timer timer=new TimerService.Timer(requireContext(), removableView, endTime, name);
        TimerService.addTimer(timer);

        Intent intent = new Intent(requireContext(), TimerService.class);
        intent.setAction(TimerService.ACTION_UPDATE_TIMERS);
        requireContext().startForegroundService(intent);
    }

    @Override
    public void unregisterReceivers(Context context) {
        context.unregisterReceiver(mAddTimerReceiver);
        context.unregisterReceiver(mChangeFragmentReceiver);
    }

}
