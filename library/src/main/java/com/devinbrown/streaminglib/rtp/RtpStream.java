package com.devinbrown.streaminglib.rtp;

import android.util.Pair;

import com.devinbrown.streaminglib.media.RtpMedia;

import org.greenrobot.eventbus.EventBus;

import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * RtpStream encapsulates an RTP/RTCP session
 * <p>
 * Reference: https://tools.ietf.org/html/rfc3550
 */

public class RtpStream {
    enum RtpStreamState {NEW, INITIALIZED, CONFIGURED, STREAMING, PLAYING, PAUSED, FINISHED}

    public enum RtpPacketType {RTP, RTCP}

    public enum RtpProtocol {TCP, UDP}

    public enum StreamType {SERVER, CLIENT}

    public enum Delivery {UNICAST, MULTICAST}

    private static final int STARTING_UDP_RTP_PORT = 50000;

    // UDP
    Pair<Integer, Integer> localRtpPorts;
    Pair<Integer, Integer> remoteRtpPorts;

    // TCP (RTSP Interleaved)
    Pair<Integer, Integer> interleavedRtpChannels;

    RtpProtocol rtpProtocol;
    StreamType streamType;
    Delivery delivery;
    String sessionId;
    Integer timeout;
    RtpStreamState state = RtpStreamState.NEW;
    RtpMedia rtpMedia;
    EventBus streamEventBus;

    // UDP
    DatagramSocket rtpSocket;
    DatagramSocket rtcpSocket;

    public EventBus getStreamEventBus() {
        return streamEventBus;
    }

    void setupUdpPorts() throws SocketException {
        int port = STARTING_UDP_RTP_PORT;
        while (rtpSocket == null && rtcpSocket == null) {
            if (port >= 0xFFFF) {
                throw new SocketException("Unable to create two consecutive sockets.");
            }
            try {
                rtpSocket = new DatagramSocket(port);
                rtcpSocket = new DatagramSocket(port + 1);
            } catch (SocketException e) {
                rtpSocket = null;
                rtcpSocket = null;
                port += 2;
            }
        }
        localRtpPorts = new Pair<>(rtpSocket.getLocalPort(), rtcpSocket.getLocalPort());
    }

    /**
     * TODO: Make RTCP run in an extension of Runnable or make RTCP Client and Server and base class like RTP
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

    void validateRtpProtocol(RtpProtocol requiredProtocol) throws IllegalStateException {
        if (rtpProtocol != requiredProtocol) {
            String msg = "RtpClientStream must be initialized for " + requiredProtocol.name() +
                    " (current RtpProtocol: <" + rtpProtocol.name() + ">)";
            throw new IllegalStateException(msg);
        }
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


    public RtpProtocol getRtpProtocol() {
        return rtpProtocol;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public RtpMedia getRtpMedia() {
        return rtpMedia;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public StreamType getStreamType() {
        return streamType;
    }

    public Delivery getDelivery() {
        return delivery;
    }

    public Pair<Integer, Integer> getLocalRtpPorts() {
        return localRtpPorts;
    }

    public Pair<Integer, Integer> getRemoteRtpPorts() {
        return remoteRtpPorts;
    }

    public Pair<Integer, Integer> getInterleavedRtpChannels() {
        return interleavedRtpChannels;
    }
}
