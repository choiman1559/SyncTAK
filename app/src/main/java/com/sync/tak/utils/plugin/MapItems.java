package com.sync.tak.utils.plugin;

import android.util.Log;

import com.atakmap.android.maps.MapGroup;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class MapItems {
    private static final String TAG = MapItems.class.getName();

    // Utility to print all map groups and sub map items
    public static void printMapGroupsMapItems(MapGroup mapGroup, int counter){
        printMapGroupsMapItemsSub(mapGroup,0);
    }
    private static void printMapGroupsMapItemsSub(MapGroup mapGroup, int counter){
        int j = 0;
        String test = "";
        while(j <= counter){
            j++;
            test += "-";
        }

        Log.d(TAG, "printMapGroups: " + test + mapGroup.getFriendlyName());
        Collection<MapGroup> collection = mapGroup.getChildGroups();
        Iterator<MapGroup> iterator = collection.iterator();

        if(mapGroup.getItems().size() > 0){
            Collection<MapItem> items = mapGroup.getItems();
            for (MapItem item : items) {
                Log.d(TAG, "printMapGroups: " + test + " " + item.toString());
            }
        }

        while(iterator.hasNext()){
            printMapGroupsMapItemsSub(iterator.next(), counter + 1);
        }
    }

    public static Collection<MapItem> getMapItemsInGroup(MapGroup mapGroup, Collection<MapItem> mapItems){
        if(mapGroup == null){
            return null;
        }

        Collection<MapGroup> childGroups = mapGroup.getChildGroups();

        if(childGroups.size() == 0){
            return mapGroup.getItems();
        }else {
            for (MapGroup childGroup : childGroups) {
                mapItems.addAll(getMapItemsInGroup(childGroup, mapItems));
            }
        }

        return mapItems;
    }

    public static LinkedHashSet<MapItem> getCursorOnTargetMapItems(MapView mapView){
        MapGroup cotMapGroup = mapView.getRootGroup().findMapGroup("Cursor on Target");
        LinkedHashSet<MapItem> cotMapItems = new LinkedHashSet<>(MapItems.getMapItemsInGroup(cotMapGroup, new ArrayList<MapItem>()));

        if(mapView.getRootGroup() != null) {
            MapGroup userObjects = mapView.getRootGroup().findMapGroup("User Objects");
            Collection<MapItem> subItems;
            subItems = MapItems.getMapItemsInGroup(userObjects.findMapGroup("Hostile"), new ArrayList<MapItem>());
            if(subItems != null) {
                cotMapItems.addAll(subItems);
            }
            subItems = MapItems.getMapItemsInGroup(userObjects.findMapGroup("Friendly"), new ArrayList<MapItem>());
            if(subItems != null)
                cotMapItems.addAll(subItems);
            subItems = MapItems.getMapItemsInGroup(userObjects.findMapGroup("Neutral"), new ArrayList<MapItem>());
            if(subItems != null)
                cotMapItems.addAll(subItems);
            subItems = MapItems.getMapItemsInGroup(userObjects.findMapGroup("Unknown"), new ArrayList<MapItem>());
            if(subItems != null)
                cotMapItems.addAll(subItems);
        }

        return cotMapItems;
    }

}
