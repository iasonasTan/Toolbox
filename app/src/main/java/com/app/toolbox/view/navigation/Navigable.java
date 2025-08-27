package com.app.toolbox.view.navigation;

import com.app.toolbox.MainActivity;

public interface Navigable {
    NavigationItemView getNavItem(MainActivity mainActivity) throws IllegalArgumentException;
}
