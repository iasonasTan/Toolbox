package com.app.toolbox;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.app.toolbox.fragment.CalculatorFragment;
import com.app.toolbox.fragment.NotepadFragment;
import com.app.toolbox.fragment.RNGFragment;
import com.app.toolbox.fragment.stopwatch.StopwatchFragment;
import com.app.toolbox.fragment.timer.TimerFragment;
import com.app.toolbox.utils.ToolFragment;
import com.app.toolbox.view.navigation.NavigationItemView;
import com.app.toolbox.view.navigation.NavigationView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
        // because first fragment shows automatically it also increases usages automatically
        fragments.get(0).decreaseUsages();

        doIntentStuff();
    }

    private void doIntentStuff() {
        Intent intent=getIntent();
        if(intent==null) return;

        String name=intent.getStringExtra("FRAGMENT_NAME");
        if(name!=null) setFragmentByName(name);
    }

    public void setFragment(ToolFragment fragment) {
        vpg2.setCurrentItem(fragments.indexOf(fragment));
    }

}