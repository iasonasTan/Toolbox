package com.app.toolbox.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public abstract class ParentPageFragment extends PageFragment {
    private static final String ACTION_CHANGE_PAGE   = "toolbox.utils.ppf.changePage";
    public static final String PAGE_CLASSNAME_EXTRA = "toolbox.utils.ppf.pageNameExtra";

    public static String actionChangePage(String parentName) {
        return ACTION_CHANGE_PAGE+"__on__"+parentName;
    }

    private int mContainerId = 0;
    private List<Fragment> mFragments;

    private final BroadcastReceiver mSwitchPageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String pageName = Objects.requireNonNull(intent.getStringExtra(PAGE_CLASSNAME_EXTRA));
            Fragment fragment = getFragment(pageName);
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(mContainerId, fragment)
                    .commit();
        }
    };

    protected abstract List<Fragment> pages();
    protected abstract String defaultPageClassName();
    protected abstract int containerId();

    private Fragment getFragment(String className) {
        for (Fragment frag: mFragments) {
            String fragClassName = frag.getClass().getName();
            if(fragClassName.equals(className))
                return frag;
        }
        throw new NoSuchElementException("Could not find item with classname = "+className);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragments = pages();
        mContainerId = containerId();
        IntentFilter intentFilter = new IntentFilter(actionChangePage(fragmentName()));
        ContextCompat.registerReceiver(requireContext(), mSwitchPageReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED);
        Fragment fragment = getFragment(defaultPageClassName());
        if(savedInstanceState == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .add(mContainerId, fragment)
                    .commit();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requireContext().unregisterReceiver(mSwitchPageReceiver);
    }
}
