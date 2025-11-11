package com.app.toolbox.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.app.toolbox.MainActivity;
import com.app.toolbox.view.navigation.Navigable;
import com.app.toolbox.view.navigation.NavigationItemView;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This class represents a <b>parent</b> {@link Fragment} that lives inside {@link MainActivity}
 * and contains a <b>tool</b>. This type of fragment has the below features.<br>
 * Pages can also get changed via {@code Broadcasts} with {@link Intent}.
 *
 * @see MainActivity#ACTION_SHOW_PAGE
 */
public abstract class PageFragment extends Fragment implements Comparable<PageFragment>, Navigable {
    /**
     * Usages of the {@code Page}. They <i>increase</i> when fragment
     * is used for more than <i>3 seconds continuously</i>.<br>
     * They get <i>saved</i> when {@code onDestroy()} is called
     * and <i>restored</i> when {@code onCreate()} is called.
     * 
     * @see #setUsages(long)
     */
    private long mUsages;

    /**
     * {@code Item} that navigates to this page.
     * 
     * @see #createNavigationItem(Context) 
     */
    private NavigationItemView mNavigationItem;

    /**
     * Name of fragment. Used for identifying fragment.
     * 
     * @see #fragmentName() 
     */
    private final String mName;

    /**
     * Constructor initializes name.
     */
    public PageFragment() {
        mName = fragmentName();
    }

    /**
     * Responsible to create or return {@link NavigationItemView} that
     * points to this {@code Page}. If instantiation is needed, instance
     * is get by abstract method{@link #createNavigationItem(Context)}.
     *
     * @see #createNavigationItem(Context)
     * 
     * @return {@code NavigationItem} that contains <i>this</i> {@code Page's} info.
     */
    @Override 
    public final NavigationItemView getNavItem(Context context) {
        if (mNavigationItem == null) {
            Log.d("initialization_spoil", "Initializing new navigation item for fragment "+this);
            mNavigationItem = createNavigationItem(context);
            mNavigationItem.setOnClickListener(av -> {
                Intent intent = new Intent(MainActivity.SWITCH_PAGE).setPackage(context.getPackageName());
                intent.putExtra(MainActivity.PAGE_NAME_EXTRA, name());
                context.sendBroadcast(intent);
            });
        }
        return mNavigationItem;
    }

    @Override
    public final int compareTo(PageFragment o) {
        return Long.compare(this.mUsages, o.mUsages);
    }

    public final String name() {
        return mName;
    }

    public final long getUsages() {
        return mUsages;
    }

    @Override
    public void onResume() {
        super.onResume();
        getNavItem(requireContext()).setCurrent(true);
        var scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        Log.d("checking_visibility", "Check will start in 3 secs");
        scheduledExecutorService.schedule(() -> {
            Log.d("checking_visibility", "Checking if fragment is currently visible...");
            if (isVisible()) {
                Log.d("checking_visibility", "Fragment is visible! increasing usages...");
                mUsages++;
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
        getNavItem(requireContext()).setCurrent(false);
    }

    public final void setUsages(long v) {
        mUsages = v;
    }

    public final void decreaseUsages() {
        mUsages -= 1;
    }
}
