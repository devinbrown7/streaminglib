package com.devinbrown.streaminglib;

import android.media.MediaFormat;

import com.devinbrown.streaminglib.rtp.RtpClientStream;
import com.devinbrown.streaminglib.rtp.RtpStream;

import org.greenrobot.eventbus.EventBus;

import java.net.URI;

public class RtspClientStreamEvent {

    public static class ConnectionError {
        public Exception exception;

        public ConnectionError(Exception e) {
            exception = e;
        }
    }

    // Request Events

    // Connect->OPTIONS->DESCRIBE Request
    public static class ConnectionRequest {
        public URI uri;
        public EventBus eventBus;

        public ConnectionRequest(URI u, EventBus e) {
            uri = u;
            eventBus = e;
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
        public MediaFormat format;
        public RtpStream.RtpProtocol rtpProtocol;

        public SetupStreamRequest(MediaFormat f, RtpStream.RtpProtocol p) {
            format = f;
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
        public MediaFormat[] formats;

        public ConnectionResponse(MediaFormat[] f) {
            formats = f;
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
}
