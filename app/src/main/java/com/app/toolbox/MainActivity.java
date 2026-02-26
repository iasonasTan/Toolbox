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
import com.app.toolbox.tools.notepad.NotepadEditor;
import com.app.toolbox.tools.notepad.NotepadRoot;
import com.app.toolbox.tools.notepad.storage.Storage;
import com.app.toolbox.tools.randnumgen.RandNumGenFragment;
import com.app.toolbox.tools.stopwatch.StopwatchRoot;
import com.app.toolbox.tools.stopwatch.StopwatchService;
import com.app.toolbox.tools.timer.TimerEditor;
import com.app.toolbox.tools.timer.TimerRoot;
import com.app.toolbox.utils.IntentContentsMissingException;
import com.app.toolbox.utils.PageFragment;
import com.app.toolbox.utils.ParentPageFragment;
import com.app.toolbox.utils.Utils;
import com.app.toolbox.view.navigation.NavigationItemView;
import com.app.toolbox.view.navigation.NavigationView;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Consumer;

/*
 * Codes:
 * Permissions: 1000-1999
 * PendingIntents: 2000-2999, action.hashcode
 * Launch activity: 3000-3999
 */

public final class MainActivity extends AppCompatActivity {
    private static final String USAGES_FILE_NAME = "usages.dat";
    public static final String SWITCH_PAGE       = "toolbox.mainActivity.switchPage";
    public static final String PAGE_NAME_EXTRA   = "toolbox.mainActivity.pageName";
    public static final String CONFIG_VIEW_PAGER = "toolbox.mainActivity.configViewPager";
    public static final String ENABLE_SCROLL_EXTRA = "toolbox.mainActivity.scrollingEnabled";
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
                case CONFIG_VIEW_PAGER -> mViewPager2.setUserInputEnabled(intent.getBooleanExtra(ENABLE_SCROLL_EXTRA, true));
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

        IntentFilter intentFilter = Utils.intentFilter(SWITCH_PAGE, CONFIG_VIEW_PAGER);
        ContextCompat.registerReceiver(getApplicationContext(), mSwitchPageReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED);

        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1000);

        mManager.initFragments();
        initViews();

        Storage.initIfNotInitialized(this, "notes");
        if(savedInstanceState==null) {
            new IntentProcessor(this, mManager).processIntent(getIntent());
            new ApplicationGreeter(this).greet();
            //new UpdateChecker().checkVersionAsynchronously();
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
        sReceiverOwners.forEach(owner -> Utils.execute(() -> owner.unregisterReceivers(this)));
        sReceiverOwners.clear();
        Storage.destroy();
    }

    public final class PageManager {
        private List<PageFragment> fragments;

        public void setPageByName(String requiredName) {
            for(PageFragment pageFragment : fragments) {
                if(requiredName.equals(pageFragment.getPageName())) {
                    setPage(pageFragment);
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
            Collections.addAll(loc_fragments, new TimerRoot(), new StopwatchRoot(),
                    new CalculatorFragment(), new NotepadRoot(), new RandNumGenFragment());
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

        public void setPage(PageFragment fragment) {
            mViewPager2.setCurrentItem(fragments.indexOf(fragment));
        }

        private final class ToolAdapter extends FragmentStateAdapter {
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
            } catch (Exception ignored) {
                // causes: no internet, DNS problem or server is closed
                // ignore, no version check
            }
        }

        private boolean needsUpdate(String appV, String latV) {
            double latestVersion = Double.parseDouble(latV);
            double appVersion = Double.parseDouble(appV);
            return appVersion < latestVersion;
        }

        private String getApplicationVersion() throws PackageManager.NameNotFoundException {
            PackageManager pm = getPackageManager();
            PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
            if(info==null||info.versionName==null)
                throw new NullPointerException("Cannot get version code. Null returned by android system.");
            return info.versionName;
        }

        private String requestVersionFromServer() {
            // TODO get latest version code from somewhere online
            throw new UnsupportedOperationException();
        }

        private void showUpdateDialog() {
            runOnUiThread(() -> new MaterialAlertDialogBuilder(MainActivity.this)
                    .setTitle("Update available!")
                    .setMessage("Do you want to update?")
                    .setCancelable(false)
                    .setNegativeButton("Not now.", (a, b) -> a.dismiss())
                    .setPositiveButton("Yes!", (a, b) -> {
                        a.dismiss();
                        String url = "https://github.com/iasonasTan/Toolbox/releases";
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

            if(name!=null&&(action.equals(SWITCH_PAGE)||action.equals(ACTION_SHOW_PAGE)))
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
            mManager.setPageByName(TimerRoot.STRING_ID, () -> {
                Intent changeFragmentIntent = new Intent(ParentPageFragment.actionChangePage(TimerRoot.STRING_ID)).setPackage(context.getPackageName());
                changeFragmentIntent.putExtra(ParentPageFragment.PAGE_CLASSNAME_EXTRA, TimerEditor.class.getName());
                context.sendBroadcast(changeFragmentIntent);
            });
        }

        private void appendToNotepad(Intent intent) {
            String text = intent.getStringExtra(Intent.EXTRA_TEXT);
            mManager.setPageByName(NotepadRoot.STRING_ID, () -> {
                Intent changeFragmentIntent = new Intent(ParentPageFragment.actionChangePage(NotepadRoot.STRING_ID)).setPackage(context.getPackageName());
                changeFragmentIntent.putExtra(ParentPageFragment.PAGE_CLASSNAME_EXTRA, NotepadEditor.class.getName());
                context.sendBroadcast(changeFragmentIntent);

                Intent newNoteIntent = new Intent(NotepadEditor.ACTION_LOAD).setPackage(context.getPackageName());
                newNoteIntent.putExtra(NotepadEditor.FILE_NAME_EXTRA, NotepadEditor.NO_FILE_EXTRA);
                newNoteIntent.putExtra(NotepadEditor.TEXT_EXTRA, text);
                context.sendBroadcast(newNoteIntent);
            });
        }

        private void createNote() {
            mManager.setPageByName(NotepadRoot.STRING_ID, () -> {
                Intent intent1 = new Intent(ParentPageFragment.actionChangePage(NotepadRoot.STRING_ID)).setPackage(context.getPackageName());
                intent1.putExtra(ParentPageFragment.PAGE_CLASSNAME_EXTRA, NotepadEditor.class.getName());
                context.sendBroadcast(intent1);

                Intent newNoteIntent = new Intent(NotepadEditor.ACTION_LOAD).setPackage(context.getPackageName());
                newNoteIntent.putExtra(NotepadEditor.FILE_NAME_EXTRA, NotepadEditor.NO_FILE_EXTRA);
                context.sendBroadcast(newNoteIntent);
            });
        }

        private void showStopwatch(boolean start) {
            mManager.setPageByName(StopwatchRoot.STRING_ID, () -> {
                if(start) {
                    Intent startTimerIntent = new Intent(context, StopwatchService.class);
                    startTimerIntent.setAction(StopwatchService.ACTION_START_TIMER);
                    context.startForegroundService(startTimerIntent);
                }
            });
        }
    }
}