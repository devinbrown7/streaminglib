package com.devinbrown.streaminglib.rtp;

import android.util.Log;
import android.util.Pair;

import com.devinbrown.streaminglib.media.RtpMedia;

import java.net.SocketException;

public class RtpServerStream extends RtpStream {

    public RtpServerStream(RtpMedia m) {
        super(m);
    }

    @Override
    public void initializeUdp() throws SocketException {
        rtpProtocol = RtpProtocol.UDP;
        streamType = StreamType.SERVER;
        delivery = Delivery.UNICAST;

        setupUdpPorts();

        state = RtpStreamState.INITIALIZED;
    }

    @Override
    public void initializeMulticast() {
        assert(false);
    }

    @Override
    public void configureUdp(Pair<Integer, Integer> remoteRtpPorts) throws IllegalStateException {
        Log.d("RtpServerStream", "configureUdp: !!!!!!!!");
        validateState(RtpStreamState.CONFIGURED, RtpStreamState.INITIALIZED);
        validateRtpProtocol(RtpProtocol.UDP);
        this.remoteRtpPorts = remoteRtpPorts;

        state = RtpStreamState.CONFIGURED;
    }

    @Override
    public void initializeTcp(Pair<Integer, Integer> interleavedRtpChannels) {
        rtpProtocol = RtpProtocol.TCP;
        streamType = StreamType.SERVER;
        delivery = Delivery.UNICAST;
        this.interleavedRtpChannels = interleavedRtpChannels;
        state = RtpStreamState.INITIALIZED;
    }

    @Override
    public void configureTcp() {
        Log.d("RtpServerStream", "configureTcp: !!!!!!!!");
        validateState(RtpStreamState.CONFIGURED, RtpStreamState.INITIALIZED);
        validateRtpProtocol(RtpProtocol.TCP);

        state = RtpStreamState.CONFIGURED;
    }
}
