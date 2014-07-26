package com.heb.castor.app.cast;

import android.content.Context;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.cast.CastDevice;

public class CastorMediaRouterCallback extends MediaRouter.Callback {
    private static final String TAG = CastorMediaRouterCallback.class.getSimpleName();

    private final Context context;
    private CastDevice castDevice;

    public CastorMediaRouterCallback(Context context) {
        this.context = context;
    }

    @Override
    public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo info) {
        Log.d(TAG, "onRouteSelected");
        castDevice = CastDevice.getFromBundle(info.getExtras());

        Toast.makeText(context,
                "blablalba", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo info) {
        Log.d(TAG, "onRouteUnselected: info=" + info);
        castDevice = null;
    }

    public CastDevice getCastDevice() {
        return castDevice;
    }
}

