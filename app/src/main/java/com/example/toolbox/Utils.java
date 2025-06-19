package com.example.toolbox;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.toolbox.view.navigation.Navigable;
import com.example.toolbox.view.navigation.NavigationItemView;

import java.util.Locale;

public abstract class Utils {

    /**
     * Method that converts milliseconds as long to formated string.
     * @param millis input millis
     * @param millisOnFormat the string format will contain milliseconds in the end.
     * @return time format: HH:MM:SS:MM (hours:minutes:seconds:milliseconds)
     */
    public static String longToTime(long millis, boolean millisOnFormat) {
        final boolean INPUT_POSITIVE=millis>=0;
        millis=Math.abs(millis);
        long secs=0, mins=0, hours=0;
        while (millis>=1000) { secs+=1;millis-=1000; }
        while (secs>=60) { mins+=1;secs-=60; }
        while (mins>=60) { hours+=1;mins-=60; }
        millis/=10;
        String timeFormated = String.format(Locale.ENGLISH, "%s%02d:%02d:%02d",
                INPUT_POSITIVE?"":"-", hours, mins, secs);
        return millisOnFormat?timeFormated+"."+millis:timeFormated;
    }

    /**
     * Method that gives a {@link PendingIntent} which starts {@link MainActivity} and a {@link ToolFragment}
     * @param fragToShow the fragment you want to show
     * @return {@link PendingIntent} which starts activity and shows the fragment passed
     */
    public static PendingIntent createPendingIntent(ToolFragment fragToShow, Context context) {
        Intent intent=new Intent(context, MainActivity.class);
        intent.setAction(fragToShow.name());
        intent.putExtra("FRAGMENT_NAME", fragToShow.name());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    public abstract static class ToolFragment extends Fragment implements Comparable<ToolFragment>, Navigable {
        private long usages;
        protected NavigationItemView navigationItem;
        private String name;

        @Override
        public final NavigationItemView getNavItem(Context context) {
            if(navigationItem==null)
                navigationItem = getNavigationItem(context);
            return navigationItem;
        }

        @Override
        public final int compareTo(ToolFragment o) {
            return Long.compare(this.usages, o.usages);
        }

        public final String name() { return name; }
        public final long getUsages() { return usages; }

        @Override
        public void onResume() {
            super.onResume();
            navigationItem.setCurrent(true);
            usages++;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            navigationItem=getNavigationItem(requireContext());
            name=getName();
        }

        abstract protected String getName();
        abstract protected NavigationItemView getNavigationItem(Context context);

        @Override
        public void onPause() {
            super.onPause();
            navigationItem.setCurrent(false);
        }

        public final void setUsages(long v) {
            usages=v;
        }

        public void decreaseUsages() {
            usages--;
        }
    }
}
