package com.heb.castor.app.server;

import fi.iki.elonen.NanoHTTPD;

public class EmbedHttpServer extends NanoHTTPD {

    public EmbedHttpServer(int port) {
        super(port);
    }

    public EmbedHttpServer(String hostname, int port) {
        super(hostname, port);
    }
}
