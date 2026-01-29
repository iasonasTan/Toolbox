package com.app.toolbox;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.toolbox.tools.randnumgen.widget.RandNumGenWidget;
import com.app.toolbox.view.AnimatedButton;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public final class SettingsActivity extends AppCompatActivity {
    public static final String PREFERENCES_NAME       = "toolbox.settings.preferences";
    public static final String RNG_WIDGET_LIMIT_EXTRA = "toolbox.settings.rngWidgetLimit";

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

        TextInputEditText limitInput = findViewById(R.id.limit_input);
        limitInput.setText(String.valueOf(preferences.getFloat(RNG_WIDGET_LIMIT_EXTRA, RandNumGenWidget.DEFAULT_LIMIT)));
    }

    private void addOnClickListeners() {
        findViewById(R.id.back_button).setOnClickListener(new ExitListener());
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

            // save preferences
            TextInputEditText limitInput = findViewById(R.id.limit_input);
            Editable limitText = limitInput.getText();
            float widgetLimit = Float.parseFloat(limitText!=null?limitText.toString():""+ RandNumGenWidget.DEFAULT_LIMIT);
            editor.putFloat(RNG_WIDGET_LIMIT_EXTRA, widgetLimit);

            // tell widgets to update
            Intent intent = new Intent(getApplicationContext(), RandNumGenWidget.class)
                .setAction("android.appwidget.action.APPWIDGET_UPDATE");
            sendBroadcast(intent);

            editor.apply();
            finish();
            AnimatedButton.sBackground = -1;
        }
    }

}