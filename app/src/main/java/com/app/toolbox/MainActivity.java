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
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.app.toolbox.tools.calculator.CalculatorFragment;
import com.app.toolbox.tools.notepad.EditorFragment;
import com.app.toolbox.tools.notepad.NotepadFragment;
import com.app.toolbox.tools.randnumgen.RandNumGenFragment;
import com.app.toolbox.tools.stopwatch.StopwatchFragment;
import com.app.toolbox.tools.stopwatch.StopwatchService;
import com.app.toolbox.tools.timer.TimerFragment;
import com.app.toolbox.utils.IntentContentsMissingException;
import com.app.toolbox.utils.PageFragment;
import com.app.toolbox.utils.Utils;
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
import java.util.function.Consumer;

/*
 * Integer Codes:
 * Permissions: 1000-1999
 * PendingIntents: 2000-2999, action.hashcode
 * Launch activity: 3000-3999
 */

public final class MainActivity extends AppCompatActivity {
    private static final String USAGES_FILE_NAME = "usages.dat";
    public static final String SWITCH_PAGE       = "toolbox.mainActivity.switchPage";
    public static final String PAGE_NAME_EXTRA   = "toolbox.mainActivity.pageName";
    public static final String CONFIG_VIEW_PAGER = "toolbox.mainActivity.configViewPager";
    public static final String USER_INPUT_EXTRA  = "toolbox.mainActivity.scrollingEnabled";
    public static final String ACTION_SHOW_PAGE  = "toolbox.mainActivity.showPage";

    public static final List<ReceiverOwner> sReceiverOwners = new ArrayList<>();
    private final PageManager mManager = new PageManager();
    private NavigationView bnv;
    private ViewPager2 mViewPager2;

    private final BroadcastReceiver mSwitchPageReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            String action = Objects.requireNonNull(intent.getAction());
            switch(action) {
                case SWITCH_PAGE -> mManager.setPageByName(intent.getStringExtra(PAGE_NAME_EXTRA));
                case CONFIG_VIEW_PAGER -> mViewPager2.setUserInputEnabled(intent.getBooleanExtra(USER_INPUT_EXTRA, true));
                default -> throw new IntentContentsMissingException();
            }
        }
    };

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
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1000);

        mManager.initFragments();
        initViews();

        if(savedInstanceState==null) {
            new IntentProcessor(this, mManager).processIntent(getIntent());
            //new UpdateChecker().checkVersionAsynchronously();
            new ApplicationGreeter(this).greet();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mManager.storeFragmentUsages();
    }

    private void initViews() {
        Log.d("initialization", "Initializing navigation bar.");
        bnv = findViewById(R.id.bottom_navigation);
        mManager.forEach(frag -> {
            NavigationItemView view = frag.getNavItem(this);
            sReceiverOwners.add(view);
            bnv.addView(view);
        });
        findViewById(R.id.settings_button).setOnClickListener(v -> {
            Utils.execute(() -> getApplicationContext()
                    .getSystemService(Vibrator.class)
                    .vibrate(VibrationEffect.createOneShot(70, VibrationEffect.EFFECT_DOUBLE_CLICK)));
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getApplicationContext().unregisterReceiver(mSwitchPageReceiver);
        sReceiverOwners.forEach(r -> Utils.execute(() -> r.unregisterReceivers(this)));
        sReceiverOwners.clear();
    }

    public final class PageManager {
        private List<PageFragment> fragments;

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
            Collections.addAll(loc_fragments, new TimerFragment(), new StopwatchFragment(),
                    new CalculatorFragment(), new NotepadFragment(), new RandNumGenFragment());
            loadFragmentUsages(loc_fragments);

            // sort fragments by usages
            loc_fragments.sort((o1, o2) -> Long.compare(o2.getPageUsages(), o1.getPageUsages()));
            fragments=Collections.unmodifiableList(loc_fragments);

            var fragmentStateAdapter = new PageManager.ToolAdapter(MainActivity.this);
            mViewPager2 =findViewById(R.id.fragment_adapter);
            mViewPager2.setAdapter(fragmentStateAdapter);

            // remove one usage from first fragment
            // because first fragment shows automatically it increases usages
            fragments.get(0).decreaseUsages();
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

        public void setPageByName(String name, Runnable action) {
            mManager.setPageByName(name);
            new Handler(Looper.getMainLooper()).postDelayed(action, 200);
        }

        private void loadFragmentUsages(List<PageFragment> loc_fragments) {
            Properties usages = createProperties();

            usages.forEach((k, v) -> {
                final String className=(String)k;
                final long classUsages=Long.parseLong((String)v);

                for(PageFragment frag: loc_fragments) {
                    if(className.equals(frag.getClass().toString()))
                        frag.setUsages(classUsages);
                }
            });
        }

        private Properties createProperties() {
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
            return usages;
        }

        public void forEach(Consumer<PageFragment> cons) {
            fragments.forEach(cons);
        }
    }

    /**
     * Deprecated because iasonas.duckdns.org isn't working anymore.
     * Cause: ISP blocked ports.
     */
    @SuppressWarnings("unused")
    @Deprecated
    private final class UpdateChecker {
        public void checkVersionAsynchronously() {
            new Thread(this::checkVersion).start();
        }

        public void checkVersion() {
            try {
                String latestVersion = requestVersionFromServer();
                String appVersion = getApplicationVersion();
                if(needsUpdate(appVersion, latestVersion))
                    showUpdateDialog();
            } catch (IOException | PackageManager.NameNotFoundException ignored) {
                // causes: no internet, DNS problem or server is closed
                // ignore, no version check
            }
        }

        private boolean needsUpdate(String appV, String latV) {
            Log.d("net-test", "Comparing version codes...");
            double latestVersion = Double.parseDouble(latV);
            double appVersion = Double.parseDouble(appV);
            Log.d("net-test", "Latest version: " + latestVersion + ", App version: " + appVersion);
            return appVersion < latestVersion;
        }

        private String getApplicationVersion() throws PackageManager.NameNotFoundException {
            PackageManager pm = getPackageManager();
            PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
            if(info==null||info.versionName==null)
                throw new NullPointerException("Cannot get version code. Null returned by android system.");
            return info.versionName;
        }

        private String requestVersionFromServer() throws IOException {
            BufferedWriter writer = null;
            BufferedReader reader = null;
            Socket socket = null;
            try {
                Log.d("net-test", "Checking for updates...");
                socket = new Socket("iasonas.duckdns.org", 1422);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                writer.write("get_version_code?toolbox");
                writer.append('\n');
                writer.flush();
                String latest_version_code = reader.readLine();
                Log.d("net-test", "Latest version code is " + latest_version_code);
                return latest_version_code;
            } finally {
                if(reader != null && !socket.isClosed()) {
                    reader.close();
                }
                if(writer!= null && !socket.isClosed()) {
                    writer.write("disconnect\n");
                    writer.flush();
                    writer.close();
                }
                if(socket != null && !socket.isClosed()) {
                    socket.close();
                }
            }
        }

        private void showUpdateDialog() {
            Log.d("net-test", "Asking user to update...");
            runOnUiThread(() -> new MaterialAlertDialogBuilder(MainActivity.this)
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

    private static final class IntentProcessor {
        private final Context context;
        private final PageManager mManager;

        private IntentProcessor(Context context, PageManager manager){
            this.context = context;
            mManager = manager;
        }

        public void processIntent(Intent intent) {
            if(intent==null)
                return;
            final String action=Objects.requireNonNull(intent.getAction());
            final String name=intent.getStringExtra(PAGE_NAME_EXTRA);
            final String type = intent.getType();
            if(name!=null&&action.equals(SWITCH_PAGE))
                mManager.setPageByName(name);
            if(action.equals("toolbox.mainActivity.startStopwatch")) {
                showStopwatch(true);
            } else if (action.equals("toolbox.mainActivity.newNote")) {
                createNote();
            } else if (action.equals(Intent.ACTION_SEND) && "text/plain".equals(type)) {
                appendToNotepad(intent);
            } else if (action.equals("toolbox.mainActivity.addTimer")||action.equals("com.android.intent.action.SET_TIMER")) {
                addTimer();
            } else if (action.equals("toolbox.mainActivity.showStopwatch")) {
                showStopwatch(false);
            } else if (action.equals("toolbox.rng.widget.showRNG")) {
                showRNG();
            }
        }

        private void showRNG() {
            mManager.setPageByName(RandNumGenFragment.STRING_ID);
        }

        private void addTimer() {
            mManager.setPageByName(TimerFragment.STRING_ID, () -> {
                Intent changeFragmentIntent = new Intent(TimerFragment.ACTION_CHANGE_FRAGMENT).setPackage(context.getPackageName());
                changeFragmentIntent.putExtra(TimerFragment.FRAGMENT_NAME_EXTRA, TimerFragment.SETTER_FRAGMENT);
                context.sendBroadcast(changeFragmentIntent);
            });
        }

        private void appendToNotepad(Intent intent) {
            String text = intent.getStringExtra(Intent.EXTRA_TEXT);
            mManager.setPageByName(NotepadFragment.PAGE_ID, () -> {
                Intent changeFragmentIntent = new Intent(NotepadFragment.ACTION_CHANGE_FRAGMENT).setPackage(context.getPackageName());
                changeFragmentIntent.putExtra(NotepadFragment.PAGE_ID, NotepadFragment.FRAGMENT_EDITOR);
                context.sendBroadcast(changeFragmentIntent);

                Intent newNoteIntent = new Intent(EditorFragment.ACTION_OPEN_FILE).setPackage(context.getPackageName());
                newNoteIntent.putExtra(EditorFragment.FILE_PATH_EXTRA, EditorFragment.PATH_NONE_EXTRA);
                newNoteIntent.putExtra(EditorFragment.TEXT_EXTRA, text);
                context.sendBroadcast(newNoteIntent);
            });
        }

        private void createNote() {
            mManager.setPageByName(NotepadFragment.PAGE_ID, () -> {
                Intent intent1 = new Intent(NotepadFragment.ACTION_CHANGE_FRAGMENT).setPackage(context.getPackageName());
                intent1.putExtra(NotepadFragment.PAGE_ID, NotepadFragment.FRAGMENT_EDITOR);
                context.sendBroadcast(intent1);

                Intent newNoteIntent = new Intent(EditorFragment.ACTION_OPEN_FILE).setPackage(context.getPackageName());
                newNoteIntent.putExtra(EditorFragment.FILE_PATH_EXTRA, EditorFragment.PATH_NONE_EXTRA);
                context.sendBroadcast(newNoteIntent);
            });
        }

        private void showStopwatch(boolean start) {
            mManager.setPageByName(StopwatchFragment.STRING_ID, () -> {
                if(start) {
                    Intent startTimerIntent = new Intent(context, StopwatchService.class);
                    startTimerIntent.setAction(StopwatchService.ACTION_START_TIMER);
                    context.startForegroundService(startTimerIntent);
                }
            });
        }
    }
}