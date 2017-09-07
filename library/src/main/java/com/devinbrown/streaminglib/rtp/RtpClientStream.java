package com.devinbrown.streaminglib.rtp;

import android.util.Pair;

import com.devinbrown.streaminglib.media.RtpMedia;
import com.devinbrown.streaminglib.rtsp.RtspSessionEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;

/**
 *
 */

// TODO: RTCP!

public class RtpClientStream extends RtpStream {
    public static final int MTU = 1400;

    private RtpInputProcessor rtpInputProcessor;

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
                    streamEventBus.post(new RtspSessionEvent.RtpPacketReceived(buffer));
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
                    streamEventBus.post(new RtspSessionEvent.RtcpPacketReceived(buffer));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
