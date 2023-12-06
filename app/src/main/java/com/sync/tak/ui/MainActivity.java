package com.sync.tak.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;

import com.kieronquinn.monetcompat.app.MonetCompatActivity;
import com.kieronquinn.monetcompat.core.MonetCompat;
import com.kieronquinn.monetcompat.view.MonetSwitch;

import com.sync.tak.Application;
import com.sync.tak.R;
import com.sync.tak.ui.profile.NetProfile;
import com.sync.tak.ui.profile.ProfileActivity;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MainActivity extends MonetCompatActivity {

    MonetCompat mMonet;
    SharedPreferences prefs;

    MonetSwitch serviceToggle;
    MaterialCardView GeneralPreferences;
    MaterialCardView ProfilePreferences;
    MaterialCardView InfoPreferences;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = Application.getPreferences(this);

        serviceToggle = findViewById(R.id.serviceToggle);
        GeneralPreferences = findViewById(R.id.GeneralPreferences);
        ProfilePreferences = findViewById(R.id.ProfilePreferences);
        InfoPreferences = findViewById(R.id.InfoPreferences);

        serviceToggle.setChecked(prefs.getBoolean("serviceToggle", false));
        serviceToggle.setOnCheckedChangeListener((card, isChecked) -> prefs.edit().putBoolean("serviceToggle", isChecked).apply());

        @SuppressLint("NonConstantResourceId")
        View.OnClickListener onClickListener = v -> {
            switch (v.getId()) {
                case R.id.GeneralPreferences:
                    startOptionActivity("General");
                    break;

                case R.id.InfoPreferences:
                    startOptionActivity("About");
                    break;

                case R.id.ProfilePreferences:
                    startActivity(new Intent(this, ProfileActivity.class));
                    break;
            }
        };

        GeneralPreferences.setOnClickListener(onClickListener);
        ProfilePreferences.setOnClickListener(onClickListener);
        InfoPreferences.setOnClickListener(onClickListener);

        if (prefs.getString("FirebaseIIDPrefix", "").isEmpty()) {
            FirebaseInstallations.getInstance().getId().addOnCompleteListener(task -> {
                if (task.isSuccessful())
                    prefs.edit().putString("FirebaseIIDPrefix", task.getResult()).apply();
            });
        }

        if (prefs.getString("AndroidIDPrefix", "").isEmpty()) {
            prefs.edit().putString("AndroidIDPrefix", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID)).apply();
        }

        if (prefs.getString("GUIDPrefix", "").isEmpty()) {
            prefs.edit().putString("GUIDPrefix", UUID.randomUUID().toString()).apply();
        }

        if (prefs.getString("MacIDPrefix", "").isEmpty()) {
            String interfaceName = "wlan0";
            try {
                List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface intf : interfaces) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName)) continue;
                    byte[] mac = intf.getHardwareAddress();
                    if (mac == null) {
                        prefs.edit().putString("MacIDPrefix", "unknown").apply();
                        break;
                    }
                    StringBuilder buf = new StringBuilder();
                    for (byte b : mac) buf.append(String.format("%02X:", b));
                    if (buf.length() > 0) buf.deleteCharAt(buf.length() - 1);
                    prefs.edit().putString("MacIDPrefix", buf.toString()).apply();
                    break;
                }
            } catch (Exception e) {
                prefs.edit().putString("MacIDPrefix", "unknown").apply();
            }
        }

        NetProfile netProfile = NetProfile.getNetProfileFromPrefs(this);
        if(!prefs.getBoolean("IsFcmTopicSubscribed", false) && netProfile != null) {
            FirebaseMessaging.getInstance().subscribeToTopic(Objects.requireNonNull(netProfile.fcmTopic));
        }

        getAPIKeyFromCloud(this);
    }

    void startOptionActivity(String optionType) {
        Intent intent = new Intent(this, OptionActivity.class);
        intent.putExtra("Type", optionType);
        startActivity(intent);
    }

    public static void getAPIKeyFromCloud(Activity mContext) {
        FirebaseFirestore mFirebaseFireStore = FirebaseFirestore.getInstance();
        mFirebaseFireStore.collection("ApiKey")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Application.getPreferences(mContext).edit()
                                    .putString("Play_Latest", document.getString("Play_Latest"))
                                    .putString("ApiKey_FCM", document.getString("ApiKey_FCM"))
                                    .apply();
                        }
                    } else {
                        new MaterialAlertDialogBuilder(mContext)
                                .setTitle("Error occurred!")
                                .setMessage("Error occurred while initializing client token.\nplease check your internet connection and try again.")
                                .setPositiveButton("OK", (dialog, which) -> mContext.finishAndRemoveTask())
                                .setCancelable(false);
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        MonetCompat.setup(context);
        mMonet = MonetCompat.getInstance();
        mMonet.updateMonetColors();
        return super.onCreateView(name, context, attrs);
    }
}
