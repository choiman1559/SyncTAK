package com.sync.tak.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Objects;

public class NetProfile {

    public String uniqueID = "";
    public String profileName = "";
    public String fcmTopic = "";
    public String encryptKey = "";
    public String hmacKey = "";

    public boolean isEncrypt = false;
    public boolean useHMAC = false;

    @NonNull
    @Override
    public String toString() {
        try {
            JSONObject properties = new JSONObject();
            properties.put("uniqueID", uniqueID);
            properties.put("profileName", profileName);
            properties.put("fcmTopic", fcmTopic);
            properties.put("encryptKey", encryptKey);
            properties.put("hmacKey", hmacKey);
            properties.put("isEncrypt", isEncrypt);
            properties.put("useHMAC", useHMAC);

            return properties.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "";
    }

    @Nullable
    public static NetProfile getNetProfile(String profile) {
        NetProfile netProfile = new NetProfile();
        try {
            JSONObject object = new JSONObject(profile);
            netProfile.uniqueID = object.getString("uniqueID");
            netProfile.profileName = object.getString("profileName");
            netProfile.fcmTopic = object.getString("fcmTopic");
            netProfile.encryptKey = object.getString("encryptKey");
            netProfile.hmacKey = object.getString("hmacKey");
            netProfile.isEncrypt = object.getBoolean("isEncrypt");
            netProfile.useHMAC = object.getBoolean("useHMAC");

            return netProfile;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String getUniqueID() {
        if(uniqueID == null || uniqueID.isBlank()) {
            uniqueID = Integer.toString(this.hashCode());
        }

        return uniqueID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetProfile that = (NetProfile) o;

        return isEncrypt == that.isEncrypt &&
                useHMAC == that.useHMAC &&
                Objects.equals(uniqueID, that.uniqueID) &&
                Objects.equals(profileName, that.profileName) &&
                Objects.equals(fcmTopic, that.fcmTopic) &&
                Objects.equals(encryptKey, that.encryptKey) &&
                Objects.equals(hmacKey, that.hmacKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueID, profileName, fcmTopic, encryptKey, hmacKey, isEncrypt, useHMAC, Calendar.getInstance().getTimeInMillis());
    }

    @Nullable
    public static NetProfile getNetProfileFromPrefs(Context context) {
        SharedPreferences profilePrefs = context.getSharedPreferences(context.getPackageName() + "_profile_preferences", Context.MODE_PRIVATE);
        String selectedKey = profilePrefs.getString(ProfileActivity.profileSelectedKey, "");
        if(!selectedKey.isBlank() && profilePrefs.getStringSet(ProfileActivity.profileIDListKey, new HashSet<>()).contains(selectedKey)) {
            return NetProfile.getNetProfile(profilePrefs.getString(selectedKey, ""));
        }

        return null;
    }
}