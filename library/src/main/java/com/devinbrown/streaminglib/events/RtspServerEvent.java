package com.devinbrown.streaminglib.events;

import java.net.Socket;

public class RtspServerEvent {
    public static class Connection {
        public Socket socket;

        public Connection(Socket s) {
            socket = s;
        }
    }

    public static class Request {
        public String message;

        public Request(String m) {
            message = m;
        }
    }

    public static class Response {
        public String message;

        public Response(String m) {
            message = m;
        }
    }
}
