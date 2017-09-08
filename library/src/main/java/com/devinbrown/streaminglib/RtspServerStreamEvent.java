package com.devinbrown.streaminglib;

import com.devinbrown.streaminglib.rtsp.RtspServerInputStream;

/**
 * Created by devinbrown on 9/8/17.
 */

public class RtspServerStreamEvent {

    public static class StreamAvailable {
        private RtspServerInputStream rtspServerInputStream;

        public StreamAvailable(RtspServerInputStream is) {
            rtspServerInputStream = is;
        }

        public RtspServerInputStream getRtspServerInputStream() {
            return rtspServerInputStream;
        }
    }
}
