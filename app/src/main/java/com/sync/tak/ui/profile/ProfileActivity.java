package com.sync.tak.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sync.tak.Application;
import com.sync.tak.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class ProfileActivity extends AppCompatActivity {

    public static final String profileIDListKey = "$profileID";
    public static final String profileSelectedKey = "$profileSelected";

    SharedPreferences prefs;
    SharedPreferences profilePrefs;

    LinearLayoutCompat profileNotAvailableLayout;
    LinearLayoutCompat profileListLayout;

    ProfileHolder lastSelectedProfile;
    ArrayList<ProfileHolder> profileItemList = new ArrayList<>();

    SharedPreferences.OnSharedPreferenceChangeListener onPreferenceChangeListener = (preference, key) -> {
        if(key == null) return;

        ArrayList<String> keys = new ArrayList<>();
        for(ProfileHolder holder : profileItemList) {
            keys.add(holder.profile.getUniqueID());
        }

        if(key.equals(profileIDListKey)) {
            for(String object : profilePrefs.getStringSet(profileIDListKey, new HashSet<>())) {
                if(!keys.contains(object)) {
                    addItem(NetProfile.getNetProfile(profilePrefs.getString(object, "")));
                    initViews(false);

                    if(profileItemList.size() == 1) {
                        selectProfile(profileItemList.get(0));
                    }
                    return;
                }
            }
        } else if(!key.equals(profileSelectedKey) && !profilePrefs.getString(key, "").isBlank()) {
            if(keys.contains(key)) {
                int index = keys.indexOf(key);
                ProfileHolder holder = profileItemList.get(index);
                holder.profile = NetProfile.getNetProfile(profilePrefs.getString(key, ""));
                holder.updateProfile();
                profileItemList.set(index, holder);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        prefs = Application.getPreferences(this);
        profilePrefs = getSharedPreferences(getPackageName() + "_profile_preferences", Context.MODE_PRIVATE);
        profilePrefs.registerOnSharedPreferenceChangeListener(onPreferenceChangeListener);

        ExtendedFloatingActionButton profileAddButton = findViewById(R.id.profileAddButton);
        profileAddButton.setOnClickListener((v) -> startActivity(new Intent(this, AddProfileActivity.class)));

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener((v) -> this.finish());

        profileNotAvailableLayout = findViewById(R.id.profileNotAvailableLayout);
        profileListLayout = findViewById(R.id.profileListLayout);

        initViews(true);
    }

    void initViews(boolean initList) {
        if(profilePrefs.getStringSet(profileIDListKey, new HashSet<>()).size() == 0) {
            profileNotAvailableLayout.setVisibility(View.VISIBLE);
            if(!initList) {
                prefs.edit().putBoolean("IsFcmTopicSubscribed", false).apply();
            }
        } else {
            profileNotAvailableLayout.setVisibility(View.GONE);
            if(initList) {
                for (String key : profilePrefs.getStringSet(profileIDListKey, new HashSet<>())) {
                    NetProfile profile = NetProfile.getNetProfile(profilePrefs.getString(key, ""));
                    if (profile != null) {
                        addItem(profile);
                    }
                }
            }
        }
    }

    void addItem(NetProfile profile) {
        CoordinatorLayout layout = (CoordinatorLayout) View.inflate(this, R.layout.cardview_profile, null);
        ProfileHolder profileHolder = new ProfileHolder(profile, layout);

        if(profile.getUniqueID().equals(profilePrefs.getString(profileSelectedKey, ""))) {
            profileHolder.profileSelected.setChecked(true);
            lastSelectedProfile = profileHolder;
        }

        profileHolder.Parent.setOnClickListener(v -> selectProfile(profileHolder));
        profileHolder.profileDelete.setOnClickListener(v -> {
            MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(new ContextThemeWrapper(ProfileActivity.this, R.style.Theme_App_Palette_Dialog));
            dialog.setIcon(R.drawable.ic_fluent_edit_24_regular);
            dialog.setCancelable(false);
            dialog.setTitle("Delete profile");
            dialog.setMessage(String.format(Locale.getDefault(), "Are you sure you want to delete profile \"%s\"?\n\nThis action cannot be undone.", profile.profileName));
            dialog.setNegativeButton("Cancel", (dialog12, which) -> { });
            dialog.setPositiveButton("Delete", (dialog1, which) -> {
                Set<String> keys = new HashSet<>(profilePrefs.getStringSet(ProfileActivity.profileIDListKey, new HashSet<>()));
                keys.remove(profile.getUniqueID());
                profilePrefs.edit()
                        .putStringSet(ProfileActivity.profileIDListKey, keys)
                        .remove(profile.getUniqueID())
                        .apply();

                profileItemList.remove(profileHolder);
                profileListLayout.removeView(layout);
                if(profileItemList.size() == 1) {
                    selectProfile(profileItemList.get(0));
                }

                initViews(false);
            });
            dialog.show();
        });

        profileListLayout.addView(layout);
        profileItemList.add(profileHolder);
    }

    void selectProfile(ProfileHolder profileHolder) {
        if(lastSelectedProfile.equals(profileHolder)) {
            return;
        }

        profileHolder.profileSelected.setChecked(true);
        if(lastSelectedProfile != null) {
            lastSelectedProfile.profileSelected.setChecked(false);
        }

        lastSelectedProfile = profileHolder;
        profilePrefs.edit().putString(profileSelectedKey, profileHolder.profile.getUniqueID()).apply();
        FirebaseMessaging.getInstance().subscribeToTopic(Objects.requireNonNull(profileHolder.profile.fcmTopic));
        prefs.edit().putBoolean("IsFcmTopicSubscribed", true).apply();
    }

    class ProfileHolder {

        NetProfile profile;
        AppCompatRadioButton profileSelected;
        TextView profileName;
        TextView profileGroup;
        AppCompatImageButton profileEdit;
        AppCompatImageButton profileDelete;
        MaterialCardView Parent;

        public ProfileHolder(NetProfile profile, View view) {
            this.profile = profile;

            this.profileSelected = view.findViewById(R.id.profileSelected);
            this.profileName = view.findViewById(R.id.profileName);
            this.profileGroup = view.findViewById(R.id.profileGroup);
            this.profileEdit = view.findViewById(R.id.profileEdit);
            this.profileDelete = view.findViewById(R.id.profileDelete);
            this.Parent = view.findViewById(R.id.Parent);

            this.profileEdit.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, AddProfileActivity.class).putExtra("profileKey", profile.getUniqueID())));
            updateProfile();
        }

        public void updateProfile() {
            profileName.setText(profile.profileName);
            profileGroup.setText(String.format(Locale.getDefault(), "Group: %s", profile.fcmTopic));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(lastSelectedProfile != null) {
            lastSelectedProfile.profileSelected.setChecked(true);
        }
    }
}
