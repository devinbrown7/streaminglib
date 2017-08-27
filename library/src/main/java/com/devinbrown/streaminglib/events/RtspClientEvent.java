package com.devinbrown.streaminglib.events;

import android.media.MediaFormat;

import com.devinbrown.streaminglib.rtsp.RtspRequest;
import com.devinbrown.streaminglib.rtsp.RtspResponse;


public class RtspClientEvent {
    public static class Connected {
    }

    public static class ConnectionError {
        public Exception error;

        public ConnectionError(Exception e) {
            error = e;
        }
    }

    public static class Request {
        public RtspRequest rtspRequest;

        public Request(RtspRequest r) {
            rtspRequest = r;
        }
    }

    public static class Response {
        public RtspRequest rtspRequest;
        public RtspResponse rtspResponse;

        public Response(RtspRequest req, RtspResponse res) {
            rtspRequest = req;
            rtspResponse = res;
        }
    }

    public static class StreamAvailable {
        public MediaFormat[] formats;

        public StreamAvailable(MediaFormat[] f) {
            formats = f;
        }
    }

    public static class StreamNotFound {
    }

    public static class UpdatedMethods {
    }
}
