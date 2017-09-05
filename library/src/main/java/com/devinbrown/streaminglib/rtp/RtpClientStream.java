package com.devinbrown.streaminglib.rtp;

import android.util.Log;
import android.util.Pair;

import com.devinbrown.streaminglib.RtspClientStreamEvent;
import com.devinbrown.streaminglib.media.RtpMedia;
import com.devinbrown.streaminglib.rtsp.RtspClientEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.Arrays;

/**
 *
 */

// TODO: RTCP!

public class RtpClientStream extends RtpStream {
    public static final int MTU = 1400;

    private RtpInputProcessor rtpInputProcessor;

    public EventBus streamEventBus;
    public RtpMedia media;

    public RtpClientStream(RtpMedia m) {
        media = m;
        streamEventBus = new EventBus();
        rtpInputProcessor = new RtpInputProcessor(streamEventBus);
    }

    /**
     * Create RtpClientStream for UDP
     */
    public void initializeUdp() throws SocketException {
        rtpProtocol = RtpProtocol.UDP;
        streamType = StreamType.CLIENT;
        delivery = Delivery.UNICAST;

        setupUdpPorts();

        state = RtpStreamState.INITIALIZED;
    }

    /**
     * Create RtpClientStream for UDP
     */
    public void initializeMulticast() {
        // TODO: Multicast not yet supported
        assert (false);

        rtpProtocol = RtpProtocol.UDP;
        streamType = StreamType.CLIENT;
        delivery = Delivery.MULTICAST;

        // TODO: This might need to be different for multicast
        // setupUdpPorts(localRtpPorts);

        state = RtpStreamState.INITIALIZED;
    }

    public void configureUdp(Pair<Integer, Integer> remoteRtpPorts) throws IllegalStateException {
        validateState(RtpStreamState.CONFIGURED, RtpStreamState.INITIALIZED);
        validateRtpProtocol(RtpProtocol.UDP);
        this.remoteRtpPorts = remoteRtpPorts;

        state = RtpStreamState.CONFIGURED;

        startListening();
    }

    /**
     * Create RtpClientStream for TCP (RTSP Interleaved)
     *
     * @param interleavedRtpChannels The RTSP interleaved channels this stream will use
     */
    public void initializeTcp(Pair<Integer, Integer> interleavedRtpChannels) {
        this.interleavedRtpChannels = interleavedRtpChannels;
        rtpProtocol = RtpProtocol.UDP;
        streamType = StreamType.SERVER;

        state = RtpStreamState.INITIALIZED;
    }

    public void configureTcp() {
        validateState(RtpStreamState.CONFIGURED, RtpStreamState.INITIALIZED);
        validateRtpProtocol(RtpProtocol.TCP);

        state = RtpStreamState.CONFIGURED;

        startListening();
    }

    public RtpPacketType getTypeByChannel(int channel) {
        RtpPacketType t = null;
        Integer rtp = interleavedRtpChannels.first;
        Integer rtcp = interleavedRtpChannels.second;
        if (rtp != null && rtp == channel) {
            t = RtpPacketType.RTP;
        } else if (rtcp != null && rtcp == channel) {
            t = RtpPacketType.RTCP;
        }
        return t;
    }

    private void startListening() {
        new Thread(new RtpInputListener()).start();
        new Thread(new RtcpInputListener()).start();
        state = RtpStreamState.STREAMING;
    }

    /**
     * Listen for RTP packets on UDP
     */
    private class RtpInputListener implements Runnable {
        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    byte[] buffer = new byte[MTU];
                    DatagramPacket p = new DatagramPacket(buffer, MTU);
                    rtpSocket.receive(p);
                    streamEventBus.post(new RtspClientEvent.RtpPacketReceived(buffer));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Listen for RTCP packets on UDP
     */
    private class RtcpInputListener implements Runnable {
        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    byte[] buffer = new byte[MTU];
                    DatagramPacket p = new DatagramPacket(buffer, MTU);
                    rtcpSocket.receive(p);
                    streamEventBus.post(new RtspClientEvent.RtcpPacketReceived(buffer));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Received and analyzes incoming RTP and RTCP data
     */
    private class RtpInputProcessor {
        private static final String TAG = "RtpInputProcessor";

        private EventBus streamEventBus;

        RtpInputProcessor(EventBus e) {
            streamEventBus = e;
            streamEventBus.register(this);
        }

        @Subscribe(threadMode = ThreadMode.ASYNC)
        public void handleEvent(RtspClientEvent.RtpPacketReceived event) {
            handleRtp(event.data);
        }

        @Subscribe(threadMode = ThreadMode.ASYNC)
        public void handleEvent(RtspClientEvent.RtcpPacketReceived event) {
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
}
