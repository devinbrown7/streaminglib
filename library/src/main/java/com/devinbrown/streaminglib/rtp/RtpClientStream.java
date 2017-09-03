package com.devinbrown.streaminglib.rtp;

import android.media.MediaFormat;
import android.util.Pair;

import java.net.SocketException;

/**
 *
 */

// TODO: RTCP!

public class RtpClientStream extends RtpStream {

    public RtpClientStream(MediaFormat f) {
        format = f;
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
    }
}
