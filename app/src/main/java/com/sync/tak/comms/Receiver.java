package com.sync.tak.comms;

import com.sync.tak.utils.AsyncTask;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by roman on 2/10/15.
 */
public class Receiver extends AsyncTask<Void, Double, Result> {

    final static String TAG = "Receiver";
    protected AtomicBoolean cotReceived;

    public Receiver(AtomicBoolean cotReceived){
        this.cotReceived = cotReceived;
    }

    @Override
    protected Result doInBackground(Void... params) {
        //TODO listen FCM data here
        //Log.i(TAG, "Received " + output.toByteArray().length + " bytes");

        String str = new String(new byte[0], StandardCharsets.UTF_8);
        return new Result(str, null);
    }
}

