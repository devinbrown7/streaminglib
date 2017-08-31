package com.devinbrown.streaminglib.rtp;

import java.net.DatagramSocket;
import java.net.Socket;

/**
 * RtpStream encapsulates an RTP/RTCP session
 *
 * Reference: https://tools.ietf.org/html/rfc3550
 */

public class RtpStream {
    private int rtpPort;
    private int rtcpPort;
    private RtpProtocol rtpProtocol;
    private StreamType streamType;

    enum RtpProtocol {TCP, UDP}

    enum StreamType {SERVER, CLIENT}

    // TCP
    Socket rtpTcpSocket;
    Socket rtcpTcpSocket;

    // UDP
    DatagramSocket rtpUdpSocket;
    DatagramSocket rtcpUdpSocket;

    RtpStream(int rtpPort, int rtcpPort, RtpProtocol rtpProtocol, StreamType streamType) {
        this.rtpPort = rtpPort;
        this.rtcpPort = rtcpPort;
        this.rtpProtocol = rtpProtocol;
        this.streamType = streamType;

        // Set up the right type sockets
        switch (rtpProtocol) {
            case TCP:
                setupTcpPorts(rtpPort, rtcpPort);
                break;
            case UDP:
                setupUdpPorts(rtpPort, rtcpPort);
                break;
        }

        // Establish what type of stream this will be (in or out)
        switch (streamType) {
            case SERVER:
                setupRtpServer();
                break;
            case CLIENT:
                setupRtpClient();
                break;
        }

        // Trigger RTCP thread
        setupRtcp();
    }

    private void setupTcpPorts(int rtpPort, int rtcpPort) {

    }

    private void setupUdpPorts(int rtpPort, int rtcpPort) {

    }

    private void setupRtpServer() {

    }

    private void setupRtpClient() {

    }

    /**
     * TODO: maybe make RTCP run in an extension of Runnable
     */
    private void setupRtcp() {
        Thread rtcpThread = new Thread(new Runnable() {
            @Override
            public void run() {

            }
        });
        rtcpThread.start();
    }

    /*
     * Server: Receives data and sends it on the socket
     *
     * Client: Receives data on the socket and sends it somewhere
     *
     * Maybe these need to be different implementations?
     *
     *
     *
     */
}
