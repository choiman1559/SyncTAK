
package com.sync.tak.plugin;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.atak.plugins.impl.AbstractPlugin;
import com.atak.plugins.impl.PluginContextProvider;
import com.atakmap.android.maps.MapComponent;
import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.log.Log;
import com.sync.tak.CoTUtilityMapComponent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import gov.tak.api.plugin.IServiceController;

public class AtakPluginLifecycle extends AbstractPlugin {
    private final Collection<MapComponent> overlays;
    private final MapView mapView;
    @SuppressLint("StaticFieldLeak")
    public static Context activity;

    private final static String TAG = AtakPluginLifecycle.class.getSimpleName();

    public AtakPluginLifecycle(IServiceController serviceController) {
        super(serviceController, new AtakPluginTool(serviceController.getService(PluginContextProvider.class).getPluginContext()), new CoTUtilityMapComponent());
        this.overlays = new LinkedList<>();
        this.mapView = null;

        final PluginContextProvider ctxProvider = serviceController.getService(PluginContextProvider.class);
        if (ctxProvider != null) {
            activity = ctxProvider.getPluginContext();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        AtakPluginLifecycle.this.overlays.add(new CoTUtilityMapComponent());

        Iterator<MapComponent> iter = AtakPluginLifecycle.this.overlays.iterator();
        MapComponent c;
        while (iter.hasNext()) {
            c = iter.next();
            try {
                c.onCreate(activity, ((Activity) activity).getIntent(), AtakPluginLifecycle.this.mapView);
            } catch (Exception e) {
                Log.w(TAG, "Unhandled exception trying to create overlays MapComponent", e);
                iter.remove();
            }
        }
    }
}
