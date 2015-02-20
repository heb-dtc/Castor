package com.heb.castor.app.server;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Random;

public class EmbedHttpServer extends NanoHTTPD {
    private static final int PORT = 8080;
    private static final String TAG = EmbedHttpServer.class.getName();

    private static final String MUSIC_ENDPOINT = "/music";
    private static final String IMGAE_ENDPOINT = "/image";
    private static final String VIDEO_ENDPOINT = "/video";
    private static final String PDF_ENDPOINT = "/file";
    private static final String STREAM_ENDPOINT = "/stream";

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
            return serveMusicStream();
        } else if (uri.equals(IMGAE_ENDPOINT)) {
            return serveImage();
        } else if (uri.equals(VIDEO_ENDPOINT)) {
            return serveVideoStream();
        } else if (uri.equals(PDF_ENDPOINT)) {
            return serveFile();
        } else if (uri.equals(STREAM_ENDPOINT)) {
            return serveStream();
        }
        return generateIndexHtml();
    }

    private Response serveImage() {
        return getFullResponse("image/png", "/Download/floppies0005.png");
    }

    private Response serveMusicStream() {
        return getFullResponse("audio/mpeg", "/music/mc_soraal.mp3");
    }

    private Response serveVideoStream() {
        return getFullResponse("video/mp4", "/Download/VID_20131207_195120.mp4");
    }

    private Response serveFile() {
        return getFullResponse("application/pdf", "/Download/slides.pdf");
    }

    private Response serveStream() {
        return getFullResponse("application/pdf", "/Download/slides.pdf");
    }

    private Response getFullResponse(String mimeType, String source) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(Environment.getExternalStorageDirectory()
                    + source);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new NanoHTTPD.Response(Response.Status.OK, mimeType, fis);
    }

    private Response getStreamResponse(String mimeType, String source) {
        File file = new File(source);
        long fileLength = file.length();

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(Environment.getExternalStorageDirectory()
                    + source);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Random rnd = new Random();
        String etag = Integer.toHexString(rnd.nextInt());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        Response response = new Response(Response.Status.PARTIAL_CONTENT, mimeType, fis);
        response.addHeader("ETag", etag);
        response.addHeader("Content-Length", fileLength + "");
        response.addHeader("Content-Range", "bytes " + 0 + "-" + fileLength + "/" + fileLength);
        response.setChunkedTransfer(true);

        return response;
    }

    private Response generateIndexHtml() {
        String msg = "<html><body><h1>Local server</h1>\n";
        msg += "</body></html>\n";
        return new NanoHTTPD.Response(msg);
    }
}
