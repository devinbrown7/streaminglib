package com.devinbrown.streaminglib.rtsp;

import com.devinbrown.streaminglib.media.RtpMedia;

/**
 * This is a stream of data that the RTSP server can use as a media
 */

public class RtspInputStream {
    // Has a MediaFormat

    // Gets events for some incomming stream

    // Posts events of that stream

    // Is there a way to cut out this middle-man and just have input messages received by the other end?
    // Maybe this class just facilitates that connection.

    private RtpMedia rtpMedia;
    private String control;

    public RtspInputStream(RtpMedia r) {
        rtpMedia = r;
    }

    public RtpMedia getRtpMedia() {
        return rtpMedia;
    }

    public String getControl() {
        return control;
    }

    public void setControl(String control) {
        this.control = control;
    }
}
