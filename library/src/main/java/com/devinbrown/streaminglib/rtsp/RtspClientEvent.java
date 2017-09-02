package com.devinbrown.streaminglib.rtsp;

import android.media.MediaFormat;

import com.devinbrown.streaminglib.rtp.RtpClientStream;


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
        RtpClientStream stream;

        Request(RtspRequest r) {
            rtspRequest = r;
        }

        Request(RtspRequest r, RtpClientStream s) {
            rtspRequest = r;
            stream = s;
        }
    }

    static class Response {
        RtspRequest rtspRequest;
        RtspResponse rtspResponse;
        RtpClientStream stream;

        Response(RtspRequest req, RtspResponse res) {
            rtspRequest = req;
            rtspResponse = res;
        }

        Response(RtspRequest req, RtspResponse res, RtpClientStream s) {
            rtspRequest = req;
            rtspResponse = res;
            stream = s;
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
