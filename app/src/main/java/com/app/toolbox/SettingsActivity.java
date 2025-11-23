package com.app.toolbox;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.toolbox.view.AnimatedButton;
import com.google.android.material.color.DynamicColors;

import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {
    public static final String PREFERENCES_NAME       = "toolbox.settings.preferences";
//    public static final String ENABLE_BORDERS_PREF    = "toolbox.settings.enableBorder";
//    public static final String ROUND_CORNERS_PREF     = "toolbox.settings.roundCorners";
//    public static final String BORDER_WEIGHT_PREF     = "toolbox.settings.borderWeight";
    public static final String BUTTON_BACKGROUND_PREF = "toolbox.settings.buttonBackground";
    public static final String BORDER_COLOR_PREF      = "toolbox.settings.borderColor";
    private int mButtonBackgroundColor;
    private int mBorderColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DynamicColors.applyToActivityIfAvailable(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initAboutSection();
        addOnClickListeners();
        restorePrefsToViews();
    }

    private void restorePrefsToViews() {
        SharedPreferences preferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);

//        CheckBox enableBordersCheckBox = findViewById(R.id.enable_borders_checkbox);
//        enableBordersCheckBox.setChecked(preferences.getBoolean(ENABLE_BORDERS_PREF, true));
//
//        CheckBox roundCornersCheckBox = findViewById(R.id.round_corners_checkbox);
//        roundCornersCheckBox.setChecked(preferences.getBoolean(ROUND_CORNERS_PREF, true));
//
//        SeekBar borderWeightSeekBar = findViewById(R.id.border_weight_seekbar);
//        borderWeightSeekBar.setProgress(preferences.getInt(BORDER_WEIGHT_PREF, 2));

        mButtonBackgroundColor = preferences.getInt(BUTTON_BACKGROUND_PREF, Color.BLUE);
        mBorderColor = preferences.getInt(BORDER_COLOR_PREF, Color.BLUE);
    }

    private void addOnClickListeners() {
        findViewById(R.id.back_button).setOnClickListener(new ExitListener());
//        findViewById(R.id.select_buttons_background_button).setOnClickListener(v ->
//                showColorPicker(mButtonBackgroundColor, color -> mButtonBackgroundColor = color));
//        findViewById(R.id.select_border_color_button).setOnClickListener(v ->
//                showColorPicker(mBorderColor, color -> mBorderColor = color));
    }

    private void initAboutSection() {
        TextView appVersionView = findViewById(R.id.app_version);
        String versionName;
        try {
            PackageInfo packageInfo = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = " "+packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("get_version_name", "Cannot get version getPageName.", e);
            versionName = "Unknown";
        }
        appVersionView.append(versionName);

        TextView copyrightView = findViewById(R.id.copyright_view);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        copyrightView.append(String.valueOf(year));
        copyrightView.append(getString(R.string.developer_name));
    }

    private class ExitListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            SharedPreferences preferences = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

//            CheckBox enableBordersCheckBox = findViewById(R.id.enable_borders_checkbox);
//            boolean enableBorders = enableBordersCheckBox.isChecked();
//            editor.putBoolean(ENABLE_BORDERS_PREF, enableBorders);
//
//            CheckBox roundCornersCheckBox = findViewById(R.id.round_corners_checkbox);
//            boolean roundCorners = roundCornersCheckBox.isChecked();
//            editor.putBoolean(ROUND_CORNERS_PREF, roundCorners);
//
//            SeekBar borderWeightSeekBar = findViewById(R.id.border_weight_seekbar);
//            int borderWeight = borderWeightSeekBar.getProgress();
//            editor.putInt(BORDER_WEIGHT_PREF, borderWeight);

            editor.putInt(BUTTON_BACKGROUND_PREF, mButtonBackgroundColor);
            editor.putInt(BORDER_COLOR_PREF, mBorderColor);

            editor.apply();
            finish();
            AnimatedButton.sBackground = -1;
        }
    }

}