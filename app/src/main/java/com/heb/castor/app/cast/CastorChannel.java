package com.heb.castor.app.cast;

import android.util.Log;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;

public class CastorChannel implements Cast.MessageReceivedCallback {

    private static final String CASTOR_NAMESPACE = "urn:x-cast:com.heb.castor.app";

    public String getNamespace() {
        return CASTOR_NAMESPACE;
    }

    @Override
    public void onMessageReceived(CastDevice castDevice, String s, String message) {
        Log.d("CastorChannel", "onMessageReceived: " + message);
    }
}
