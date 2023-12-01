package com.sync.tak.ui.options;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kieronquinn.monetcompat.core.MonetCompat;
import com.sync.tak.Application;
import com.sync.tak.R;
import com.sync.tak.receivers.CoTTransmittingReceiver;
import com.sync.tak.ui.NetSelectActivity;
import com.sync.tak.utils.ui.ToastHelper;

public class GeneralPreference extends PreferenceFragmentCompat  {

    Activity mContext;
    MonetCompat monet;
    SharedPreferences prefs;

    Preference useAbbreviated;
    Preference UseSplitData;
    Preference SplitInterval;
    Preference SplitAfterEncryption;

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
        setPreferencesFromResource(R.xml.general_preferences, rootKey);
        prefs = Application.getPreferences(mContext);

        useAbbreviated = findPreference("useAbbreviated");
        UseSplitData = findPreference("UseSplitData");
        SplitInterval = findPreference("SplitInterval");
        SplitAfterEncryption = findPreference("SplitAfterEncryption");

        useAbbreviated.setOnPreferenceChangeListener((preference, newValue) -> {
            CoTTransmittingReceiver.sendBroadcastMetaDataResponse(mContext);
            return true;
        });

        int splitIntervalValue = prefs.getInt("SplitInterval", 500);
        boolean useSplit = prefs.getBoolean("UseSplitData", false);
        SplitInterval.setVisible(useSplit);
        SplitAfterEncryption.setVisible(useSplit);
        SplitInterval.setSummary("Now : " + (splitIntervalValue == 500 ? "500 ms (Default)" : (splitIntervalValue < 1 ? "0 ms (Disabled)" : splitIntervalValue + " ms")));
        UseSplitData.setOnPreferenceChangeListener(((preference, newValue) -> {
            SplitInterval.setVisible((boolean) newValue);
            SplitAfterEncryption.setVisible((boolean) newValue);
            return true;
        }));
    }

    @Override
    public boolean onPreferenceTreeClick(@NonNull Preference preference) {
        super.onPreferenceTreeClick(preference);
        String key = preference.getKey();

        if (key.equals("providerSelect")) {
            mContext.startActivity(new Intent(mContext, NetSelectActivity.class));
        } else if(key.equals("SplitInterval")) {
            MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(new ContextThemeWrapper(mContext, R.style.Theme_App_Palette_Dialog));
            dialog.setIcon(R.drawable.ic_fluent_edit_24_regular);
            dialog.setCancelable(false);
            dialog.setTitle("Input Value");
            dialog.setMessage("The interval maximum limit is 2147483647 ms and Input 0 or lower to disable this option.");

            EditText editText = new EditText(mContext);
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setHint("Input interval value");
            editText.setGravity(Gravity.CENTER);
            editText.setText(String.valueOf(prefs.getInt("SplitInterval", 500)));

            LinearLayout parentLayout = new LinearLayout(mContext);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(30, 16, 30, 16);
            editText.setLayoutParams(layoutParams);
            parentLayout.addView(editText);
            dialog.setView(parentLayout);

            dialog.setPositiveButton("Apply", (d, w) -> {
                String value = editText.getText().toString();
                if (value.equals("")) {
                    ToastHelper.show(mContext, "Please Input Value", "DISMISS",ToastHelper.LENGTH_SHORT);
                } else {
                    int IntValue = Integer.parseInt(value);
                    if (IntValue > 0x7FFFFFFF - 1) {
                        ToastHelper.show(mContext, "Value must be lower than 2147483647", "DISMISS",ToastHelper.LENGTH_SHORT);
                    } else {
                        prefs.edit().putInt("SplitInterval", IntValue).apply();
                        SplitInterval.setSummary("Now : " + (IntValue == 500 ? "500 ms (Default)" : (IntValue < 1 ? "0 ms (Disabled)" : IntValue + " ms")));
                    }
                }
            });
            dialog.setNeutralButton("Reset Default", (d, w) -> {
                prefs.edit().putInt("SplitInterval", 500).apply();
                SplitInterval.setSummary("Now : 500 ms (Default)");
            });
            dialog.setNegativeButton("Cancel", (d, w) -> {});
            dialog.show();
        }
        return true;
    }
}
