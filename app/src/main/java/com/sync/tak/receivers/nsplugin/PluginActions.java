package com.sync.tak.receivers.nsplugin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.sync.tak.BuildConfig;

public class PluginActions {

    public static void pushException(Context context, String packageName, Exception exception) {
        Bundle extras = new Bundle();
        extras.putString(PluginConst.DATA_KEY_TYPE, PluginConst.ACTION_PUSH_EXCEPTION);
        extras.putSerializable(PluginConst.DATA_KEY_EXCEPTION, exception);
        sendBroadcast(context, packageName, extras);
    }

    public static void sendBroadcast(Context context, String packageName, Bundle extras) {
        final Intent intent = new Intent();
        intent.setAction(PluginConst.RECEIVER_ACTION_NAME);
        intent.putExtras(extras);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setComponent(new ComponentName(packageName, PluginConst.RECEIVER_CLASS_NAME));
        context.sendBroadcast(intent);
        if (BuildConfig.DEBUG)
            Log.d("sent", packageName + " " + extras.getString(PluginConst.DATA_KEY_TYPE));
    }
}
