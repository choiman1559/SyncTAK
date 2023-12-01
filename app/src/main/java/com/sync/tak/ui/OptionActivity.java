package com.sync.tak.ui;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.DynamicColors;

import com.sync.tak.R;
import com.sync.tak.ui.options.AboutPreference;
import com.sync.tak.ui.options.GeneralPreference;

public class OptionActivity extends AppCompatActivity {

    private static String title = "Default Message";
    private static String lastType = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fragment fragment;
        fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);

        if (savedInstanceState == null || fragment == null) {
            String type = getIntent().getStringExtra("Type");
            if (type != null) lastType = type;

            switch (lastType) {
                case "General" -> {
                    fragment = new GeneralPreference();
                    title = "General Options";
                }
                case "About" -> {
                    fragment = new AboutPreference();
                    title = "About SyncTAK";
                }
                default -> {
                    fragment = null;
                    title = getString(R.string.app_name);
                }
            }
        }

        setContentView(R.layout.activity_options);
        DynamicColors.applyToActivityIfAvailable(this);

        Bundle bundle = new Bundle(0);
        if (fragment != null) {
            fragment.setArguments(bundle);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .commit();
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        toolbar.setNavigationOnClickListener((v) -> this.finish());
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_options);
    }
}
