package com.devinbrown.streaminglib.media;

import android.media.MediaFormat;

import com.devinbrown.streaminglib.sdp.MediaDescription;

import java.net.URI;

public class RtpMedia {
    public URI uri;
    public MediaDescription mediaDescription;
    public MediaFormat mediaFormat;

    RtpMedia(URI u, MediaDescription d, MediaFormat f) {
        uri = u;
        mediaDescription = d;
        mediaFormat = f;
    }
}
