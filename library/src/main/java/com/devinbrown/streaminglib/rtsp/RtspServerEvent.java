package com.devinbrown.streaminglib.rtsp;

import java.net.Socket;

class RtspServerEvent {
    static class Connection {
        Socket socket;

        Connection(Socket s) {
            socket = s;
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
