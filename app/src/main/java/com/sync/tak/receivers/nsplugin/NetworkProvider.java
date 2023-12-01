package com.sync.tak.receivers.nsplugin;

import static com.sync.tak.comms.NetworkWorkers.getUniqueID;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;
import com.sync.tak.Application;
import com.sync.tak.comms.NetworkWorkers;
import com.sync.tak.ui.profile.NetProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NetworkProvider {

    public static onProviderMessageListener onNetworkProviderListener;
    public static ArrayList<onFCMIgnitionCompleteListener> onFCMIgnitionCompleteListenerList;

    public interface onProviderMessageListener {
        void onMessageReceived(RemoteMessage message);
    }

    public interface onFCMIgnitionCompleteListener {
        void onStartUp();
    }

    public static void setOnNetworkProviderListener(onProviderMessageListener listener) {
        onNetworkProviderListener = listener;
    }

    public static void addOnFCMIgnitionCompleteListener(onFCMIgnitionCompleteListener listener) {
        if(onFCMIgnitionCompleteListenerList == null) {
            onFCMIgnitionCompleteListenerList = new ArrayList<>();
        }

        onFCMIgnitionCompleteListenerList.add(listener);
    }

    public static void fcmIgnitionComplete() {
        if(onFCMIgnitionCompleteListenerList != null) {
            for(onFCMIgnitionCompleteListener listener : onFCMIgnitionCompleteListenerList) {
                listener.onStartUp();
            }
            onFCMIgnitionCompleteListenerList.clear();
        }
    }

    public static void processReception (Context context, NetPacket packet) {
        RemoteMessage remoteMessage = new RemoteMessage.Builder("Implement").setData(packet.build()).build();
        processReception(context, remoteMessage);
    }

    public static void processReception (Context context, RemoteMessage remoteMessage) {
        if(onNetworkProviderListener != null) {
            onNetworkProviderListener.onMessageReceived(remoteMessage);
        } else {
            NetProfile netProfile = NetProfile.getNetProfileFromPrefs(context);
            if(netProfile == null) {
                Log.d("NetworkWorkers", "Network provider is not selected, Ignoring sending message request");
                return;
            }

            String DEVICE_NAME = Build.MANUFACTURER + " " + Build.MODEL;
            String DEVICE_ID = getUniqueID(context);
            String TOPIC = "/topics/" + Application.getPreferences(context).getString("UID", "");

            JSONObject notificationHead = new JSONObject();
            JSONObject notificationBody = new JSONObject();
            try {
                notificationBody.put("type", "startup");
                notificationBody.put("device_name", DEVICE_NAME);
                notificationBody.put("device_id", DEVICE_ID);

                notificationHead.put("to", TOPIC);
                notificationHead.put("priority", "high");
                notificationHead.put("data", notificationBody);
            } catch (JSONException e) {
                Log.e("Noti", "onCreate: " + e.getMessage());
            }

            addOnFCMIgnitionCompleteListener(() -> onNetworkProviderListener.onMessageReceived(remoteMessage));
            NetworkWorkers.sendNotification(notificationHead, netProfile, context.getPackageName(), context, true);
        }
    }
}
