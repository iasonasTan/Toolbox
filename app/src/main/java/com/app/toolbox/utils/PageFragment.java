package com.app.toolbox.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.app.toolbox.MainActivity;
import com.app.toolbox.view.navigation.Navigable;
import com.app.toolbox.view.navigation.NavigationItemView;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This class represents a <b>page</b> {@link Fragment} that lives inside {@link MainActivity}
 * and represents <b>tool</b>. This type of fragment is linked with a {@link NavigationItemView}.<br>
 * Pages can also get changed via {@code Broadcast} with {@link Intent}.
 *
 * @see MainActivity#ACTION_SHOW_PAGE
 */
public abstract class PageFragment extends Fragment implements Comparable<PageFragment>, Navigable {
    /**
     * Time to wait to increase {@link #mUsages} of the fragment.
     * @see #onResume()
     */
    private static final int WAIT_TIME_SECONDS = 3;

    /**
     * Usages of the {@code Page}. They <i>increase</i> when fragment
     * is used for more than <i>{@link #WAIT_TIME_SECONDS} seconds continuously</i>.<br>
     * They get <i>saved</i> when {@code onDestroy()} is called
     * and <i>restored</i> when {@code onCreate()} is called.
     * <br><b>NOTE</b>: This field is getting updated by different threads.
     * 
     * @see #setUsages(long)
     */
    private volatile long mUsages;

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
     * Constructor initializes getPageName.
     * @see #fragmentName()
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
                intent.putExtra(MainActivity.PAGE_NAME_EXTRA, getPageName());
                context.sendBroadcast(intent);
            });
        }
        return mNavigationItem;
    }

    /**
     * Compares pages based on usages.
     * @param o the page to be compared.
     * @return a negative integer, zero, or a positive integer
     * as this object's usages are less than, equal to, or greater than the specified object's usages.
     */
    @Override
    public final int compareTo(PageFragment o) {
        return Long.compare(this.mUsages, o.mUsages);
    }

    public final String getPageName() {
        return mName;
    }

    public final long getPageUsages() {
        return mUsages;
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * This is generally tied to Activity.onResume of the containing Activity's lifecycle.
     * <br><b>Override:</b><br>
     * Updates page's usages and navigation item.
     */
    @Override
    public void onResume() {
        super.onResume();
        getNavItem(requireContext()).setCurrent(true);
        // noinspection all
        var scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        Log.d("checking_visibility", "Check will start in 3 secs");
        scheduledExecutorService.schedule(() -> {
            Log.d("checking_visibility", "Checking if fragment is currently visible...");
            if (isVisible()) {
                Log.d("checking_visibility", "Fragment is visible! increasing usages...");
                setUsages(mUsages+1);
            }
        }, WAIT_TIME_SECONDS, TimeUnit.SECONDS);
        scheduledExecutorService.shutdown();
    }

    /**
     * Abstract method used to request fragment's getPageName from sub-class
     * @return getPageName of this fragment as {@code String}
     */
    abstract protected String fragmentName();

    /**
     * Creates a navigation item for this page.
     * @param context context is required to create views.
     * @return {@link NavigationItemView} linked to this page
     */
    abstract protected NavigationItemView createNavigationItem(Context context);

    /**
     * Called when the Fragment is no longer resumed.  This is generally
     * tied to {@code Activity#onPause() Activity.onPause} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onPause() {
        super.onPause();
        getNavItem(requireContext()).setCurrent(false);
    }

    public final void setUsages(long v) {
        mUsages = v;
    }

    public final void decreaseUsages() {
        long usages = mUsages;
        mUsages = usages - 1;
    }
}
