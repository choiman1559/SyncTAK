package com.sync.tak.comms;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import com.sync.tak.Application;
import com.sync.tak.receivers.nsplugin.PluginActions;
import com.sync.tak.receivers.nsplugin.PluginConst;
import com.sync.tak.ui.profile.NetProfile;
import com.sync.tak.utils.PowerUtils;
import com.sync.tak.utils.network.AESCrypto;
import com.sync.tak.utils.network.CompressStringUtil;
import com.sync.tak.utils.network.HMACCrypto;
import com.sync.tak.utils.network.JsonRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkWorkers {

    public static void pushMessage(Context context, String message) {
        SharedPreferences prefs = Application.getPreferences(context);
        NetProfile netProfile = NetProfile.getNetProfileFromPrefs(context);

        if(!prefs.getBoolean("serviceToggle", false)) {
            Log.d("NetworkWorkers", "Service toggle is disabled, Ignoring sending message request");
            return;
        }

        if(netProfile == null) {
            Log.d("NetworkWorkers", "Network provider is not selected, Ignoring sending message request");
            return;
        }

        String DEVICE_NAME = Build.MANUFACTURER + " " + Build.MODEL;
        String DEVICE_ID = getUniqueID(context);
        String TOPIC = "/topics/" + netProfile.fcmTopic;

        JSONObject notificationHead = new JSONObject();
        JSONObject notificationBody = new JSONObject();
        try {
            notificationBody.put("type","cot_message");
            notificationBody.put("device_name", DEVICE_NAME);
            notificationBody.put("device_id", DEVICE_ID);
            notificationBody.put("cot_data", message);

            notificationHead.put("to", TOPIC);
            notificationHead.put("priority", "high");
            notificationHead.put("data", notificationBody);
        } catch (JSONException e) {
            Log.e("Noti", "onCreate: " + e.getMessage());
        }

        sendNotification(notificationHead, netProfile, context.getPackageName(), context);
    }

    public static void sendNotification(JSONObject notification, NetProfile netProfile, String PackageName, Context context, boolean useFCMOnly) {
        SharedPreferences prefs = Application.getPreferences(context);
        PowerUtils manager = PowerUtils.getInstance(context);
        manager.acquire();

        try {
            boolean useSplit = prefs.getBoolean("UseSplitData", false) && notification.getString("data").length() > 3072;
            boolean useEncryption = netProfile.isEncrypt;
            boolean splitAfterEncryption = prefs.getBoolean("SplitAfterEncryption", false);
            int splitInterval = prefs.getInt("SplitInterval", 500);

            if (useSplit && !useFCMOnly) {
                if(useEncryption && splitAfterEncryption) encryptData(netProfile, context, notification);
                for (JSONObject object : splitData(context, notification)) {
                    if(useEncryption && !splitAfterEncryption) encryptData(netProfile, context, object);
                    finalProcessData(object, netProfile, PackageName, context, false);
                    if (splitInterval > 0) {
                        Thread.sleep(splitInterval);
                    }
                }
                return;
            } else if (useEncryption) {
                encryptData(netProfile, context, notification);
            }

            finalProcessData(notification, netProfile, PackageName, context, useFCMOnly);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendNotification(JSONObject notification, NetProfile netProfile, String PackageName, Context context) {
        sendNotification(notification, netProfile, PackageName, context, false);
    }

    protected static JSONObject[] splitData(Context context, JSONObject notification) throws JSONException {
        String rawData = notification.getString("data");

        int size = 1024;
        List<String> arr = new ArrayList<>((rawData.length() + size - 1) / size);
        for (int start = 0; start < rawData.length(); start += size) {
            arr.add(rawData.substring(start, Math.min(rawData.length(), start + size)));
        }

        JSONObject[] data = new JSONObject[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            String str = arr.get(i);
            JSONObject obj = new JSONObject();
            obj.put("type", "split_data");
            obj.put("split_index", i + "/" + arr.size());
            obj.put("split_unique", Integer.toString(rawData.hashCode()));
            obj.put("split_data", str);
            obj.put("device_name", Build.MANUFACTURER + " " + Build.MODEL);
            obj.put("device_id", getUniqueID(context));
            Log.d("unique_id", "id: " + rawData.hashCode());
            data[i] = new JSONObject(notification.put("data", obj).toString());
        }
        return data;
    }

    protected static void encryptData(NetProfile netProfile, Context context, JSONObject notification) throws Exception {
        SharedPreferences prefs = Application.getPreferences(context);
        JSONObject data = notification.getJSONObject("data");

        String rawPassword = netProfile.encryptKey;
        String HmacToken = netProfile.hmacKey;
        boolean useEncryption = netProfile.isEncrypt;
        boolean useHmacAuth = netProfile.useHMAC;

        if ((useEncryption && !rawPassword.equals(""))) {
            String uid = prefs.getString("UID", "");

            if (!uid.isBlank()) {
                String encryptedData = useHmacAuth ? HMACCrypto.encrypt(data.toString(), HmacToken, rawPassword) : AESCrypto.encrypt(data.toString(), rawPassword);
                JSONObject newData = new JSONObject();
                newData.put("encrypted", "true");
                newData.put("encryptedData", CompressStringUtil.compressString(encryptedData));
                notification.put("data", newData);
            }
        } else {
            if (useHmacAuth) {
                String encryptedData = HMACCrypto.encrypt(data.toString(), HmacToken, null);
                JSONObject newData = new JSONObject();
                newData.put("encrypted", "false");
                newData.put("encryptedData", CompressStringUtil.compressString(encryptedData));
                notification.put("data", newData);
            } else {
                data.put("encrypted", "false");
                data.put("HmacID", "none");
                notification.put("data", data);
            }
        }
    }

    protected static void finalProcessData(JSONObject notification, NetProfile netProfile, String PackageName, Context context, boolean useFCMOnly) throws JSONException {
        SharedPreferences prefs = Application.getPreferences(context);
        JSONObject data = notification.getJSONObject("data");
        data.put("topic", netProfile.fcmTopic);
        notification.put("data", data);

        if(data.has("encryptedData")) {
            int uniqueId = data.getString("encryptedData").hashCode();
            FirebaseMessageService.selfReceiveDetectorList.add(uniqueId);
        }

        String networkProvider = useFCMOnly ? "Firebase Cloud Message" : prefs.getString("server", "Firebase Cloud Message");
        if (networkProvider.equals("Firebase Cloud Message")) {
            sendFCMNotification(notification, PackageName, context);
        } else {
            boolean isAppInstalled;
            PackageManager packageManager = context.getPackageManager();

            try {
                packageManager.getApplicationInfo(networkProvider, PackageManager.GET_META_DATA);
                isAppInstalled = true;
            } catch (PackageManager.NameNotFoundException e) {
                isAppInstalled = false;
            }

            if (isAppInstalled) {
                Bundle extras = new Bundle();
                extras.putString(PluginConst.DATA_KEY_TYPE, PluginConst.NET_PROVIDER_POST);
                extras.putSerializable(PluginConst.NET_PROVIDER_DATA, notification.toString());
                PluginActions.sendBroadcast(context, networkProvider, extras);
            } else {
                sendFCMNotification(notification, PackageName, context);
            }
        }

        System.gc();
    }

    private static void sendFCMNotification(JSONObject notification, String PackageName, Context context) {
        SharedPreferences prefs = Application.getPreferences(context);
        PowerUtils manager = PowerUtils.getInstance(context);

        final String FCM_API = "https://fcm.googleapis.com/fcm/send";
        final String serverKey = "key=" + prefs.getString("ApiKey_FCM", "");
        final String contentType = "application/json";
        final String TAG = "NOTIFICATION TAG";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, FCM_API, notification,
                response -> {
                    Log.i(TAG, "onResponse: " + response.toString() + " ,package: " + PackageName);
                    manager.release();
                },
                error -> {
                    Toast.makeText(context, "Failed to send Notification! Please check internet and try again!", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "onErrorResponse: Didn't work" + " ,package: " + PackageName);
                    manager.release();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };

        JsonRequest.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    @SuppressLint("HardwareIds")
    public static String getUniqueID(Context context) {
        SharedPreferences prefs = Application.getPreferences(context);
        String str = "";

        if (prefs != null) {
            switch (prefs.getString("uniqueIdMethod", "Globally-Unique ID")) {
                case "Globally-Unique ID" -> str = prefs.getString("GUIDPrefix", "");
                case "Android ID" -> str = prefs.getString("AndroidIDPrefix", "");
                case "Firebase IID" -> str = prefs.getString("FirebaseIIDPrefix", "");
                case "Device MAC ID" -> str = prefs.getString("MacIDPrefix", "");
            }
            return str;
        }
        return "";
    }
}
