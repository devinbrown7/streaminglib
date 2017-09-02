package com.devinbrown.streaminglib.rtp;

/**
 *
 */

// TODO: RTCP!

public class RtpClientStream extends RtpStream {

    /**
     * Create RtpClientStream for UDP
     *
     * @param rtpLocalPort RTP stream port
     * @param rtcpLocalPort RTCP stream port
     */
    public void initializeUdp(int rtpLocalPort, int rtcpLocalPort) {
        this.rtpLocalPort = rtpLocalPort;
        this.rtcpLocalPort = rtcpLocalPort;
        rtpProtocol = RtpProtocol.UDP;
        streamType = StreamType.CLIENT;

        // Set up the right type of sockets
        setupUdpPorts(rtpLocalPort, rtcpLocalPort);

        state = RtpStreamState.INITIALIZED;
    }

    public void configureUdp(int rtpRemotePort, int rtcpRemotePort) throws IllegalStateException {
        if (state != RtpStreamState.INITIALIZED) {
            throw new IllegalStateException("RtpClientStream can only be Configured from the Initialized state (currently state: <" + state.name() + ">)");
        }

        if (rtpProtocol != RtpProtocol.UDP) {
            throw new IllegalStateException("RtpClientStream can only be Configured for the RtpProtocol in which it was initialized as (current RtpProtocol: <" + rtpProtocol.name() + ">)");
        }

        state = RtpStreamState.CONFIGURED;
    }

    /**
     * Create RtpClientStream for TCP (RTSP Interleaved)
     *
     * @param rtpChannel The RTSP interleaved channel this stream will use
     */
    public void initializeTcp(int rtpChannel, int rtcpChannel) {
        this.rtpChannel = rtcpChannel;
        this.rtcpChannel = rtcpChannel;
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
