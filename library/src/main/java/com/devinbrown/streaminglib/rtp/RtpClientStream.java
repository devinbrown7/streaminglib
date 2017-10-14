package com.devinbrown.streaminglib.rtp;

import android.util.Pair;

import com.devinbrown.streaminglib.media.RtpMedia;
import com.devinbrown.streaminglib.rtsp.RtspSession;

import java.net.SocketException;

/**
 *
 */

// TODO: RTCP!

public class RtpClientStream extends RtpStream {
    private RtpInputProcessor rtpInputProcessor;

    public RtpClientStream(RtspSession s, RtpMedia m) {
        super(s, m);
        rtpInputProcessor = new RtpInputProcessor(m, streamEventBus);
    }

    /**
     * Create RtpClientStream for UDP
     */
    @Override
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
    @Override
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

    /**
     * Create RtpClientStream for TCP (RTSP Interleaved)
     *
     * @param interleavedRtpChannels The RTSP interleaved channels this stream will use
     */
    @Override
    public void initializeTcp(Pair<Integer, Integer> interleavedRtpChannels) {
        this.interleavedRtpChannels = interleavedRtpChannels;
        rtpProtocol = RtpProtocol.UDP;
        streamType = StreamType.SERVER;

        state = RtpStreamState.INITIALIZED;
    }

    @Override
    public void configureTcp() {
        validateState(RtpStreamState.CONFIGURED, RtpStreamState.INITIALIZED);
        validateRtpProtocol(RtpProtocol.TCP);

        state = RtpStreamState.CONFIGURED;

        startListening();
    }

    @Override
    void startListening() {
        new Thread(new RtpInputListener()).start();
        new Thread(new RtcpInputListener()).start();
        state = RtpStreamState.STREAMING;
    }
}
