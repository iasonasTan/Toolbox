package com.app.toolbox.utils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.app.toolbox.MainActivity;
import com.app.toolbox.R;
import com.app.toolbox.view.navigation.Navigable;
import com.app.toolbox.view.navigation.NavigationItemView;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Superclass of all fragments related to {@link MainActivity}
 * adds some features like: usages tracking, easy access to navigation item,
 * recognition by name e.t.c.
 */
public abstract class ToolFragment extends Fragment implements Comparable<ToolFragment>, Navigable {
    private long usages;
    private NavigationItemView navigationItem;
    private final String name;

    public ToolFragment() {
        name = fragmentName();
    }

    @Override
    public final NavigationItemView getNavItem(MainActivity mainActivity) throws IllegalArgumentException {
        if (navigationItem == null) {
            Log.d("initialization_spoil", "Initializing new navigation item view for fragment "+this);
            navigationItem = createNavigationItem(mainActivity);
            mainActivity.bnv.replaceItemWithSameIcon(navigationItem);
            navigationItem.setOnClickListener(av -> mainActivity.setFragment(MainActivity.getFragment(getClass())));
        }
        return navigationItem;
    }

    public void removeNavItem() {
        navigationItem=null;
    }

    @Override
    public final int compareTo(ToolFragment o) {
        return Long.compare(this.usages, o.usages);
    }

    public final String name() {
        return name;
    }

    public final long getUsages() {
        return usages;
    }

    @Override
    public void onResume() {
        super.onResume();
        getNavItem((MainActivity) requireActivity()).setCurrent(true);
        var scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        Log.d("action_spoil", "Check will start in 3 secs");
        scheduledExecutorService.schedule(() -> {
            Log.d("action_spoil", "Checking if fragment is currently visible...");
            if (isVisible()) {
                Log.d("action_spoil", "Fragment is visible! increasing usages...");
                usages++;
            }
        }, 3, TimeUnit.SECONDS);
        scheduledExecutorService.shutdown();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    abstract protected String fragmentName();

    abstract protected NavigationItemView createNavigationItem(Context context);

    @Override
    public void onPause() {
        super.onPause();
        getNavItem((MainActivity)requireActivity()).setCurrent(false);
    }

    public final void setUsages(long v) {
        usages = v;
    }

    public final void decreaseUsages() {
        usages -= 1;
    }
}
