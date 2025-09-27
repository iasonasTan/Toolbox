package com.app.toolbox.fragment.timer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.app.toolbox.R;
import com.app.toolbox.utils.ToolFragment;
import com.app.toolbox.view.ItemView;
import com.app.toolbox.view.navigation.NavigationItemView;

public class TimerFragment extends ToolFragment {
    static final String NOTIFICATION_CHANNEL_ID="timer_channel";
    static Ringtone ringtone;

    final TimerSetterFragment timerSetterFragment =new TimerSetterFragment();
    final ActiveTimersFragment timersFragment=new ActiveTimersFragment();

    @Override
    protected String fragmentName() {
        return "TIMER_FRAGMENT";
    }

    @Override
    protected NavigationItemView createNavigationItem(Context context) {
        return new NavigationItemView(context, R.drawable.timer_icon);
    }

    private void initNotificationChannel() {
        NotificationChannel channel=new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Timer Notifications", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager= requireContext().getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtone=RingtoneManager.getRingtone(requireContext(), uri);

        initNotificationChannel();
        setFragment(timersFragment);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentTransaction transaction= getChildFragmentManager().beginTransaction();
        transaction.add(R.id.timer_container, timerSetterFragment).hide(timerSetterFragment);
        transaction.add(R.id.timer_container, timersFragment).hide(timersFragment).commit();

        return inflater.inflate(R.layout.fragment_timer_root, container, false);
    }

    void setFragment(Fragment fragment) {
        FragmentManager manager=getChildFragmentManager();
        FragmentTransaction transaction= manager.beginTransaction();
        for (Fragment frag: manager.getFragments()) {
            transaction.hide(frag);
        }
        transaction.show(fragment);
        transaction.commit();
    }

    public void addTimer(long time, String name) {
        long endTime=System.currentTimeMillis()+time;
        ItemView itemView=new ItemView(requireContext());
        Timer timer=new Timer(requireContext(), itemView, endTime, name);
        TimerManager.instance.add(timer);
    }

    static abstract class InnerFragment extends Fragment {
        private TimerFragment parentFragment;

        protected TimerFragment getParentTimerFragment() {
            return parentFragment;
        }

        @Override
        public void onAttach(@NonNull Context context) {
            super.onAttach(context);

            Fragment parent = getParentFragment();
            if (parent instanceof TimerFragment) {
                this.parentFragment = (TimerFragment) parent;
            } else {
                throw new IllegalStateException("Parent fragment is not TimerFragment");
            }
        }
    }
}
