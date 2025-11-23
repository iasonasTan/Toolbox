package com.app.toolbox;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.app.toolbox.tools.CalculatorFragment;
import com.app.toolbox.tools.RandNumGenFragment;
import com.app.toolbox.tools.notepad.EditorFragment;
import com.app.toolbox.tools.notepad.NotepadFragment;
import com.app.toolbox.tools.stopwatch.StopwatchRootFragment;
import com.app.toolbox.tools.stopwatch.StopwatchService;
import com.app.toolbox.tools.timer.TimerRootFragment;
import com.app.toolbox.utils.IntentContentsMissingException;
import com.app.toolbox.utils.PageFragment;
import com.app.toolbox.view.navigation.NavigationItemView;
import com.app.toolbox.view.navigation.NavigationView;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Properties;

public final class MainActivity extends AppCompatActivity {
    private static final String USAGES_FILE_NAME = "usages.dat";
    public static final String SWITCH_PAGE       = "toolbox.mainActivity.switchPage";
    public static final String PAGE_NAME_EXTRA   = "toolbox.mainActivity.pageName";
    public static final String CONFIG_VIEW_PAGER = "toolbox.mainActivity.configViewPager";
    public static final String USER_INPUT_EXTRA  = "toolbox.mainActivity.scrollingEnabled";
    public static final String ACTION_SHOW_PAGE  = "toolbox.mainActivity.showPage";

    public NavigationView bnv;
    private List<PageFragment> fragments;
    private ViewPager2 mViewPager2;

    // listens on event CHANGE_PAGE and CONFIG_VIEW_PAGER
    private final BroadcastReceiver mSwitchPageReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            String action = Objects.requireNonNull(intent.getAction());
            switch(action) {
                case SWITCH_PAGE -> setPageByName(intent.getStringExtra(PAGE_NAME_EXTRA));
                case CONFIG_VIEW_PAGER -> mViewPager2.setUserInputEnabled(intent.getBooleanExtra(USER_INPUT_EXTRA, true));
                default -> throw new IntentContentsMissingException();
            }
        }
    };

    public void setPageByName(String requiredName) {
        for(PageFragment pageFragment : fragments) {
            if(requiredName.equals(pageFragment.getPageName())) {
                setPageByName(pageFragment);
                return;
            }
        }
        StringBuilder codes=new StringBuilder();
        fragments.forEach(f -> codes.append('\t').append(f.getPageName()).append("\n"));
        throw new NoSuchElementException("could not found item with getPageName "+requiredName+ "\n"+ "Available names are:\n"+ codes);
    }

    @Deprecated(forRemoval = true)
    public PageFragment getFragment(Class<? extends PageFragment> clazz) {
        if (clazz==null) throw new NullPointerException("Class is null.");
        for (PageFragment fragment: fragments) {
            if (fragment.getClass().isAssignableFrom(clazz)) {
                return fragment;
            }
        }
        throw new NoSuchElementException("No such element. class: "+clazz);
    }

    @Override
    public void onStop() {
        super.onStop();
        storeFragmentUsages();
    }

    private void storeFragmentUsages() {
        Properties usages=new Properties();
        for(PageFragment tf: fragments) {
            usages.setProperty(tf.getClass().toString(), tf.getPageUsages()+"");
        }
        try {
            File file = new File(getApplicationContext().getFilesDir(), USAGES_FILE_NAME);
            if (!file.exists())
                // noinspection all
                file.createNewFile();
            FileOutputStream fos=new FileOutputStream(file);
            usages.store(fos, "usages");
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initFragments() {
        List<PageFragment> loc_fragments=new ArrayList<>();
        Collections.addAll(loc_fragments, new TimerRootFragment(), new StopwatchRootFragment(),
                new CalculatorFragment(), new NotepadFragment(), new RandNumGenFragment());
        loadFragmentUsages(loc_fragments);

        // sort fragments by usages
        loc_fragments.sort((o1, o2) -> Long.compare(o2.getPageUsages(), o1.getPageUsages()));
        fragments=Collections.unmodifiableList(loc_fragments);

        ToolAdapter fragmentStateAdapter = new ToolAdapter(this);
        mViewPager2 =findViewById(R.id.fragment_adapter);
        mViewPager2.setAdapter(fragmentStateAdapter);

        // remove one usage from first fragment
        // because first fragment shows automatically it increases usages
        fragments.get(0).decreaseUsages();
    }

    private void loadFragmentUsages(List<PageFragment> loc_fragments) {
        Properties usages=new Properties();
        try {
            File usage_file=new File(getFilesDir(), USAGES_FILE_NAME);
            if(!usage_file.exists()) {
                //noinspection all
                usage_file.createNewFile();
            }
            FileInputStream fis=new FileInputStream(usage_file);
            usages.load(fis);
            fis.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        usages.forEach((k, v) -> {
            final String className=(String)k;
            final long classUsages=Long.parseLong((String)v);

            for(PageFragment frag: loc_fragments) {
                if(className.equals(frag.getClass().toString()))
                    frag.setUsages(classUsages);
            }
        });
    }

    private void initNavBar() {
        Log.d("initialization", "Initializing navigation bar.");
        bnv = findViewById(R.id.bottom_navigation);
        fragments.forEach(frag -> {
            NavigationItemView nv=frag.getNavItem(this);
            bnv.replaceItemWithSameIcon(nv);
            //bnv.addItem(nv);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getApplicationContext().unregisterReceiver(mSwitchPageReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DynamicColors.applyToActivityIfAvailable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EdgeToEdge.enable(this);

        IntentFilter intentFilter = new IntentFilter(SWITCH_PAGE);
        intentFilter.addAction(CONFIG_VIEW_PAGER);
        ContextCompat.registerReceiver(getApplicationContext(), mSwitchPageReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED);

        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1007);

        initFragments();
        initNavBar();
        handleIntent(getIntent());

        findViewById(R.id.settings_button).setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        new Thread(this::checkForUpdates).start();

        Greeter greeter = new Greeter(this);
        greeter.greet();
    }

    private void checkForUpdates() {
        try {
            Log.d("net-test", "Checking for updates...");
            // noinspection all
            Socket socket = new Socket("iasonas.duckdns.org", 1422);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write("get_version_code?toolbox");
            writer.append('\n');
            writer.flush();
            String latest_version_code = reader.readLine();
            Log.d("net-test", "Latest version code is " + latest_version_code);
            PackageManager pm = getPackageManager();
            PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
            Log.d("net-test", "Checking package info...");
            if (info != null && info.versionName != null) {
                Log.d("net-test", "Comparing version codes...");
                double latestVersion = Double.parseDouble(latest_version_code);
                double appVersion = Double.parseDouble(info.versionName);
                Log.d("net-test", "Latest version: " + latestVersion);
                Log.d("net-test", "App version: " + appVersion);
                if (latestVersion > appVersion) {
                    Log.d("net-test", "Asking user to update...");
                    runOnUiThread(() -> new MaterialAlertDialogBuilder(this)
                            .setTitle("Update available!")
                            .setMessage("Do you want to update?")
                            .setCancelable(false)
                            .setNegativeButton("Not now.", (a, b) -> a.dismiss())
                            .setPositiveButton("Yes!", (a, b) -> {
                                a.dismiss();
                                String url = "http://iasonas.duckdns.org/download_toolbox/index.html";
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            })
                            .show());
                }
            }
            reader.close();
            writer.write("disconnect");
            writer.append('\n');
            writer.flush();
            writer.close();
            socket.close();
        } catch (IOException | PackageManager.NameNotFoundException e) {
            Log.d("net-test", "Exception: "+e);
        }
    }

    private void handleIntent(Intent intent) {
        if(intent==null) return;
        final String action=Objects.requireNonNull(intent.getAction());
        final String name=intent.getStringExtra(PAGE_NAME_EXTRA);
        final String type = intent.getType();

        if(name!=null) setPageByName(name);

        if (action.equals("toolbox.mainActivity.startStopwatch")) {
            setPageByName(StopwatchRootFragment.STRING_ID);
            Intent startTimerIntent = new Intent(this, StopwatchService.class);
            startTimerIntent.setAction(StopwatchService.ACTION_START_TIMER);
            startForegroundService(startTimerIntent);
        }

        if (action.equals("toolbox.mainActivity.newNote")) {
            setPageByName(NotepadFragment.STRING_ID, () -> {
                Intent intent1 = new Intent(NotepadFragment.ACTION_CHANGE_FRAGMENT).setPackage(getPackageName());
                intent1.putExtra(NotepadFragment.STRING_ID, NotepadFragment.FRAGMENT_EDITOR);
                sendBroadcast(intent1);

                Intent newNoteIntent = new Intent(EditorFragment.ACTION_OPEN_FILE).setPackage(getPackageName());
                newNoteIntent.putExtra(EditorFragment.FILE_PATH_EXTRA, EditorFragment.PATH_NONE_EXTRA);
                sendBroadcast(newNoteIntent);
            });
        }

        if (action.equals(Intent.ACTION_SEND) && "text/plain".equals(type)) {
            String text = intent.getStringExtra(Intent.EXTRA_TEXT);
            setPageByName(NotepadFragment.STRING_ID, () -> {
                Intent changeFragmentIntent = new Intent(NotepadFragment.ACTION_CHANGE_FRAGMENT).setPackage(getPackageName());
                changeFragmentIntent.putExtra(NotepadFragment.STRING_ID, NotepadFragment.FRAGMENT_EDITOR);
                sendBroadcast(changeFragmentIntent);

                Intent newNoteIntent = new Intent(EditorFragment.ACTION_OPEN_FILE).setPackage(getPackageName());
                newNoteIntent.putExtra(EditorFragment.FILE_PATH_EXTRA, EditorFragment.PATH_NONE_EXTRA);
                newNoteIntent.putExtra(EditorFragment.TEXT_EXTRA, text);
                sendBroadcast(newNoteIntent);
            });
        }

        if (action.equals("toolbox.mainActivity.addTimer")||action.equals("com.android.intent.action.SET_TIMER")) {
            setPageByName(TimerRootFragment.STRING_ID, () -> {
                Intent changeFragmentIntent = new Intent(TimerRootFragment.ACTION_CHANGE_FRAGMENT).setPackage(getPackageName());
                changeFragmentIntent.putExtra(NotepadFragment.STRING_ID, TimerRootFragment.SETTER_FRAGMENT);
                sendBroadcast(changeFragmentIntent);
            });
        }
    }

    public void setPageByName(String name, Runnable action) {
        setPageByName(name);
        new Handler(Looper.getMainLooper()).postDelayed(action, 200);
    }

    public void setPageByName(PageFragment fragment) {
        mViewPager2.setCurrentItem(fragments.indexOf(fragment));
    }

    public class ToolAdapter extends FragmentStateAdapter {

        public ToolAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragments.get(position);
        }

        @Override
        public int getItemCount() {
            return fragments.size();
        }
    }
}