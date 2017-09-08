package com.devinbrown.streaminglib.rtsp;

import android.media.MediaFormat;

import com.devinbrown.streaminglib.rtp.RtpStream;

public class RtspSessionEvent {
    static class SessionConnected {
        RtspSession session;

        SessionConnected(RtspSession s) {
            session = s;
        }
    }

    static class ConnectionError {
        Exception error;

        ConnectionError(Exception e) {
            error = e;
        }
    }

    static class SendRequest {
        RtspRequest rtspRequest;
        RtpStream stream;

        SendRequest(RtspRequest r) {
            rtspRequest = r;
        }

        SendRequest(RtspRequest r, RtpStream s) {
            rtspRequest = r;
            stream = s;
        }
    }

    static class SendResponse {
        RtspRequest rtspRequest;
        RtspResponse rtspResponse;
        RtpStream stream;

        SendResponse(RtspRequest req, RtspResponse res) {
            rtspRequest = req;
            rtspResponse = res;
        }

        SendResponse(RtspRequest req, RtspResponse res, RtpStream s) {
            rtspRequest = req;
            rtspResponse = res;
            stream = s;
        }
    }

    static class ReceivedRequest {
        RtspRequest rtspRequest;

        ReceivedRequest(RtspRequest r) {
            rtspRequest = r;
        }
    }

    static class ReceivedResponse {
        RtspRequest rtspRequest;
        RtspResponse rtspResponse;
        RtpStream stream;

        ReceivedResponse(RtspRequest req, RtspResponse res) {
            rtspRequest = req;
            rtspResponse = res;
        }

        ReceivedResponse(RtspRequest req, RtspResponse res, RtpStream s) {
            rtspRequest = req;
            rtspResponse = res;
            stream = s;
        }
    }

    private static class SessionConfigured {
        RtspClientSession session;

        SessionConfigured(RtspClientSession s) {
            session = s;
        }
    }

    private static class StreamAvailable {
        MediaFormat[] formats;

        StreamAvailable(MediaFormat[] f) {
            formats = f;
        }
    }

    static class UpdatedMethods {
        RtspSession session;

        UpdatedMethods(RtspSession s) {
            session = s;
        }
    }

    private static class Setup {
        Setup() {

        }
    }

    public static class RtpPacketReceived {
        public byte[] data;
        public RtpPacketReceived(byte[] d) {
            data = d;
        }
    }

    public static class RtcpPacketReceived {
        public byte[] data;
        public RtcpPacketReceived(byte[] d) {
            data = d;
        }
    }
}
