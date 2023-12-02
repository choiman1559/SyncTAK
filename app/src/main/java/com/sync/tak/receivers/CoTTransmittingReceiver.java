package com.sync.tak.receivers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sync.tak.Application;
import com.sync.tak.BuildConfig;
import com.sync.tak.comms.NetworkWorkers;

import java.util.Objects;

public class CoTTransmittingReceiver extends BroadcastReceiver {

    public static final String IS_METADATA_KEY = "is_metadata";
    public static final String MESSAGE_KEY = "cot_message";
    public static final String ABBREVIATED_KEY = "use_abbreviated";

    public static final String RECEIVER_CLASS = "com.sync.tak.receivers.CoTTransmittingReceiver";
    public static final String ACTION_SEND = "com.sync.tak.receivers.REQUEST_DATA_SEND";
    public static final String ACTION_RECEIVE = "com.sync.tak.receivers.REQUEST_DATA_RECEIVE";

    public static onMessageReceiveListener mOnMessageReceiveListener;
    public static onMetaDataReceiveListener onMetaDataReceiveListener;

    public interface onMessageReceiveListener {
        void onReceive(String message);
    }

    public interface onMetaDataReceiveListener {
        void onReceive(boolean useAbbreviated);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(Objects.equals(intent.getAction(), ACTION_SEND)) {
            if(intent.getBooleanExtra(IS_METADATA_KEY, false)) {
                sendBroadcastMetaDataResponse(context);
            } else {
                NetworkWorkers.pushMessage(context, intent.getStringExtra(MESSAGE_KEY));
            }
        } else if(Objects.equals(intent.getAction(), ACTION_RECEIVE)) {
            if(intent.getBooleanExtra(IS_METADATA_KEY, false)) {
                if(onMetaDataReceiveListener != null) {
                   onMetaDataReceiveListener.onReceive(intent.getBooleanExtra(ABBREVIATED_KEY, false));
                }
            } else if(mOnMessageReceiveListener != null) {
                mOnMessageReceiveListener.onReceive(intent.getStringExtra(MESSAGE_KEY));
            } else Log.d("onMessageReceiveListener", "returning: listener is null");
        }
    }

    public static void sendBroadcastSend(Context context, String message) {
        final Intent intent = new Intent();
        intent.setAction(ACTION_SEND);
        intent.putExtra(IS_METADATA_KEY, false);
        intent.putExtra(MESSAGE_KEY, message);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setComponent(new ComponentName("com.sync.tak", RECEIVER_CLASS));
        context.sendBroadcast(intent);

        if (BuildConfig.DEBUG) {
            Log.d("sent to other device", message);
        }
    }

    public static void sendBroadcastMetaDataRequest(Context context) {
        final Intent intent = new Intent();
        intent.setAction(ACTION_SEND);
        intent.putExtra(IS_METADATA_KEY, true);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setComponent(new ComponentName("com.sync.tak", RECEIVER_CLASS));
        context.sendBroadcast(intent);
    }

    public static void sendBroadcastMetaDataResponse(Context context) {
        final Intent intent = new Intent();
        intent.setAction(ACTION_RECEIVE);
        intent.putExtra(IS_METADATA_KEY, true);
        intent.putExtra(ABBREVIATED_KEY, Application.getPreferences(context).getBoolean("useAbbreviated", false));
        context.sendBroadcast(intent);
    }

    public static void sendBroadcastReceive(Context context, String message) {
        final Intent intent = new Intent();
        intent.setAction(ACTION_RECEIVE);
        intent.putExtra(IS_METADATA_KEY, false);
        intent.putExtra(MESSAGE_KEY, message);
        context.sendBroadcast(intent);

        if (BuildConfig.DEBUG) {
            Log.d("sent received to atak", message);
        }
    }
}
