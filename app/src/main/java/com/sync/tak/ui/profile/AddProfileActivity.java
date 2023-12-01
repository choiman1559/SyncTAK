package com.sync.tak.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.sync.tak.R;
import com.sync.tak.utils.ui.SwitchedCard;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class AddProfileActivity extends AppCompatActivity {

    String profileKey;
    NetProfile profile;
    SharedPreferences profilePrefs;

    ExtendedFloatingActionButton saveButton;
    SwitchedCard profileEncryptionEnabled;
    SwitchedCard profileHMACEnabled;

    TextInputEditText profileName;
    TextInputEditText profileGroupID;
    TextInputEditText profileEncryptionKey;
    TextInputEditText profileHMACKey;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_profile);
        profilePrefs = getSharedPreferences(getPackageName() + "_profile_preferences", Context.MODE_PRIVATE);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener((v) -> this.finish());

        saveButton = findViewById(R.id.saveButton);
        profileEncryptionEnabled = findViewById(R.id.profileEncryptionEnabled);
        profileHMACEnabled = findViewById(R.id.profileHMACEnabled);

        profileName = findViewById(R.id.profileName);
        profileGroupID = findViewById(R.id.profileGroupID);
        profileEncryptionKey = findViewById(R.id.profileEncryptionKey);
        profileHMACKey = findViewById(R.id.profileHMACKey);

        profileEncryptionEnabled.setOnClickListener(v -> profileEncryptionEnabled.setSwitchChecked(!profileEncryptionEnabled.isChecked()));
        profileHMACEnabled.setOnClickListener(v -> profileHMACEnabled.setSwitchChecked(!profileHMACEnabled.isChecked()));

        if(getIntent().hasExtra("profileKey")) {
            profileKey = getIntent().getStringExtra("profileKey");
            profile = NetProfile.getNetProfile(profilePrefs.getString(profileKey, ""));

            profileEncryptionEnabled.setSwitchChecked(profile.isEncrypt);
            profileHMACEnabled.setSwitchChecked(profile.useHMAC);

            profileName.setText(profile.profileName);
            profileGroupID.setText(profile.fcmTopic);
            profileEncryptionKey.setText(profile.encryptKey);
            profileHMACKey.setText(profile.hmacKey);
        } else {
            profileKey = "";
            profile = new NetProfile();
        }

        saveButton.setOnClickListener((v) -> {
            boolean isSafeToSave = true;

            String profileNameString = Objects.requireNonNull(profileName.getText()).toString();
            if(profileNameString.isBlank()) {
                isSafeToSave = false;
                profileName.setError("profile name is blank");
            } else {
                profile.profileName = profileNameString;
            }

            String profileGroupIDString = Objects.requireNonNull(profileGroupID.getText()).toString();
            if(profileGroupIDString.isBlank()) {
                isSafeToSave = false;
                profileGroupID.setError("profile group ID is blank");
            } else {
                profile.fcmTopic = profileGroupIDString;
            }

            String profileEncryptionKeyString = Objects.requireNonNull(profileEncryptionKey.getText()).toString();
            if(!profileEncryptionKeyString.isBlank()) {
                profile.encryptKey = profileEncryptionKeyString;
            }

            String profileHMACKeyString = Objects.requireNonNull(profileHMACKey.getText()).toString();
            if(!profileEncryptionKeyString.isBlank()) {
                profile.hmacKey = profileHMACKeyString;
            }

            profile.isEncrypt = profileEncryptionEnabled.isChecked();
            profile.useHMAC = profileHMACEnabled.isChecked();

            if(isSafeToSave) {
                profilePrefs.edit().putString(profile.getUniqueID(), profile.toString()).apply();
                if(!profileKey.equals(profile.getUniqueID())) {
                    Set<String> keys = new HashSet<>(profilePrefs.getStringSet(ProfileActivity.profileIDListKey, new HashSet<>()));
                    keys.add(profile.getUniqueID());
                    profilePrefs.edit().putStringSet(ProfileActivity.profileIDListKey, keys).apply();
                }
                finish();
            }
        });
    }
}
