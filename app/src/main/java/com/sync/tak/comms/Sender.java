package com.sync.tak.comms;

import android.util.Log;

import com.sync.tak.utils.AsyncTask;
import com.sync.tak.utils.ModemCotUtility;


public class Sender extends AsyncTask<String, Double, Void> {
    private final ModemCotUtility modemCotUtility;
    public Sender(ModemCotUtility modemCotUtility){
        this.modemCotUtility = modemCotUtility;
    }
    final static String TAG = "Sender";

    @Override
    protected Void doInBackground(String... params) {
        final byte[] data = params[0].getBytes();

        Log.i(TAG, "Sending " + data.length + " bytes");

        //TODO: Send Data here;
        modemCotUtility.startListener();
        return null;
    }
}