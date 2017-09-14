package com.devinbrown.streaminglib.rtsp;

import java.net.Socket;

class RtspServerEvent {
    static class Connection {
        RtspServer server;
        Socket socket;

        Connection(RtspServer server, Socket socket) {
            this.server = server;
            this.socket = socket;
        }
    }

    static class Request {
        String message;

        public Request(String m) {
            message = m;
        }
    }

    static class Response {
        String message;

        Response(String m) {
            message = m;
        }
    }
}
