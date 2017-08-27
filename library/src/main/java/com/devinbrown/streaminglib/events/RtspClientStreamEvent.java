package com.devinbrown.streaminglib.events;

import android.media.MediaFormat;

import org.greenrobot.eventbus.EventBus;

import java.net.URI;

public class RtspClientStreamEvent {
    public static class ConnectionRequest {
        public URI uri;
        public EventBus eventBus;

        public ConnectionRequest(URI u, EventBus e) {
            uri = u;
            eventBus = e;
        }
    }

    public static class PauseStreamRequest {
    }

    public static class PlayStreamRequest {
    }

    public static class SetupStreamRequest {
        MediaFormat mediaFormat;

        public SetupStreamRequest(MediaFormat mediaFormat) {
            this.mediaFormat = mediaFormat;
        }
    }

    public static class StopStreamRequest {
    }
}
