package com.devinbrown.streaminglib;

import com.devinbrown.streaminglib.rtsp.RtspInputStream;

/**
 * Created by devinbrown on 9/8/17.
 */

public class RtspServerStreamEvent {

    public static class StreamAvailable {
        private RtspInputStream rtspInputStream;

        public StreamAvailable(RtspInputStream is) {
            rtspInputStream = is;
        }

        public RtspInputStream getRtspInputStream() {
            return rtspInputStream;
        }
    }
}
