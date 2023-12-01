package com.sync.tak;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import com.kieronquinn.monetcompat.core.MonetCompat;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MonetCompat.enablePaletteCompat();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences("com.sync.tak_preferences", Context.MODE_PRIVATE);
    }
}
