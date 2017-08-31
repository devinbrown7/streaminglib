package com.devinbrown.streaminglib.rtsp;

import android.media.MediaFormat;


class RtspClientEvent {
    static class SessionConnected {
        RtspClient.Session session;

        SessionConnected(RtspClient.Session s) {
            session = s;
        }
    }

    static class ConnectionError {
        Exception error;

        ConnectionError(Exception e) {
            error = e;
        }
    }

    static class Request {
        RtspRequest rtspRequest;

        Request(RtspRequest r) {
            rtspRequest = r;
        }
    }

    static class Response {
        RtspRequest rtspRequest;
        RtspResponse rtspResponse;

        Response(RtspRequest req, RtspResponse res) {
            rtspRequest = req;
            rtspResponse = res;
        }
    }

    static class SessionConfigured {
        RtspClient.Session session;

        SessionConfigured(RtspClient.Session s) {
            session = s;
        }
    }

    static class StreamAvailable {
        MediaFormat[] formats;

        StreamAvailable(MediaFormat[] f) {
            formats = f;
        }
    }

    static class StreamNotFound {
        RtspClient.Session session;

        StreamNotFound(RtspClient.Session s) {
            session = s;
        }
    }

    static class UpdatedMethods {
        RtspClient.Session session;

        UpdatedMethods(RtspClient.Session s) {
            session = s;
        }
    }

    static class Setup {
        Setup() {

        }
    }
}
