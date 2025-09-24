package com.app.toolbox;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.app.toolbox.fragment.CalculatorFragment;
import com.app.toolbox.fragment.RNGFragment;
import com.app.toolbox.fragment.notepad.NotepadFragment;
import com.app.toolbox.fragment.stopwatch.StopwatchFragment;
import com.app.toolbox.fragment.timer.TimerFragment;
import com.app.toolbox.utils.ToolFragment;
import com.app.toolbox.view.navigation.NavigationItemView;
import com.app.toolbox.view.navigation.NavigationView;

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
import java.util.Properties;

public final class MainActivity extends AppCompatActivity {
    private static final String USAGES_FILE_NAME = "usages.dat";
    public NavigationView bnv;
    public static List<ToolFragment> fragments;
    private ViewPager2 vpg2;

    public void setFragmentByName(String requiredName) {
        for(ToolFragment toolFragment: fragments) {
            if(requiredName.equals(toolFragment.name())) {
                setFragment(toolFragment);
                return;
            }
        }
        StringBuilder codes=new StringBuilder();
        fragments.forEach(f -> codes.append('\t').append(f.name()).append("\n"));
        throw new NoSuchElementException("could not found item with name "+requiredName+ "\n"+ "Available names are:\n"+ codes);
    }

    public static ToolFragment getFragment(Class<? extends ToolFragment> clazz) {
        if (clazz==null) throw new NullPointerException("Class is null.");
        for (ToolFragment fragment: fragments) {
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
        // save fragment usages to file
        Properties usages=new Properties();
        for(ToolFragment tf: fragments) {
            usages.setProperty(tf.getClass().toString(), tf.getUsages()+"");
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
        List<ToolFragment> loc_fragments=new ArrayList<>();
        Collections.addAll(loc_fragments, new TimerFragment(), new StopwatchFragment(),
                new CalculatorFragment(), new NotepadFragment(), new RNGFragment());
        loadFragmentUsages(loc_fragments);

        // sort fragments by usages
        loc_fragments.sort((o1, o2) -> Long.compare(o2.getUsages(), o1.getUsages()));
        fragments=Collections.unmodifiableList(loc_fragments);

        ToolAdapter fragmentStateAdapter = new ToolAdapter(this);
        vpg2=findViewById(R.id.fragment_adapter);
        vpg2.setAdapter(fragmentStateAdapter);
    }

    private void loadFragmentUsages(List<ToolFragment> loc_fragments) {
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

            for(ToolFragment frag: loc_fragments) {
                if(className.equals(frag.getClass().toString()))
                    frag.setUsages(classUsages);
            }
        });
    }

    private void initNavBar() {
        Log.d("action_spoil", "Initializing navigation bar.");
        bnv = findViewById(R.id.bottom_navigation);
        fragments.forEach(frag -> {
            frag.removeNavItem();
            NavigationItemView nv=frag.getNavItem(this);
            bnv.addItem(nv);
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EdgeToEdge.enable(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU&&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
        initFragments();
        initNavBar();

        // remove one usage from first fragment
        // because first fragment shows automatically it increases usages
        fragments.get(0).decreaseUsages();
        handleIntent(getIntent());

        new Thread(this::checkForUpdates).start();
    }

    public void checkForUpdates() {
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
                    runOnUiThread(() -> new AlertDialog.Builder(this)
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

        String name=intent.getStringExtra("FRAGMENT_NAME");
        if(name!=null) setFragmentByName(name);
    }

    public void setFragment(ToolFragment fragment) {
        vpg2.setCurrentItem(fragments.indexOf(fragment));
    }

}