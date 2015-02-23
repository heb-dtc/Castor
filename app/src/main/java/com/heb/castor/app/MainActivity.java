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
import android.widget.Toast;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.heb.castor.app.cast.CastorCastClientListener;
import com.heb.castor.app.cast.CastorChannel;
import com.heb.castor.app.cast.CastorMediaRouterCallback;
import com.heb.castor.app.server.EmbedHttpServer;

import java.io.IOException;

public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String CAST_PLAYER_NAMESPACE = "urn:x-cast:com.google.cast.media";

    private MediaRouter mediaRouter;
    private MediaRouteSelector mediaRouteSelector;
    private CastorMediaRouterCallback mediaRouterCallback;

    private RemoteMediaPlayer castPlayer;
    private CastorChannel castorChannel;

    private GoogleApiClient googleApiClient;

    private EmbedHttpServer httpServer;

    private String appId;

    private Button launchCastorApplicationButton;
    private Button launchCastPlayerApplicationButton;
    private Button doConnectButton;
    private Button sendMessageButton;
    private Button startServerButton;
    private Button stopServerButton;
    private Button castImageButton;
    private Button castVideoButton;
    private Button castFileButton;
    private Button castMusicButton;
    private TextView ipTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        launchCastPlayerApplicationButton = (Button) findViewById(R.id.launchCastPlayerApplicationButton);
        launchCastorApplicationButton = (Button) findViewById(R.id.launchCastorApplicationButton);
        doConnectButton = (Button) findViewById(R.id.doConnectButton);

        sendMessageButton = (Button) findViewById(R.id.sendMessageButton);
        castImageButton = (Button) findViewById(R.id.castImageButton);
        castVideoButton = (Button) findViewById(R.id.castVideoButton);
        castFileButton = (Button) findViewById(R.id.castFileButton);
        castMusicButton = (Button) findViewById(R.id.castMusicButton);

        startServerButton = (Button) findViewById(R.id.startServerButton);
        stopServerButton = (Button) findViewById(R.id.stopServerButton);
        ipTextView = (TextView) findViewById(R.id.ipadressView);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: do nothing for now
            }
        });

        doConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToReceiverApplication(mediaRouterCallback.getCastDevice(), new CastorCastClientListener());
            }
        });

        launchCastorApplicationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCastorReceiverApplication();
            }
        });

        launchCastPlayerApplicationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCastPlayerApplication();
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

        castImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                castImage();
            }
        });

        castVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                castVideo();
            }
        });

        castFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                castFile();
            }
        });

        castMusicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                castMusic();
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

    private void castImage() {
        String name = "floppies0005.png";
        String mimeType = "image/png";
        String url = "http://" + ipTextView.getText().toString() + ":8080/image";
        int type = MediaMetadata.MEDIA_TYPE_PHOTO;

        sendDataToCastPlayer(name, mimeType, url, type);
    }

    private void castVideo() {
        String name = "VID_20131207_195120.mp4";
        String mimeType = "video/mp4";
        String url = "http://" + ipTextView.getText().toString() + ":8080/video";
        int type = MediaMetadata.MEDIA_TYPE_MOVIE;

        sendDataToCastPlayer(name, mimeType, url, type);
    }

    private void castFile() {
        String name = "slides.pdf";
        String mimeType = "application/pdf";
        String url = "http://" + ipTextView.getText().toString() + ":8080/file";
        int type = MediaMetadata.MEDIA_TYPE_GENERIC;

        sendDataToCastPlayer(name, mimeType, url, type);
    }

    private void castMusic() {
        String name = "mc_soraal.mp3";
        String mimeType = "audio/mpeg";
        String url = "http://" + ipTextView.getText().toString() + ":8080/music";
        int type = MediaMetadata.MEDIA_TYPE_MUSIC_TRACK;

        sendDataToCastPlayer(name, mimeType, url, type);
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
        if (isFinishing()) {
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
        if (googleApiClient != null) {
            googleApiClient.disconnect();
            googleApiClient = null;
        }
    }

    public void playItem(final MediaInfo mediaInfo) {
        if (mediaInfo == null) {
            return;
        }
    }

    private void sendMessageToCastPlayer(String message) {
        try {
            Cast.CastApi.sendMessage(googleApiClient, CAST_PLAYER_NAMESPACE, message)
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

    private void sendMessageToCastorReceiver(String message) {
        try {
            Cast.CastApi.sendMessage(googleApiClient, castorChannel.getNamespace(), message)
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

    private void launchCastorReceiverApplication() {
        try {
            Cast.CastApi.launchApplication(googleApiClient, appId, false)
                    .setResultCallback(new ResultCallback<Cast.ApplicationConnectionResult>() {
                        @Override
                        public void onResult(Cast.ApplicationConnectionResult result) {
                            Status status = result.getStatus();
                            if (status.isSuccess()) {
                                ApplicationMetadata applicationMetadata =
                                        result.getApplicationMetadata();
                                String sessionId = result.getSessionId();
                                String applicationStatus = result.getApplicationStatus();
                                boolean wasLaunched = result.getWasLaunched();

                                castorChannel = new CastorChannel();

                                try {
                                    //TODO: check result to confirm connection
                                    Cast.CastApi.setMessageReceivedCallbacks(googleApiClient,
                                            castorChannel.getNamespace(),
                                            incomingMsgHandler);
                                } catch (IOException e) {
                                    Log.e(TAG, "Exception while creating channel", e);
                                }


                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch application", e);
        }
    }

    private void launchCastPlayerApplication() {
        try {
            Cast.CastApi.launchApplication(googleApiClient, appId, false)
                    .setResultCallback(new ResultCallback<Cast.ApplicationConnectionResult>() {
                        @Override
                        public void onResult(Cast.ApplicationConnectionResult result) {
                            Status status = result.getStatus();
                            if (status.isSuccess()) {
                                ApplicationMetadata applicationMetadata = result.getApplicationMetadata();
                                String sessionId = result.getSessionId();
                                String applicationStatus = result.getApplicationStatus();
                                boolean wasLaunched = result.getWasLaunched();

                                createCastPlayer();

                                try {
                                    //TODO: check result to confirm connection
                                    Cast.CastApi.setMessageReceivedCallbacks(googleApiClient,
                                            castPlayer.getNamespace(),
                                            castPlayer);
                                } catch (IOException e) {
                                    Log.e(TAG, "Exception while creating channel", e);
                                }

                                castPlayer.requestStatus(googleApiClient)
                                        .setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                                            @Override
                                            public void onResult(RemoteMediaPlayer.MediaChannelResult mediaChannelResult) {
                                                if (!mediaChannelResult.getStatus().isSuccess()) {
                                                    Log.e(TAG, "Failed to request status.");
                                                }

                                                Toast.makeText(getApplicationContext(), "App Launch, status read",
                                                        Toast.LENGTH_SHORT).show();;
                                            }
                                        });
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch application", e);
        }
    }

    private void createCastPlayer() {
        castPlayer = new RemoteMediaPlayer();

        castPlayer.setOnMetadataUpdatedListener(new RemoteMediaPlayer.OnMetadataUpdatedListener() {
            @Override
            public void onMetadataUpdated() {
                MediaInfo mediaInfo = castPlayer.getMediaInfo();

                if (mediaInfo != null) {
                    MediaMetadata metadata = mediaInfo.getMetadata();
                    //TODO: do stuff
                }
            }
        });

        castPlayer.setOnStatusUpdatedListener(new RemoteMediaPlayer.OnStatusUpdatedListener() {
            @Override
            public void onStatusUpdated() {
                MediaStatus mediaStatus = castPlayer.getMediaStatus();

                if (mediaStatus != null) {
                    boolean isPlaying = mediaStatus.getPlayerState() == MediaStatus.PLAYER_STATE_PLAYING;
                    //TODO: do stuff
                }
            }
        });
    }

    private void sendDataToCastPlayer(String dataTitle, String contentType, String dataUrl, int type) {
        MediaMetadata mediaMetadata = new MediaMetadata(type);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, dataTitle);

        MediaInfo mediaInfo = new MediaInfo.Builder(
                dataUrl)
                .setContentType(contentType)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mediaMetadata)
                .build();

        try {
            castPlayer.load(googleApiClient, mediaInfo, true)
                    .setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                        @Override
                        public void onResult(RemoteMediaPlayer.MediaChannelResult result) {
                            if (result.getStatus().isSuccess()) {
                                Log.d(TAG, "Media loaded successfully");
                            }
                        }
                    });
        } catch (IllegalStateException e) {
            Log.e(TAG, "Problem occurred with media during loading", e);
        } catch (Exception e) {
            Log.e(TAG, "Problem opening media during loading", e);
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

        Toast.makeText(getApplicationContext(), "Connection failed", Toast.LENGTH_SHORT).show();
    }

    // GoogleApiClient.ConnectionCallbacks.onConnected
    @Override
    public void onConnected(Bundle bundle) {
        //TODO: no auto launch for now
        //launchCastPlayerApplication();
        Toast.makeText(getApplicationContext(), "Connection successful", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "onConnectionSuspended ");
    }
}
