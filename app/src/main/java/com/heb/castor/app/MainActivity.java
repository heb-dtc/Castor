package com.heb.castor.app;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
import com.heb.castor.app.server.EmbedHttpServer;

import java.io.IOException;

public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String NAMESPACE = "urn:x-cast:com.heb.castor.app";
    private static final String MEDIA_NAMESPACE = "urn:x-cast:com.google.cast.media";

    private MediaRouter mediaRouter;
    private MediaRouteSelector mediaRouteSelector;
    private CastorMediaRouterCallback mediaRouterCallback;

    private GoogleApiClient googleApiClient;

    private EmbedHttpServer httpServer;

    private String appId;

    private Button launchApplicationButton;
    private Button doConnectButton;
    private Button sendMessageButton;
    private Button startServerButton;
    private Button stopServerButton;
    private TextView ipTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        launchApplicationButton = (Button) findViewById(R.id.launchApplicationButton);
        doConnectButton = (Button) findViewById(R.id.doConnectButton);
        sendMessageButton = (Button) findViewById(R.id.sendMessageButton);
        startServerButton = (Button) findViewById(R.id.startServerButton);
        stopServerButton = (Button) findViewById(R.id.stopServerButton);
        ipTextView = (TextView) findViewById(R.id.ipadressView);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage("Salut Salut!");
            }
        });

        doConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToReceiverApplication(mediaRouterCallback.getCastDevice(), new CastorCastClientListener());
            }
        });

        launchApplicationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchReceiverApplication();
            }
        });

        startServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLocalServer();
            }
        });

        stopServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopLocalServer();
            }
        });

        //appId = getString(R.string.cast_app_id);
        appId = CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID;

        mediaRouter = MediaRouter.getInstance(getApplicationContext());
        mediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(CastMediaControlIntent.categoryForCast(appId))
                .build();
        mediaRouterCallback = new CastorMediaRouterCallback(getApplicationContext());

    }

    private void stopLocalServer() {
        startServerButton.setEnabled(true);
        stopServerButton.setEnabled(false);

        httpServer.stop();
    }

    private void startLocalServer() {
        httpServer = new EmbedHttpServer();
        try {
            httpServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stopServerButton.setEnabled(true);
        startServerButton.setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ipTextView.setText(getLocalIp());

        mediaRouter.addCallback(mediaRouteSelector, mediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
    }

    String getLocalIp() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        final String formatedIpAddress = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        return formatedIpAddress;
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

        if (httpServer != null) {
            httpServer.stop();
        }

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

    public void playItem(final MediaInfo mediaInfo) {
        if(mediaInfo == null) {
            return;
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
            Cast.CastApi.launchApplication(googleApiClient, appId, false)
                    .setResultCallback(new ResultCallback<Cast.ApplicationConnectionResult>() {
                        @Override
                        public void onResult(Cast.ApplicationConnectionResult result) {
                            try {
                                //TODO: check result to confirm connection
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
            Log.e(TAG, "Message reveived from cast application: " + message);
        }
    };

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Failed to connect: " + connectionResult.getErrorCode());
    }

    // GoogleApiClient.ConnectionCallbacks.onConnected
    @Override
    public void onConnected(Bundle bundle) {
        launchReceiverApplication();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "onConnectionSuspended ");
    }
}
