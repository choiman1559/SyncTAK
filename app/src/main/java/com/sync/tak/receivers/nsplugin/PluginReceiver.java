package com.sync.tak.receivers.nsplugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.Objects;

public class PluginReceiver extends BroadcastReceiver {
    public static onReceivePluginInformation receivePluginInformation;

    public interface onReceivePluginInformation {
        void onReceive(Bundle data);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(PluginConst.SENDER_ACTION_NAME)) {
            Bundle rawData = intent.getExtras();
            String dataType = rawData.getString(PluginConst.DATA_KEY_TYPE);
            String packageName = rawData.getString(PluginConst.PLUGIN_PACKAGE_NAME);

            if (dataType.equals(PluginConst.ACTION_RESPONSE_INFO) && receivePluginInformation != null) {
                receivePluginInformation.onReceive(rawData);
            } else {
                if (dataType.equals(PluginConst.NET_PROVIDER_RECEIVED)) {
                    NetworkProvider.processReception(context, (NetPacket) Objects.requireNonNull(rawData.getSerializable(PluginConst.NET_PROVIDER_DATA)));
                } else {
                    PluginActions.pushException(context, packageName, new IllegalAccessException("Plugin Action type is not supported: " + dataType));
                }
            }
        }
    }
}
