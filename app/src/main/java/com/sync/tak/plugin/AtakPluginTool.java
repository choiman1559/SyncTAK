package com.sync.tak.plugin;

import com.atak.plugins.impl.AbstractPluginTool;
import com.sync.tak.R;
import com.sync.tak.receivers.CoTUtilityDropDownReceiver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import androidx.appcompat.content.res.AppCompatResources;

import gov.tak.api.util.Disposable;

public class AtakPluginTool extends AbstractPluginTool implements Disposable {

    @SuppressLint("StaticFieldLeak")
    public Intent intent;

    public AtakPluginTool(Context context) {
        super(context,
                context.getString(R.string.app_name),
                context.getString(R.string.app_desc),
                AppCompatResources.getDrawable(context, R.drawable.ic_atak_menu_foreground),
                CoTUtilityDropDownReceiver.SHOW_PLUGIN);
    }

    @Override
    public void dispose() {

    }
}
