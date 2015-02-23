package com.heb.castor.app.server;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Random;

public class EmbedHttpServer extends NanoHTTPD {
    private static final int PORT = 8080;
    private static final String TAG = EmbedHttpServer.class.getName();

    private static final String MUSIC_ENDPOINT = "/music";
    private static final String IMGAE_ENDPOINT = "/image";
    private static final String VIDEO_ENDPOINT = "/video";
    private static final String PDF_ENDPOINT = "/file";
    private static final String STREAM_ENDPOINT = "/stream";

    private static final String RANGE_VALUE_SEPARATOR = "-";
    private static final String RANGE_PREFIX = "bytes=";

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

        String range = null;
        Log.d(TAG, "Request headers:");

        Map<String, String> headers = session.getHeaders();
        for (String key : headers.keySet()) {
            Log.d(TAG, "  " + key + ":" + headers.get(key));
            if ("range".equals(key)) {
                range = headers.get(key);
            }
        }

        //TODO:extract, serve static content
        if (range == null) {
            if (uri.equals(MUSIC_ENDPOINT)) {
                return serveMusicStream();
            } else if (uri.equals(IMGAE_ENDPOINT)) {
                return serveImage();
            } else if (uri.equals(VIDEO_ENDPOINT)) {
                return serveVideoStream();
            } else if (uri.equals(PDF_ENDPOINT)) {
                return serveFile();
            } else {
                //TODO: better error handling pliz
                return generateIndexHtml();
            }
        }

        //TODO: extract, serve streaming content
        if (uri.equals(MUSIC_ENDPOINT)) {
            return getStreamResponse("audio/mpeg", "/music/mc_soraal.mp3", range);
        } else if (uri.equals(VIDEO_ENDPOINT)) {
            return getStreamResponse("video/mp4", "/Download/VID_20131207_195120.mp4", range);
        } else {
            //TODO: better error handling pliz
            return generateIndexHtml();
        }
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

    private Response getStreamResponse(String mimeType, String source, String rangeHeader) {
        String localFilePath = Environment.getExternalStorageDirectory() + source;
        File file = new File(localFilePath);
        long fileLength = file.length();
        long start;
        long end;
        long size = fileLength - 1;

        //range header is like "-258"
        if (rangeHeader.startsWith(RANGE_VALUE_SEPARATOR)) {
            long rangeValue = Long.parseLong(rangeHeader.substring(RANGE_VALUE_SEPARATOR.length()));
            end = size;
            start = size - rangeValue;
        } else { //range header is like "bytes=145-586" or "bytes=0-"
            String rangeValues = rangeHeader.substring(RANGE_PREFIX.length(), rangeHeader.length());
            String[] values = rangeValues.split(RANGE_VALUE_SEPARATOR);
            start = Long.parseLong(values[0]);

            if (values.length > 1) {
                end = Long.parseLong(values[1]);
            } else {
                end = size/4;
            }
        }

        //sanity check
        if (end > fileLength - 1) {
            end = fileLength - 1;
        }

        if (start <= end) {
            long contentLength = end - start + 1;

            FileInputStream fis = null;
            try {
                fis = new FileInputStream(localFilePath);
                fis.skip(start);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

            Random rnd = new Random();
            String etag = Integer.toHexString(rnd.nextInt());

            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            Response response = new Response(Response.Status.PARTIAL_CONTENT, mimeType, fis);
            response.addHeader("ETag", etag);
            response.addHeader("Content-Length", contentLength + "");
            response.addHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
            response.addHeader("Content-Type", mimeType);
            response.setChunkedTransfer(true);

            return response;
        } else {
            return new Response(Response.Status.RANGE_NOT_SATISFIABLE, mimeType, rangeHeader);
        }
    }

    private Response generateIndexHtml() {
        String msg = "<html><body><h1>Local server</h1>\n";
        msg += "</body></html>\n";
        return new NanoHTTPD.Response(msg);
    }
}
