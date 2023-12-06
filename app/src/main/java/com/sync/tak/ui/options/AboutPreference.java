package com.sync.tak.ui.options;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kieronquinn.monetcompat.core.MonetCompat;
import com.sync.tak.Application;
import com.sync.tak.R;

public class AboutPreference extends PreferenceFragmentCompat  {

    Activity mContext;
    MonetCompat monet;
    SharedPreferences prefs;
    Preference appVersion;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        MonetCompat.setup(requireContext());
        monet = MonetCompat.getInstance();
        monet.updateMonetColors();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        monet = null;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity) mContext = (Activity) context;
        else throw new RuntimeException("Can't get Activity instanceof Context!");
    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.about_preferences, rootKey);
        prefs = Application.getPreferences(mContext);

        appVersion = findPreference("appVersion");

        try {
            if(appVersion != null) {
                appVersion.setSummary(mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName);
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(@NonNull Preference preference) {
        super.onPreferenceTreeClick(preference);
        MaterialAlertDialogBuilder dialog;

        switch (preference.getKey()) {
            case "githubRepository":
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/choiman1559/SyncTAK")));
                break;

            case "openSource":
                dialog = new MaterialAlertDialogBuilder(new ContextThemeWrapper(mContext, R.style.Theme_App_Palette_Dialog));
                dialog.setTitle("Open Source Licenses");
                dialog.setMessage(R.string.ossl);
                dialog.setIcon(R.drawable.ic_fluent_database_search_24_regular);
                dialog.setPositiveButton("OK", (dialog1, which) -> { });
                dialog.show();
                break;

            case "hammerNotice":
                dialog = new MaterialAlertDialogBuilder(new ContextThemeWrapper(mContext, R.style.Theme_App_Palette_Dialog));
                dialog.setTitle("US Government Legal Notice");
                dialog.setMessage(R.string.hammerNotice);
                dialog.setIcon(R.drawable.ic_fluent_database_search_24_regular);
                dialog.setPositiveButton("OK", (dialog1, which) -> { });
                dialog.show();
                break;
        }
        return true;
    }
}
