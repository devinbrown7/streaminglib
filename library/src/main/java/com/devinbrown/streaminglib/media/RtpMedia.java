package com.devinbrown.streaminglib.media;

import android.media.MediaFormat;
import android.net.Uri;

import com.devinbrown.streaminglib.sdp.MediaDescription;

import org.greenrobot.eventbus.EventBus;

public class RtpMedia {
    public Uri uri;
    public String control;
    public MediaDescription mediaDescription;
    public MediaFormat mediaFormat;
    public EventBus streamEventBus;

    public RtpMedia(Uri u, String c, MediaDescription d, MediaFormat f) {
        uri = u;
        control = c;
        mediaDescription = d;
        mediaFormat = f;

        // TODO: Only use for debugging
        streamEventBus = new EventBus();

        // TODO: Use this
        //streamEventBus = EventBus.builder().logNoSubscriberMessages(false).build();
    }
}
