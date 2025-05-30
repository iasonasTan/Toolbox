package com.example.toolbox.fragment;

import androidx.fragment.app.Fragment;

import com.example.toolbox.MainActivity;
import com.example.toolbox.view.navigation.Navigable;
import com.example.toolbox.view.navigation.NavigationItemView;

public abstract class ToolFragment extends Fragment
        implements Comparable<ToolFragment>, Navigable {

    private long usages;
    protected NavigationItemView navigationItem;

    public ToolFragment(NavigationItemView view) {
        navigationItem=view;
    }

    @Override
    public final NavigationItemView getNavItem() {
        return navigationItem;
    }

    public final long getUsages() {
        return usages;
    }

    @Override
    public void onResume() {
        super.onResume();
        navigationItem.setCurrent(true);
        usages++;
    }

    @Override
    public void onPause() {
        super.onPause();
        navigationItem.setCurrent(false);
    }

    @Override
    public final int compareTo(ToolFragment o) {
        return Long.compare(this.usages, o.usages);
    }

    public final void setUsages(long v) {
        usages=v;
    }

    public void decreaseUsages() {
        usages--;
    }
}
