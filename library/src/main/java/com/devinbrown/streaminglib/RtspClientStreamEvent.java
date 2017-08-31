package com.devinbrown.streaminglib;

import android.media.MediaFormat;

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
        public PauseStreamRequest() {
        }
    }

    // PLAY Request
    public static class PlayStreamRequest {
        public PlayStreamRequest() {
        }
    }

    // SETUP Request
    public static class SetupStreamRequest {
        public MediaFormat format;

        public SetupStreamRequest(MediaFormat f) {
            format = f;
        }
    }

    // TEARDOWN Request
    public static class StopStreamRequest {
        public StopStreamRequest() {
        }
    }

    // Response Events

    // Connect->OPTIONS->DESCRIBE Request
    public static class ConnectionResponse {
        public MediaFormat[] formats;

        public ConnectionResponse(MediaFormat[] f) {
            formats = f;
        }
    }

    // SETUP Request
    public static class SetupStreamResponse {

        public SetupStreamResponse() {
        }
    }

    // TEARDOWN Request
    public static class StopStreamResponse {
        public StopStreamResponse() {
        }
    }
}
