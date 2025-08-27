package com.app.toolbox.fragment.timer;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.toolbox.R;


public class ActiveTimersFragment extends TimerFragment.InnerFragment {
    public LinearLayout timersList;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) loadTimersToUI();
    }

    public void loadTimersToUI() {
        Log.d("action_spoil", "Loading timers to gui...");
        for (Timer t : TimerManager.instance) {
            View timerView = t.getView();
            LinearLayout parent = (LinearLayout) timerView.getParent();
            if (parent != null) parent.removeView(timerView);
            timersList.addView(timerView);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // noinspection all
        view.findViewById(R.id.add_timer).setOnClickListener(l -> {
            getParentTimerFragment().setFragment(getParentTimerFragment().timerSetterFragment);
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_timer_list, container, false);
        timersList = rootView.findViewById(R.id.timers_list);
        return rootView;
    }
}
