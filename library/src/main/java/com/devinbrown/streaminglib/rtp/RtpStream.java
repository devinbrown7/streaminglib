package com.devinbrown.streaminglib.rtp;

import java.net.DatagramSocket;
import java.net.Socket;

/**
 * RtpStream encapsulates an RTP/RTCP session
 *
 * Reference: https://tools.ietf.org/html/rfc3550
 */

public class RtpStream {
    enum RtpStreamState {NEW, INITIALIZED, CONFIGURED, STREAMING, PLAYING, PAUSED, FINISHED}

    RtpStreamState state = RtpStreamState.NEW;

    // UDP
    Integer rtpLocalPort;
    Integer rtcpLocalPort;
    Integer rtpRemotePort;
    Integer rtcpRemotePort;

    // TCP (RTSP Interleaved)
    Integer rtpChannel;
    Integer rtcpChannel;

    RtpProtocol rtpProtocol;
    StreamType streamType;
    String sessionId;

    public enum RtpProtocol {TCP, UDP}

    enum StreamType {SERVER, CLIENT}

    // TCP
    Socket rtpTcpSocket;
    Socket rtcpTcpSocket;

    // UDP
    DatagramSocket rtpUdpSocket;
    DatagramSocket rtcpUdpSocket;

    void setupUdpPorts(int rtpPort, int rtcpPort) {

    }

    /**
     * TODO: maybe make RTCP run in an extension of Runnable
     * TODO: maybe make RTCP Client and Server and base class like RTP
     */
    void setupRtcp() {
        Thread rtcpThread = new Thread(new Runnable() {
            @Override
            public void run() {

            }
        });
        rtcpThread.start();
    }

    void validateState(RtpStreamState newState, RtpStreamState requiredState) throws IllegalStateException {
        if (state != requiredState) {
            String msg = "RtpClientStream can only enter " + newState.name() + " state from the " +
                    requiredState.name() + " state (current RtpStreamState: <" + state.name() + ">)";
            throw new IllegalStateException(msg);
        }
    }

    void validateRtpProtocol(RtpProtocol requiredProtocol) throws IllegalStateException{
        if (rtpProtocol != requiredProtocol) {
            String msg = "RtpClientStream must be initialized for " + requiredProtocol.name() +
                    " (current RtpProtocol: <" + rtpProtocol.name() + ">)";
            throw new IllegalStateException(msg);
        }
    }

    public RtpProtocol getRtpProtocol() {
        return rtpProtocol;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
