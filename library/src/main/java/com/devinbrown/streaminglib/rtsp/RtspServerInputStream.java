package com.devinbrown.streaminglib.rtsp;

import android.media.MediaFormat;

/**
 * This is a stream of data that the RTSP server can use as a media
 */

public class RtspServerInputStream {
    // Has a MediaFormat

    // Gets events for some incomming stream

    // Posts events of that stream

    // Is there a way to cut out this middle-man and just have input messages received by the other end?
    // Maybe this class just facilitates that connection.

    private MediaFormat format;

    public RtspServerInputStream(MediaFormat f) {
        format = f;
    }

    public MediaFormat getFormat() {
        return format;
    }
}
