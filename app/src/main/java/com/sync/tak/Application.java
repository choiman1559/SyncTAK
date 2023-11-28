package com.sync.tak;

import android.content.Context;
import android.content.SharedPreferences;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences("com.sync.tak_preferences", Context.MODE_PRIVATE);
    }
}
