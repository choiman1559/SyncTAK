package com.sync.tak.utils.plugin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.atakmap.android.ipc.AtakBroadcast;
import com.sync.tak.receivers.CoTTransmittingReceiver;
import com.sync.tak.CoTPositionTool;

import com.atakmap.android.dropdown.DropDown;
import com.atakmap.android.dropdown.DropDownReceiver;
import com.atakmap.android.importexport.CotEventFactory;
import com.atakmap.android.maps.MapEvent;
import com.atakmap.android.maps.MapEventDispatcher;
import com.atakmap.android.maps.MapGroup;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Marker;
import com.atakmap.android.maps.PointMapItem;
import com.atakmap.android.menu.PluginMenuParser;
import com.atakmap.coremap.cot.event.CotDetail;
import com.atakmap.coremap.cot.event.CotEvent;
import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.coords.GeoPoint;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ModemCotUtility extends DropDownReceiver implements DropDown.OnStateListener, MapEventDispatcher.MapEventDispatchListener {
    public static final String TAG = ModemCotUtility.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")
    private static ModemCotUtility instance = null;
    private final Context context;

    public static boolean useAbbreviatedCoT = true;
    public static CoTTransmittingReceiver coTTransmittingReceiver;
    private final Set<ChatMessageListener> chatMessageListenerSet = new HashSet<>();

    public interface ChatMessageListener {
        void chatReceived(String message, String callSign, String timeMillis, String callSignDestination);
    }

    public static ModemCotUtility getInstance(MapView mapView, Context context) {
        if (instance == null) {
            instance = new ModemCotUtility(mapView, context);
        }
        return instance;
    }

    public static Collection<MapItem> getMapItemsInGroup(MapGroup mapGroup, Collection<MapItem> mapItems) {
        Collection<MapGroup> childGroups = mapGroup.getChildGroups();
        if (childGroups.size() == 0) {
            return mapGroup.getItems();
        } else {
            for (MapGroup childGroup : childGroups) {
                mapItems.addAll(getMapItemsInGroup(childGroup, mapItems));
            }
        }

        return mapItems;
    }

    private ModemCotUtility(MapView mapView, Context context) {
        super(mapView);
        this.context = context;

        Collection<MapItem> mapItems = getMapItemsInGroup(getMapView().getRootGroup(), new HashSet<>());
        for (MapItem mapItem : mapItems) {
            mapItem.setMetaString("menu", getMenu());
        }

        getMapView().getMapEventDispatcher().addMapEventListener(MapEvent.ITEM_ADDED, this);
    }

    @Override
    public void onDropDownSelectionRemoved() {

    }

    @Override
    public void onDropDownClose() {

    }

    @Override
    public void onDropDownSizeChanged(double v, double v1) {

    }

    @Override
    public void onDropDownVisible(boolean b) {

    }

    @Override
    protected void disposeImpl() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action != null && action
                .equals("com.sync.tak.receivers.cotMenu")) {
            PointMapItem temp = findTarget(intent.getStringExtra("targetUID"));
            if (temp != null) {
                sendCoT(temp);
            }
        }
    }

    private PointMapItem findTarget(final String targetUID) {
        PointMapItem pointItem = null;
        if (targetUID != null) {
            MapItem item = getMapView().getMapItem(targetUID);
            if (item instanceof PointMapItem) {
                pointItem = (PointMapItem) item;
            }
        }
        return pointItem;
    }

    /**
     * Start the CoT stream listener
     */
    @SuppressLint({"StaticFieldLeak", "UnspecifiedRegisterReceiverFlag"})
    public void startListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            android.util.Log.d(TAG, "startCotListener: " + getMapView().getContext().getPackageName());
        }

        Log.d("version tick", "10");
        CoTTransmittingReceiver.mOnMessageReceiveListener = message -> {
            android.util.Log.d(TAG, "onPostExecute: " + message);
            if (!message.isBlank()) {
                parseCoT(message);
            }
        };

        CoTTransmittingReceiver.onMetaDataReceiveListener = isAbbreviated -> useAbbreviatedCoT = isAbbreviated;
        CoTTransmittingReceiver.sendBroadcastMetaDataRequest(getMapView().getContext());
    }

    public void stopListener() {
        android.util.Log.d(TAG, "stopListener: ");
        CoTTransmittingReceiver.mOnMessageReceiveListener = null;
        CoTTransmittingReceiver.onMetaDataReceiveListener = null;
        AtakBroadcast.getInstance().unregisterSystemReceiver(coTTransmittingReceiver);
    }

    public void parseCoT(String message) {
        boolean foundStart = false;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);

            if (c != '0' && !foundStart) {
                foundStart = true;
            }

            if (foundStart) {
                stringBuilder.append(c);
            }
        }

        Log.d("ddd", message);

        // is chat message
        if (message.startsWith("chat@@@")) {
            String[] result = message.split("@@@");
            String chatMessage = result[1];
            String callSign = result[2];
            String callSignToSendTo = result[3];
            String time = result[4];

            String myCallSign = MapView.getMapView().getDeviceCallsign();

            android.util.Log.d(TAG, "parseCoT: " + callSignToSendTo);
            android.util.Log.d(TAG, "parseCoT: " + myCallSign);

            if (callSignToSendTo.equals(myCallSign) || callSignToSendTo.equals("ALL")) {
                android.util.Log.d(TAG, "parseCoT: was equal");
                for (ChatMessageListener i : chatMessageListenerSet) {
                    i.chatReceived(chatMessage, callSign, time, callSignToSendTo);
                }
            }
        } else {
            CotEvent cotEvent = null;
            try {
                cotEvent = CotEvent.parse(stringBuilder.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (cotEvent != null) {
                android.util.Log.d(TAG, "parseCoT: " + cotEvent);
                GeoPoint geoPoint = cotEvent.getGeoPoint();
                Marker m = new Marker(geoPoint, cotEvent.getUID());
                m.setType(cotEvent.getType());
                m.setMetaString("start", cotEvent.getStart().toString());
                m.setMetaString("how", cotEvent.getHow());

                CotDetail contactElem = cotEvent.getDetail().getFirstChildByName(0, "contact");
                if (contactElem != null && contactElem.getAttribute("callsign") != null) {
                    m.setTitle(contactElem.getAttribute("callsign"));
                } else {
                    m.setTitle("HAMMER-" + cotEvent.getType());
                }

                m.setMetaBoolean("transient", false);
                m.setMetaBoolean("archive", true);
                m.setMetaBoolean("movable", true);
                m.setMetaBoolean("removable", true);
                m.setMetaBoolean("editable", true);

                CotDetail remarksElem = cotEvent.getDetail().getFirstChildByName(0, "remarks");
                if (remarksElem != null) {
                    m.setMetaString("remarks", remarksElem.getInnerText());
                }

                m.setMetaString("menu", getMenu());

                String type = "Unknown";
                if (cotEvent.getHow().contains("-h-")) {
                    type = "Hostile";
                } else if (cotEvent.getHow().contains("-f-")) {
                    type = "Friendly";
                } else if (cotEvent.getHow().contains("-n-")) {
                    type = "Neutral";
                }

                MapGroup mapGroup = getMapView().getRootGroup()
                        .findMapGroup("Cursor on Target")
                        .findMapGroup(type);
                mapGroup.addItem(m);

                Log.d(TAG, "creating a new unit marker for: " + m.getUID());

            }
        }
    }

    private String getMenu() {
        return PluginMenuParser.getMenu(context, "menu.xml");
    }

    @Override
    public void onMapEvent(MapEvent mapEvent) {
        mapEvent.getItem().setMetaString("menu", getMenu());
    }

    public void sendCoT(MapItem mapItem) {
        CotEvent cotEvent;
        if (useAbbreviatedCoT) {
            cotEvent = CoTPositionTool.createCoTEvent(mapItem);
        } else {
            cotEvent = CotEventFactory.createCotEvent(mapItem);
        }

        android.util.Log.d(TAG, "sending " + (useAbbreviatedCoT ? "abbreviated" : "non-abbreviated") + " COT: " + cotEvent.toString());
        CoTTransmittingReceiver.sendBroadcastSend(context, cotEvent.toString());
    }

    public void sendChat(String message, String callSignToSendTo) {
        String callSign = MapView.getMapView().getDeviceCallsign();
        String encodedMessage = "chat@@@" + message + "@@@" + callSign + "@@@" + callSignToSendTo + "@@@" + System.currentTimeMillis();

        android.util.Log.d(TAG, "sending chat message: " + encodedMessage);
        CoTTransmittingReceiver.sendBroadcastSend(context, encodedMessage);
    }

    public void registerChatListener(ChatMessageListener chatMessageListener) {
        chatMessageListenerSet.add(chatMessageListener);
    }
}
