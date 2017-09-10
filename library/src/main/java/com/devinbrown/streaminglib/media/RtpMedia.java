package com.devinbrown.streaminglib.media;

import android.media.MediaFormat;
import android.net.Uri;

import com.devinbrown.streaminglib.sdp.MediaDescription;

public class RtpMedia {
    public Uri uri;
    public MediaDescription mediaDescription;
    public MediaFormat mediaFormat;

    RtpMedia(Uri u, MediaDescription d, MediaFormat f) {
        uri = u;
        mediaDescription = d;
        mediaFormat = f;
    }
}
