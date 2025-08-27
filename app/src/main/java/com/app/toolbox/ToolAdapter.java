package com.app.toolbox;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ToolAdapter extends FragmentStateAdapter {

    public ToolAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return MainActivity.fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return MainActivity.fragments.size();
    }
}
