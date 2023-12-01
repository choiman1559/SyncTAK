package com.sync.tak;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.sync.tak.utils.plugin.CotUtil;
import com.sync.tak.utils.plugin.ModemCotUtility;
import com.sync.tak.receivers.SendChatDropDownReceiver;
import com.sync.tak.receivers.ViewCoTMarkersReceiver;
import com.sync.tak.receivers.CoTUtilityDropDownReceiver;

import com.atakmap.android.dropdown.DropDownReceiver;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.ipc.AtakBroadcast.DocumentedIntentFilter;

import com.atakmap.android.ipc.DocumentedExtra;
import com.atakmap.android.maps.MapEvent;
import com.atakmap.android.maps.MapEventDispatcher;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.dropdown.DropDownMapComponent;

import com.atakmap.comms.CommsMapComponent;
import com.atakmap.comms.CotServiceRemote;
import com.atakmap.coremap.cot.event.CotEvent;
import com.atakmap.coremap.log.Log;

public class CoTUtilityMapComponent extends DropDownMapComponent implements CotUtil.CotEventListener,  CotServiceRemote.CotEventListener, MapEventDispatcher.MapEventDispatchListener {

    public static final String TAG = "PluginMain";
    public Context pluginContext;
    ModemCotUtility modemCotUtility;

    public void onCreate(final Context context, Intent intent, final MapView view) {
        view.getMapEventDispatcher().addMapEventListener(MapEvent.ITEM_ADDED,this);
        context.setTheme(R.style.ATAKPluginTheme);
        super.onCreate(context, intent, view);
        pluginContext = context;

        CoTUtilityDropDownReceiver ddr = new CoTUtilityDropDownReceiver(view, context);
        Log.d(TAG, "registering the plugin filter");

        DocumentedIntentFilter ddFilter = new DocumentedIntentFilter();
        ddFilter.addAction(CoTUtilityDropDownReceiver.SHOW_PLUGIN);
        registerDropDownReceiver(ddr, ddFilter);

        CotUtil.setCotEventListener(this);

        CommsMapComponent.getInstance().addOnCotEventListener(this);
        modemCotUtility = ModemCotUtility.getInstance(view, context);

        DocumentedIntentFilter filter = new DocumentedIntentFilter();
        filter.addAction("com.sync.tak.receivers.cotMenu", "this intent launches the cot send utility",
                new DocumentedExtra[] { new DocumentedExtra("targetUID", "the map item identifier used to populate the drop down")});
        registerDropDownReceiver(modemCotUtility, filter);
        modemCotUtility.startListener();

        SendChatDropDownReceiver sendChatDropDownReceiver = new SendChatDropDownReceiver(view, context);
        registerReceiverUsingPluginContext(pluginContext, "sendchat receiver", sendChatDropDownReceiver, SendChatDropDownReceiver.SEND_CHAT_RECEIVER);

        ViewCoTMarkersReceiver viewCoTMarkersReceiver = new ViewCoTMarkersReceiver(view, context);
        registerReceiverUsingPluginContext(pluginContext, "view markers receiver", viewCoTMarkersReceiver, ViewCoTMarkersReceiver.VIEW_COT_MARKERS_RECEIVER);
    }

    private void registerReceiverUsingPluginContext(Context pluginContext, String name, DropDownReceiver rec, String actionName) {
        android.util.Log.d(TAG, "Registering " + name + " receiver with intent filter");
        AtakBroadcast.DocumentedIntentFilter mainIntentFilter = new AtakBroadcast.DocumentedIntentFilter();
        mainIntentFilter.addAction(actionName);
        this.registerReceiver(pluginContext, rec, mainIntentFilter);
    }

    private void registerReceiverUsingAtakContext(String name, DropDownReceiver rec, String actionName) {
        android.util.Log.d(TAG, "Registering " + name + " receiver with intent filter");
        AtakBroadcast.DocumentedIntentFilter mainIntentFilter = new AtakBroadcast.DocumentedIntentFilter();
        mainIntentFilter.addAction(actionName);
        AtakBroadcast.getInstance().registerReceiver(rec, mainIntentFilter);
    }

    @Override
    protected void onDestroyImpl(Context context, MapView view) {
        super.onDestroyImpl(context, view);
        modemCotUtility.stopListener();
    }

    @Override
    public void onReceiveCotEvent(CotEvent cotEvent) {
        android.util.Log.d(TAG, "onReceiveCotEvent: " + cotEvent);
    }

    @Override
    public void onCotEvent(CotEvent cotEvent, Bundle bundle) {
        android.util.Log.d(TAG, "onReceiveMapEvent: " + cotEvent.toString());
    }

    @Override
    public void onMapEvent(MapEvent mapEvent) {
        android.util.Log.d(TAG, "onReceiveMapEvent: " + mapEvent.getType());
    }
}
