package com.heb.castor.app.cast;

import android.util.Log;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.io.IOException;

public class Castor {

    private static final String TAG = "Castor";

    private static final String CAST_PLAYER_NAMESPACE = "urn:x-cast:com.google.cast.media";

    private RemoteMediaPlayer castPlayer;
    private CastorChannel castorChannel;
    private GoogleApiClient apiClient;

    private String appId;

    public Castor(String appId, GoogleApiClient apiClient) {
        this.appId = appId;
        this.apiClient = apiClient;
    }

    public String getAppId() {
        return appId;
    }

    public String getNamespace() {
        return castorChannel.getNamespace();
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

    public void launchApplicationOnCastDevice() {
        try {
            Cast.CastApi.launchApplication(apiClient, appId, false)
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
                                    Cast.CastApi.setMessageReceivedCallbacks(apiClient,
                                            castPlayer.getNamespace(),
                                            castPlayer);
                                } catch (IOException e) {
                                    Log.e(TAG, "Exception while creating channel", e);
                                }

                                castPlayer.requestStatus(apiClient)
                                        .setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                                            @Override
                                            public void onResult(RemoteMediaPlayer.MediaChannelResult mediaChannelResult) {
                                                if (!mediaChannelResult.getStatus().isSuccess()) {
                                                    Log.e(TAG, "Failed to request status.");
                                                }
                                            }
                                        });
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch application", e);
        }
    }

    public void stopApplicationOnCastDevice() {
        Cast.CastApi.stopApplication(apiClient);
    }

    public void castMediaToDevice(String dataTitle, String contentType, String dataUrl, int type) {
        MediaMetadata mediaMetadata = new MediaMetadata(type);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, dataTitle);

        MediaInfo mediaInfo = new MediaInfo.Builder(
                dataUrl)
                .setContentType(contentType)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mediaMetadata)
                .build();

        try {
            castPlayer.load(apiClient, mediaInfo, true)
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

    public void sendPlayToCastDevice() {
        castPlayer.play(apiClient);
    }

    public void sendStopToCastDevice() {
        castPlayer.stop(apiClient);
    }

    public void sendPauseToCastDevice() {
        castPlayer.pause(apiClient);
    }

    public void sendMessageToCastDevice(String message) {
        try {
            Cast.CastApi.sendMessage(apiClient, CAST_PLAYER_NAMESPACE, message)
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
}
