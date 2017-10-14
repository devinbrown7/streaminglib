package com.devinbrown.streaminglib;

import com.devinbrown.streaminglib.media.RtpMedia;

/**
 * Created by devinbrown on 9/8/17.
 */

public class RtspServerStreamEvent {

    public static class StreamAvailable {
        private RtpMedia rtpMedia;

        public StreamAvailable(RtpMedia m) {
            rtpMedia = m;
        }

        public RtpMedia getRtpMedia() {
            return rtpMedia;
        }
    }
}
