package com.sync.tak.receivers;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;

import com.sync.tak.R;
import com.atakmap.android.dropdown.DropDown;
import com.atakmap.android.dropdown.DropDownReceiver;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.log.Log;

import utils.DropDownManager;

public class ReadMeReceiver extends DropDownReceiver implements DropDown.OnStateListener {
    public static final String TAG = CoTUtilityDropDownReceiver.class
            .getSimpleName();

    public static final String SHOW_README = "com.sync.tak.receivers.SHOW_README";
    private LayoutInflater inflater;
    private View mainView;


    private Intent intent;

    public ReadMeReceiver(MapView mapView, Context context) {
        super(mapView);

        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mainView = inflater.inflate(R.layout.read_me, null);

        ImageButton backButton = mainView.findViewById(R.id.backButtonReadMeView);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReadMeReceiver.this.onBackButtonPressed();
            }
        });
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
        if(intent == null) {
            android.util.Log.w(TAG, "Doing nothing, because intent was null");
            return;
        }

        if (intent.getAction() == null) {
            android.util.Log.w(TAG, "Doing nothing, because intent action was null");
            return;
        }

        if (intent.getAction().equals(SHOW_README)) {
            this.intent = intent;
            Log.d(TAG, "showing readme drop down");
            showDropDown(mainView, HALF_WIDTH, FULL_HEIGHT, FULL_WIDTH,
                    FULL_HEIGHT, false);

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
