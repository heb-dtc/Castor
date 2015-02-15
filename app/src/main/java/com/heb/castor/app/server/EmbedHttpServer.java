package com.heb.castor.app.server;

import android.os.Environment;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class EmbedHttpServer extends NanoHTTPD {
    private static final int PORT = 8080;
    private static final String TAG = EmbedHttpServer.class.getName();

    private static final String MUSIC_ENDPOINT = "/music";
    private static final String IMGAE_ENDPOINT = "/image";
    private static final String VIDEO_ENDPOINT = "/video";
    private static final String PDF_ENDPOINT = "/file";

    public EmbedHttpServer() {
        super(PORT);
    }

    public EmbedHttpServer(String hostname, int port) {
        super(hostname, port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();
        Log.e(TAG, method + " '" + uri + "' ");

        if (uri.equals(MUSIC_ENDPOINT)) {
            return getMusicStream();
        } else if (uri.equals(IMGAE_ENDPOINT)) {
            return getImage();
        } else if (uri.equals(VIDEO_ENDPOINT)) {
            return getVideoStream();
        } else if (uri.equals(PDF_ENDPOINT)) {
            return getFile();
        }
        return generateIndexHtml();
    }

    private Response getImage() {
        return getFullResponse("image/png", "/Download/floppies0005.png");
    }

    private Response getMusicStream() {
        return getFullResponse("audio/mpeg", "/music/mc_soraal.mp3");
    }

    private Response getVideoStream() {
        return getFullResponse("video/mp4", "/Download/VID_20131207_195120.mp4");
    }

    private Response getFile() {
        return getFullResponse("application/pdf", "/Download/slides.pdf");
    }

    private Response getFullResponse(String mimeType, String source) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(Environment.getExternalStorageDirectory()
                    + source );
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new NanoHTTPD.Response(Response.Status.OK, mimeType, fis);
    }

    private Response generateIndexHtml() {
        String msg = "<html><body><h1>Local server</h1>\n";
        msg += "</body></html>\n";
        return new NanoHTTPD.Response(msg);
    }
}
