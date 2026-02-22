package com.app.toolbox.tools.timer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.app.toolbox.R;
import com.app.toolbox.utils.ParentPageFragment;
import com.app.toolbox.utils.Utils;
import com.app.toolbox.view.RemovableView;

public final class TimerHome extends Fragment {
    public LinearLayout mTimersList;

    private final BroadcastReceiver mTimersDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Data data = Data.check(intent.getParcelableExtra(TimerService.TIMERS_DATA_EXTRA, Data.class));
            mTimersList.removeAllViews();
            for(int i=0; i<data.len(); i++) {
                String title = data.titles[i];
                long delta = data.deltas[i];
                final int id = data.ids[i];

                RemovableView view = new RemovableView(context, true);
                view.setTitle(title);
                view.setContent(getString(R.string.time)+" "+Utils.longToTime(delta, false));
                view.setOnDeleteListener(new DeleteListener(requireContext(), id));
                mTimersList.addView(view);
            }
        }

        class DeleteListener implements View.OnClickListener {
            private final Context context;
            private final int mId;

            DeleteListener(Context context, int id) {
                this.context = context;
                this.mId = id;
            }

            @Override
            public void onClick(View v) {
                Intent deleteIntent = new Intent(context, TimerService.class);
                deleteIntent.setAction(TimerService.ACTION_STOP_TIMER)
                        .putExtra(TimerService.TIMER_ID_EXTRA, mId);
                requireContext().startForegroundService(deleteIntent);
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContextCompat.registerReceiver(requireContext(), mTimersDataReceiver, new IntentFilter(TimerService.ACTION_SEND_DATA), ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requireContext().unregisterReceiver(mTimersDataReceiver);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timer_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTimersList = view.findViewById(R.id.timers_list);

        view.findViewById(R.id.add_timer).setOnClickListener(v -> {
            Intent intent = new Intent(ParentPageFragment.actionChangePage(TimerRoot.STRING_ID)).setPackage(requireContext().getPackageName());
            intent.putExtra(ParentPageFragment.PAGE_CLASSNAME_EXTRA, TimerEditor.class.getName());
            requireContext().sendBroadcast(intent);
        });
    }
}
