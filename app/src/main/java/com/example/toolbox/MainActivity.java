package com.example.toolbox;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.toolbox.fragment.*;
import com.example.toolbox.view.navigation.NavigationItemView;
import com.example.toolbox.view.navigation.NavigationView;
import com.game.toolbox.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;

public class MainActivity extends AppCompatActivity {
    private static final String USAGES_FILE_NAME = "usages.dat";
    public NavigationView bnv;
    public static List<ToolFragment> fragments;
    private ViewPager2 vpg2;

    public static ToolFragment getFragment(Class<? extends ToolFragment> clazz) {
        if (clazz==null)
            throw new IllegalArgumentException("Wtf you're doing man");
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
        // save fragment usages to file
        Properties usages=new Properties();
        for(ToolFragment tf: fragments) {
            usages.setProperty(tf.getClass().toString(), tf.getUsages()+"");
        }
        try {
            File file = new File(getApplicationContext().getFilesDir(), USAGES_FILE_NAME);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos=new FileOutputStream(file);
            usages.store(fos, "usages");
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initFragments() {
        ToolAdapter fragmentStateAdapter = new ToolAdapter(this);
        vpg2=findViewById(R.id.fragment_adapter);
        vpg2.setAdapter(fragmentStateAdapter);

        List<ToolFragment> loc_fragments=new ArrayList<>();
        Collections.addAll(loc_fragments,
                new TimerFragment(this),
                new StopwatchFragment(this),
                new CalculatorFragment(this),
                new NotepadFragment(this),
                new RNGFragment(this));

        // load usages
        Properties usages=new Properties();
        try {
            File usage_file=new File(getApplicationContext().getFilesDir(), USAGES_FILE_NAME);
            if(!usage_file.exists()) {
                usage_file.createNewFile();
            }
            FileInputStream fis=new FileInputStream(usage_file);
            usages.load(fis);
            fis.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // set usages to fragments
        usages.forEach((k, v) -> {
            final String className=(String)k;
            final long classUsages=Long.parseLong((String)v);

            for(ToolFragment frag: loc_fragments) {
                if(className.equals(frag.getClass().toString()))
                    frag.setUsages(classUsages);
            }
        });

        // sort fragments by usages
        loc_fragments.sort((o1, o2) ->
                Long.compare(o2.getUsages(), o1.getUsages()));
        fragments=Collections.unmodifiableList(loc_fragments);
    }

    private void initNavBar() {
        bnv = findViewById(R.id.bottom_navigation);
        fragments.forEach(v -> {
            NavigationItemView nv=v.getNavItem();
            nv.setOnClickListener(av -> {
                setFragment(getFragment(v.getClass()));
            });
            bnv.addItem(nv);
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EdgeToEdge.enable(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        initFragments();
        initNavBar();
    }

    private void setFragment(ToolFragment fragment) {
        vpg2.setCurrentItem(fragments.indexOf(fragment));
    }

}