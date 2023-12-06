package com.sync.tak.comms;

import static com.sync.tak.comms.NetworkWorkers.getUniqueID;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sync.tak.Application;
import com.sync.tak.BuildConfig;
import com.sync.tak.receivers.CoTTransmittingReceiver;
import com.sync.tak.receivers.nsplugin.NetworkProvider;
import com.sync.tak.ui.profile.NetProfile;
import com.sync.tak.utils.PowerUtils;
import com.sync.tak.utils.network.AESCrypto;
import com.sync.tak.utils.network.CompressStringUtil;
import com.sync.tak.utils.network.HMACCrypto;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class FirebaseMessageService extends FirebaseMessagingService {

    public static final ArrayList<Integer> selfReceiveDetectorList = new ArrayList<>();
    public static final ArrayList<SplitDataObject> splitDataList = new ArrayList<>();
    private final NetworkProvider.onProviderMessageListener onProviderMessageListener = this::onMessageReceived;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        PowerUtils.getInstance(this).acquire();
        if (BuildConfig.DEBUG) Log.d(remoteMessage.getMessageId(), remoteMessage.toString());

        Map<String, String> map = remoteMessage.getData();
        preProcessReception(map, this);
        NetworkProvider.setOnNetworkProviderListener(this.onProviderMessageListener);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        NetworkProvider.setOnNetworkProviderListener(this.onProviderMessageListener);
    }

    public void preProcessReception(Map <String, String> map, Context context) {
        NetProfile netProfile = NetProfile.getNetProfileFromPrefs(context);
        SharedPreferences prefs = Application.getPreferences(context);

        if(!prefs.getBoolean("serviceToggle", false)) {
            Log.d("NetworkWorkers", "Service toggle is disabled, Ignoring received message process.");
            return;
        }

        if(netProfile == null) {
            Log.d("NetworkWorkers", "Network provider is not selected, Ignoring received message request");
            return;
        }

        String rawPassword = netProfile.encryptKey;
        boolean useEncryption = netProfile.isEncrypt;
        boolean useHmacAuth = netProfile.useHMAC;

        if ("true".equals(map.get("encrypted"))) {
            int dataHash = Objects.requireNonNull(map.get("encryptedData")).hashCode();
            if(selfReceiveDetectorList.contains(dataHash)) {
                selfReceiveDetectorList.remove((Integer) dataHash);
                return;
            }

            if ((useEncryption && !rawPassword.equals(""))) {
                try {
                    if(useHmacAuth) {
                        preProcessReceptionWithHmac(map, netProfile,rawPassword, context);
                    } else {
                        JSONObject object = new JSONObject(AESCrypto.decrypt(CompressStringUtil.decompressString(map.get("encryptedData")), rawPassword));
                        Map<String, String> newMap = objectMapping(object);
                        processReception(newMap, context);
                    }
                } catch (GeneralSecurityException e) {
                    Handler mHandler = new Handler(Looper.getMainLooper());
                    mHandler.postDelayed(() -> Toast.makeText(context, "Error occurred while decrypting data!\nPlease check password and try again!", Toast.LENGTH_SHORT).show(), 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            if(useHmacAuth) {
                preProcessReceptionWithHmac(map, netProfile, null, context);
            } else {
                processReception(map, context);
            }
        }
    }

    public void preProcessReceptionWithHmac(Map <String, String> map, NetProfile netProfile, @Nullable String token, Context context) {
        try {
            String HmacToken = netProfile.hmacKey;
            JSONObject object = new JSONObject(HMACCrypto.decrypt(CompressStringUtil.decompressString(map.get("encryptedData")), HmacToken, token));
            Map<String, String> newMap = objectMapping(object);
            processReception(newMap, context);
        } catch (GeneralSecurityException | JSONException e) {
            e.printStackTrace();
        }
    }

    protected void processSplitData(Map<String, String> map, Context context) {
        synchronized (splitDataList) {
            if(BuildConfig.DEBUG) Log.d("split_data", "current size : " + splitDataList.size());

            for (int i = 0; i < splitDataList.size(); i++) {
                SplitDataObject object = splitDataList.get(i);
                if (object.unique_id.equals(map.get("split_unique"))) {
                    object = object.addData(map);
                    splitDataList.set(i, object);

                    if (object.length == object.getSize()) {
                        try {
                            Map<String, String> newMap = objectMapping(new JSONObject(object.getFullData()));
                            preProcessReception(newMap, context);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } finally {
                            splitDataList.remove(object);
                        }
                    }
                    return;
                }
            }
            splitDataList.add(new SplitDataObject(map));
        }
    }

    private static Map<String, String> objectMapping(JSONObject object) throws JSONException {
        Map<String, String> map = new HashMap<>();
        for (Iterator<String> it = object.keys(); it.hasNext(); ) {
            String key = it.next();
            map.put(key, object.getString(key));
        }

        return map;
    }

    public void processReception(Map<String, String> map, Context context) {
        switch (Objects.requireNonNull(map.get("type"))) {
            case "cot_message":
                String cotMessage = map.get("cot_data");
                if(!isDeviceItself(context, map)) {
                    CoTTransmittingReceiver.sendBroadcastReceive(context, cotMessage);
                }
                break;

            case "startup":
                if(isDeviceItself(context, map)) {
                    NetworkProvider.fcmIgnitionComplete();
                }
                break;

            case "split_data":
                if(!isDeviceItself(context, map)) {
                    processSplitData(map, context);
                }
                break;
        }
    }

    protected boolean isDeviceItself(Context context, Map<String, String> map) {
        String Device_name = map.get("device_name");
        String Device_id = map.get("device_id");

        if (Device_id == null || Device_name == null) {
            Device_id = map.get("send_device_id");
            Device_name = map.get("send_device_name");
        }

        String DEVICE_NAME = Build.MANUFACTURER + " " + Build.MODEL;
        String DEVICE_ID = getUniqueID(context);

        return DEVICE_NAME.equals(Device_name) && DEVICE_ID.equals(Device_id);
    }

    @SuppressWarnings("unused")
    protected boolean isTargetDevice(Context context, Map<String, String> map) {
        String Device_name = map.get("send_device_name");
        String Device_id = map.get("send_device_id");

        String DEVICE_NAME = Build.MANUFACTURER + " " + Build.MODEL;
        String DEVICE_ID = getUniqueID(context);

        return DEVICE_NAME.equals(Device_name) && DEVICE_ID.equals(Device_id);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }
}
