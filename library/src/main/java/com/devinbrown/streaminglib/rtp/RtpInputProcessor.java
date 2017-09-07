package com.devinbrown.streaminglib.rtp;

import android.util.Log;

import com.devinbrown.streaminglib.RtspClientStreamEvent;
import com.devinbrown.streaminglib.rtsp.RtspSessionEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;

/**
 * Received and analyzes incoming RTP and RTCP data
 */
class RtpInputProcessor {
    private static final String TAG = "RtpInputProcessor";

    private EventBus streamEventBus;

    RtpInputProcessor(EventBus e) {
        streamEventBus = e;
        streamEventBus.register(this);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void handleEvent(RtspSessionEvent.RtpPacketReceived event) {
        handleRtp(event.data);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void handleEvent(RtspSessionEvent.RtcpPacketReceived event) {
        Log.d(TAG, "handleEvent: RtspClientEvent.RtcpPacketReceived");
    }

    private void handleRtp(byte[] data) {
        // TODO: Analyze RTP data (sequence number, timing, etc)

        sendMediaData(data);
    }

    private void handleRtcp(byte[] data) {
        // TODO: Analyze RTCP packet
    }

    /**
     * Remove RTP header and post the media data
     * <p>
     * Reference: https://tools.ietf.org/html/rfc3550#section-5.1
     */
    private void sendMediaData(byte[] rtpData) {
        // TODO: The header length is dynamic, but 12 is the "normal" length
        byte[] mediaData = Arrays.copyOfRange(rtpData, 12, rtpData.length);
        streamEventBus.post(new RtspClientStreamEvent.MediaDataReceived(mediaData));
    }
}
