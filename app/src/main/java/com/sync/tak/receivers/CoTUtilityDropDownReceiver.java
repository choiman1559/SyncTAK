
package com.sync.tak.receivers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.atakmap.android.dropdown.DropDownReceiver;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.dropdown.DropDown.OnStateListener;

import com.sync.tak.R;
import com.atakmap.coremap.log.Log;

import java.util.Objects;

public class CoTUtilityDropDownReceiver extends DropDownReceiver implements OnStateListener{

    public static final String TAG = CoTUtilityDropDownReceiver.class
            .getSimpleName();

    public static final String SHOW_PLUGIN = "com.atakmap.android.cot_utility.SHOW_PLUGIN";
    private final View mainView;

    /**************************** CONSTRUCTOR *****************************/

    @SuppressLint("InflateParams")
    public CoTUtilityDropDownReceiver(final MapView mapView,
                                      final Context context) {
        super(mapView);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mainView = inflater.inflate(R.layout.fragment_inapp_main, null);
    }

    /**************************** PUBLIC METHODS *****************************/

    public void disposeImpl() {
    }

    /**************************** INHERITED METHODS *****************************/

    @Override
    public void onReceive(final Context context, Intent intent) {

        Log.d(TAG, "showing plugin drop down");
        if (Objects.equals(intent.getAction(), SHOW_PLUGIN)) {
            showDropDown(mainView, HALF_WIDTH, FULL_HEIGHT, FULL_WIDTH, HALF_HEIGHT, false);

            Button viewCoTMarkersButton = mainView.findViewById(R.id.viewCotItemsBtn);
            viewCoTMarkersButton.setOnClickListener(view -> {
                android.util.Log.d(TAG, "onClick: ");
                Intent intent1 = new Intent();
                intent1.setAction(ViewCoTMarkersReceiver.VIEW_COT_MARKERS_RECEIVER);
                AtakBroadcast.getInstance().sendBroadcast(intent1);
            });

            Button sendChatButton = mainView.findViewById(R.id.sendChatBtn);
            sendChatButton.setOnClickListener(view -> {
                Intent intent12 = new Intent();
                intent12.setAction(SendChatDropDownReceiver.SEND_CHAT_RECEIVER);
                AtakBroadcast.getInstance().sendBroadcast(intent12);
            });

            /*
                Start listener
             */
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.RECORD_AUDIO}, 1234);
            }
        }
    }

    @Override
    public void onDropDownSelectionRemoved() {
    }

    @Override
    public void onDropDownVisible(boolean v) {
    }

    @Override
    public void onDropDownSizeChanged(double width, double height) {
    }

    @Override
    public void onDropDownClose() {
    }

}
