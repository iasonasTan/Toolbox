package com.app.toolbox.tools.timer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.app.toolbox.R;


public class ActiveTimersFragment extends Fragment {
    public LinearLayout timersList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timer_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.add_timer).setOnClickListener(new OnAddTimerPressed());
        timersList = view.findViewById(R.id.timers_list);
    }

    private final class OnAddTimerPressed implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(TimerRootFragment.ACTION_CHANGE_FRAGMENT).setPackage(requireContext().getPackageName());
            intent.putExtra(TimerRootFragment.FRAGMENT_NAME_EXTRA, TimerRootFragment.SETTER_FRAGMENT);
            requireContext().sendBroadcast(intent);
        }
    }

    public void loadTimersToUI() {
        Log.d("timer_loading", "Loading timers to gui...");
        for (TimerService.Timer t: TimerService.getTimers()) {
            View timerView = t.getView();
            LinearLayout parent = (LinearLayout) timerView.getParent();
            if (parent != null) parent.removeView(timerView);
            timersList.addView(timerView);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) loadTimersToUI();
    }
}
