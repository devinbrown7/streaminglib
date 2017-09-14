package com.devinbrown.streaminglib;

import android.net.Uri;

import com.devinbrown.streaminglib.media.RtpMedia;
import com.devinbrown.streaminglib.rtp.RtpClientStream;
import com.devinbrown.streaminglib.rtp.RtpStream;
import com.devinbrown.streaminglib.rtsp.RtspAuth;

import org.greenrobot.eventbus.EventBus;

public class RtspClientStreamEvent {

    public static class ConnectionError {
        public java.lang.Exception exception;

        public ConnectionError(java.lang.Exception e) {
            exception = e;
        }
    }

    public static class Exception {
        public java.lang.Exception exception;

        public Exception(java.lang.Exception e) {
            exception = e;
        }
    }

    // Request Events

    // Connect->OPTIONS->DESCRIBE Request
    public static class ConnectionRequest {
        public Uri uri;
        public EventBus eventBus;
        public RtspAuth.AuthParams auth;

        public ConnectionRequest(Uri u, EventBus e, RtspAuth.AuthParams a) {
            uri = u;
            eventBus = e;
            auth = a;
        }
    }

    // PAUSE Request
    public static class PauseStreamRequest {
        public RtpClientStream stream;

        public PauseStreamRequest(RtpClientStream s) {
            stream = s;
        }
    }

    // PLAY Request
    public static class PlayStreamRequest {
        public RtpClientStream stream;

        public PlayStreamRequest(RtpClientStream s) {
            stream = s;
        }
    }

    // SETUP Request
    public static class SetupStreamRequest {
        public RtpMedia media;
        public RtpStream.RtpProtocol rtpProtocol;

        public SetupStreamRequest(RtpMedia m, RtpStream.RtpProtocol p) {
            media = m;
            rtpProtocol = p;
        }
    }

    // TEARDOWN Request
    public static class StopStreamRequest {
        public RtpClientStream stream;

        public StopStreamRequest(RtpClientStream s) {
            stream = s;
        }
    }

    // Response Events

    // Connect->OPTIONS->DESCRIBE Response
    public static class ConnectionResponse {
        public RtpMedia[] media;

        public ConnectionResponse(RtpMedia[] m) {
            media = m;
        }
    }

    // SETUP Response
    public static class SetupStreamResponse {
        public RtpClientStream stream;

        public SetupStreamResponse(RtpClientStream s) {
            stream = s;
        }
    }

    // PLAY Response
    public static class PlayStreamResponse {
        public RtpClientStream stream;

        public PlayStreamResponse(RtpClientStream s) {
            stream = s;
        }
    }

    // PAUSE Response
    public static class PauseStreamResponse {
        public RtpClientStream stream;

        public PauseStreamResponse(RtpClientStream s) {
            stream = s;
        }
    }

    // TEARDOWN Response
    public static class StopStreamResponse {
        public RtpClientStream stream;

        public StopStreamResponse(RtpClientStream s) {
            stream = s;
        }
    }

    public static class StreamNotFound {
        public StreamNotFound() {
        }
    }

    public static class MediaDataReceived {
        byte[] data;
        public MediaDataReceived(byte[] d) {
            data = d;
        }
    }
}
