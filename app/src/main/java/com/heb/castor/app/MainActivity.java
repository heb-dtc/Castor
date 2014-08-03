package com.heb.castor.app;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.heb.castor.app.cast.CastorCastClientListener;
import com.heb.castor.app.cast.CastorMediaRouterCallback;
import com.heb.castor.app.presentations.StandByPresentation;

import java.io.IOException;

public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String NAMESPACE = "urn:x-cast:com.heb.castor.app";

    private MediaRouter mediaRouter;
    private MediaRouteSelector mediaRouteSelector;
    private CastorMediaRouterCallback mediaRouterCallback;
    private Display castDisplay;

    private GoogleApiClient googleApiClient;

    private Button launchApplicationButton;
    private Button launchPresentationOnRemoteDisplay;
    private Button sendMessageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        launchApplicationButton = (Button) findViewById(R.id.launchApplicationButton);
        launchPresentationOnRemoteDisplay = (Button) findViewById(R.id.launchMediaApplicationButton);
        sendMessageButton = (Button) findViewById(R.id.sendMessageButton);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage("Salut Salut!");
            }
        });

        launchApplicationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToReceiverApplication(mediaRouterCallback.getCastDevice(), new CastorCastClientListener());
            }
        });

        launchPresentationOnRemoteDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startStandbyPresentation(castDisplay);
            }
        });

        mediaRouter = MediaRouter.getInstance(getApplicationContext());
        mediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(CastMediaControlIntent.categoryForCast(getString(R.string.cast_app_id)))
                .build();
        mediaRouterCallback = new CastorMediaRouterCallback(getApplicationContext());

    }

    private void startStandbyPresentation(Display castDisplay) {
        StandByPresentation standByPresentation = new StandByPresentation(this, castDisplay);
        standByPresentation.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(isMirroring()) {
            launchPresentationOnRemoteDisplay.setEnabled(true);
        } else {
            mediaRouter.addCallback(mediaRouteSelector, mediaRouterCallback,
                    MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
        }
    }

    @Override
    protected void onPause() {
        if(isFinishing()) {
            mediaRouter.removeCallback(mediaRouterCallback);
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        mediaRouter.removeCallback(mediaRouterCallback);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
        MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider) MenuItemCompat
                .getActionProvider(mediaRouteMenuItem);
        mediaRouteActionProvider.setRouteSelector(mediaRouteSelector);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private boolean isMirroring() {
        MediaRouter.RouteInfo route = mediaRouter.getSelectedRoute();
        castDisplay = route.getPresentationDisplay();

        return castDisplay != null;
    }

    public void connectToReceiverApplication(CastDevice castDevice, Cast.Listener listener) {
        Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions.builder(castDevice, listener);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Cast.API, apiOptionsBuilder.build())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        googleApiClient.connect();
    }

    public void disconnectFromReceiverApplication() {
        if(googleApiClient != null) {
            googleApiClient.disconnect();
            googleApiClient = null;
        }
    }

    private void sendMessage(String message) {
        try {
            Cast.CastApi.sendMessage(googleApiClient, NAMESPACE, message)
                    .setResultCallback(
                            new ResultCallback<Status>() {
                                @Override
                                public void onResult(Status status) {
                                    if (!status.isSuccess()) {
                                        Log.e(TAG, "Sending message failed");
                                    }
                                }
                            }
                    );
        } catch (Exception e) {
            Log.e(TAG, "Exception while sending message", e);
        }
    }

    private void launchReceiverApplication() {
        try {

            Cast.CastApi.launchApplication(googleApiClient, getString(R.string.cast_app_id, false))
                    .setResultCallback(new ResultCallback<Cast.ApplicationConnectionResult>() {
                        @Override
                        public void onResult(Cast.ApplicationConnectionResult result) {
                            try {
                                Cast.CastApi.setMessageReceivedCallbacks(googleApiClient,
                                        NAMESPACE,
                                        incomingMsgHandler);
                            } catch (IOException e) {
                                Log.e(TAG, "Exception while creating channel", e);
                            }
                        }
                    });
        } catch (Exception e){
            Log.e(TAG, "Failed to launch application", e);
        }
    }

    private void stopReceiverApplication() {
        Cast.CastApi.stopApplication(googleApiClient);
    }

    public final Cast.MessageReceivedCallback incomingMsgHandler = new Cast.MessageReceivedCallback() {
        @Override
        public void onMessageReceived(CastDevice castDevice, String namespace, String message) {
        }
    };

    @Override
    public void onConnected(Bundle bundle) {
        launchReceiverApplication();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Failed to connect: " + connectionResult.getErrorCode());
    }
}
