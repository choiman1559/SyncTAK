package com.sync.tak.receivers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.sync.tak.R;
import com.atakmap.android.dropdown.DropDown;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.log.Log;

import java.util.Collection;
import java.util.LinkedHashSet;

import com.sync.tak.utils.ui.DropDownManager;
import com.sync.tak.utils.plugin.MapItems;
import com.sync.tak.utils.plugin.ModemCotUtility;

public class ViewCoTMarkersReceiver extends ViewTableReceiver implements
        DropDown.OnStateListener {

    public static final String TAG = ViewCoTMarkersReceiver.class
            .getSimpleName();

    public static final String VIEW_COT_MARKERS_RECEIVER = "com.atakmap.android.cot_utility.VIEW_COT_MARKERS_RECEIVER";

    private final View cotView;
    private final Context pluginContext;

    private final GridLayout table;
    private final MapView mapView;
    private LinkedHashSet<MapItem> cotMapItems;
    private Intent intent;

    @SuppressLint("InflateParams")
    public ViewCoTMarkersReceiver(MapView mapView, Context context) {
        super(mapView, context);
        this.pluginContext = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        cotView = inflater.inflate(R.layout.fragment_cot_items, null);
        this.mapView = mapView;
        table = cotView.findViewById(R.id.table);
        cotMapItems = new LinkedHashSet<>();
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        if(intent == null) {
            android.util.Log.w(TAG, "Doing nothing, because intent was null");
            return;
        }

        if (intent.getAction() == null) {
            android.util.Log.w(TAG, "Doing nothing, because intent action was null");
            return;
        }

        if (intent.getAction().equals(VIEW_COT_MARKERS_RECEIVER)) {
            Log.d(TAG, "showing view cot markers receiver");
            this.intent = intent;

            showDropDown(cotView, HALF_WIDTH, FULL_HEIGHT, FULL_WIDTH,
                    HALF_HEIGHT, false);

            ImageButton backButton = cotView.findViewById(R.id.backButtonMapItemsView);
            backButton.setOnClickListener(v -> ViewCoTMarkersReceiver.this.onBackButtonPressed());
            Button selfLocationButton = cotView.findViewById(R.id.selfLocationBtn);
            selfLocationButton.setOnClickListener(view -> {
                android.util.Log.d(TAG, "sending self position");
                ModemCotUtility.getInstance(mapView, pluginContext).sendCoT(mapView.getSelfMarker());
                Toast toast = Toast.makeText(context, "sending self marker", Toast.LENGTH_SHORT);
                toast.show();
            });

            cotMapItems = MapItems.getCursorOnTargetMapItems(mapView);

            updateList(cotMapItems);
        }
    }

    public void updateList(final Collection<MapItem> list) {
        android.util.Log.d(TAG, "updateList: " + list.toString());
        GridLayout.LayoutParams nameParams = newLayoutParams();
        GridLayout.LayoutParams typeParams = newLayoutParams();

        TextView nameColumnHeader = createTableHeader("Name");
        TextView typeColumnHeader = createTableHeader("Type");

        table.setColumnCount(2);

        // remove any residuals from the last time we displayed the table
        table.removeAllViews();
        table.addView(nameColumnHeader, nameParams);
        table.addView(typeColumnHeader, typeParams);

        int i = 0;
        for (final MapItem mapItem : list) {
            String backgroundColor = (i % 2 != 0) ? MED_DARK_GRAY : MED_GRAY;
            nameParams = newLayoutParams();
            typeParams = newLayoutParams();

            TextView mapItemName = createTableEntry(mapItem.getTitle(), backgroundColor);

            TextView mapItemInfo;
            mapItemInfo = createTableEntry(mapItem.getType(), backgroundColor);

            table.addView(mapItemName, nameParams);
            table.addView(mapItemInfo, typeParams);
            mapItemName.setOnClickListener(view -> ModemCotUtility.getInstance(mapView, pluginContext).sendCoT(mapItem));

            i++;
        }

    }

    protected boolean onBackButtonPressed() {
        DropDownManager.getInstance().clearBackStack();
        DropDownManager.getInstance().removeFromBackStack();
        intent.setAction(CoTUtilityDropDownReceiver.SHOW_PLUGIN);
        AtakBroadcast.getInstance().sendBroadcast(intent);
        return true;
    }
}
