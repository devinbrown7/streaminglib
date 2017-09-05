package com.devinbrown.streaminglib.rtsp;

import android.media.MediaFormat;

import com.devinbrown.streaminglib.rtp.RtpClientStream;

public class RtspClientEvent {
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

    static class SendRequest {
        RtspRequest rtspRequest;
        RtpClientStream stream;

        SendRequest(RtspRequest r) {
            rtspRequest = r;
        }

        SendRequest(RtspRequest r, RtpClientStream s) {
            rtspRequest = r;
            stream = s;
        }
    }

    static class SendResponse {
        RtspRequest rtspRequest;
        RtpClientStream stream;

        SendResponse(RtspRequest r) {
            rtspRequest = r;
        }

        SendResponse(RtspRequest r, RtpClientStream s) {
            rtspRequest = r;
            stream = s;
        }
    }

    static class ReceivedRequest {
        RtpClientStream stream;

        ReceivedRequest() {
        }

        ReceivedRequest(RtpClientStream s) {
            stream = s;
        }
    }

    static class ReceivedResponse {
        RtspRequest rtspRequest;
        RtspResponse rtspResponse;
        RtpClientStream stream;

        ReceivedResponse(RtspRequest req, RtspResponse res) {
            rtspRequest = req;
            rtspResponse = res;
        }

        ReceivedResponse(RtspRequest req, RtspResponse res, RtpClientStream s) {
            rtspRequest = req;
            rtspResponse = res;
            stream = s;
        }
    }

    private static class SessionConfigured {
        RtspClient.Session session;

        SessionConfigured(RtspClient.Session s) {
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
        RtspClient.Session session;

        UpdatedMethods(RtspClient.Session s) {
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
