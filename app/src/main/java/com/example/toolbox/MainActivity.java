package com.example.toolbox;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.toolbox.fragment.CalculatorFragment;
import com.example.toolbox.fragment.NotepadFragment;
import com.example.toolbox.fragment.RNGFragment;
import com.example.toolbox.fragment.StopwatchFragment;
import com.example.toolbox.fragment.TimerFragment;
import com.example.toolbox.view.navigation.NavigationItemView;
import com.example.toolbox.view.navigation.NavigationView;
import com.game.toolbox.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;

public class MainActivity extends AppCompatActivity {
    private static final String USAGES_FILE_NAME = "usages.dat";
    public NavigationView bnv;
    public static List<Utils.ToolFragment> fragments;
    private ViewPager2 vpg2;

    public void setFragmentByName(String requiredName) {
        for(Utils.ToolFragment toolFragment: fragments) {
            if(toolFragment.name().equals(requiredName)) {
                setFragment(toolFragment);
                return;
            }
        }
        StringBuilder codes=new StringBuilder();
        fragments.forEach(f -> {
            codes.append(f.name())
                    .append("\n");
        });
        throw new NoSuchElementException("could not found item with name "+requiredName+"\n"+codes);
    }

    public static Utils.ToolFragment getFragment(Class<? extends Utils.ToolFragment> clazz) {
        if (clazz==null)
            throw new NullPointerException("Wtf r u doin man!?");

        for (Utils.ToolFragment fragment: fragments) {
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
        for(Utils.ToolFragment tf: fragments) {
            usages.setProperty(tf.getClass().toString(), tf.getUsages()+"");
        }
        try {
            File file = new File(getApplicationContext().getFilesDir(), USAGES_FILE_NAME);
            if (!file.exists()) file.createNewFile();
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

        List<Utils.ToolFragment> loc_fragments=new ArrayList<>();
        Collections.addAll(loc_fragments,
                new TimerFragment(),
                new StopwatchFragment(),
                new CalculatorFragment(),
                new NotepadFragment(),
                new RNGFragment());

        loadFragmentUsages(loc_fragments);

        // sort fragments by usages
        loc_fragments.sort((o1, o2) ->
                Long.compare(o2.getUsages(), o1.getUsages()));
        fragments=Collections.unmodifiableList(loc_fragments);
    }

    private void loadFragmentUsages(List<Utils.ToolFragment> loc_fragments) {
        Properties usages=new Properties();
        try {
            File usage_file=new File(getFilesDir(), USAGES_FILE_NAME);
            if(!usage_file.exists()) {
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

            for(Utils.ToolFragment frag: loc_fragments) {
                if(className.equals(frag.getClass().toString()))
                    frag.setUsages(classUsages);
            }
        });
    }

    private void initNavBar() {
        bnv = findViewById(R.id.bottom_navigation);
        fragments.forEach(v -> {
            NavigationItemView nv=v.getNavItem(this);
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

        // remove one usage from first fragment
        // because first fragment shows automatically it also increases usages automatically
        fragments.get(0).setUsages(fragments.get(0).getUsages()-1);

        doIntentStuff(getIntent());
    }

    private void doIntentStuff(Intent intent) {
        // show the specified activity if specified
        if(intent==null) {
            return;
        }

        String name=intent.getStringExtra("FRAGMENT_NAME");
        if(name!=null)
            setFragmentByName(name);
    }

    private void setFragment(Utils.ToolFragment fragment) {
        vpg2.setCurrentItem(fragments.indexOf(fragment));
    }

}