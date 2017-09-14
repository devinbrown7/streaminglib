package com.devinbrown.streaminglib.rtsp.headers;

import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import com.devinbrown.streaminglib.rtp.RtpStream;
import com.devinbrown.streaminglib.rtsp.Rtsp;

/**
 * Reference: https://tools.ietf.org/html/rfc2326#page-58
 */

public class TransportHeader {
    private static final String TAG = "TransportHeader";

    public String transport;
    public String destination;
    public String source;
    public Rtsp.Method mode;
    public RtpStream.RtpProtocol rtpProtocol;
    public byte[] ssrc;
    public String append;

    // TCP specific
    public Pair<Integer, Integer> interleavedChannels;

    // UDP specific
    public RtpStream.Delivery delivery;
    public Pair<Integer, Integer> clientRtpPorts;
    public Pair<Integer, Integer> serverRtpPorts;

    // Multicast specific
    public Integer ttl;
    public Integer layers;

    private TransportHeader() {
    }

    /**
     * Parse Transport header
     * <p>
     * Request examples:
     * RTP/AVP;unicast;client_port=4588-4589
     * RTP/AVP;multicast;ttl=127;mode="PLAY",
     * RTP/AVP;unicast;client_port=3456-3457;mode="PLAY"
     * RTP/AVP/TCP;interleaved=0-1
     * <p>
     * Response examples:
     * RTP/AVP;unicast;destination=10.0.0.8;source=10.0.0.8;client_port=4588-4589;server_port=6970-6971
     * RTP/AVP/TCP;interleaved=0-1
     * RTP/AVP;unicast;client_port=8000-8001;server_port=9000-9001;ssrc=1234ABCD
     *
     * @param string Transport header string
     * @return TransportHeader
     */
    public static TransportHeader fromString(String string) {
        TransportHeader t = new TransportHeader();
        String[] transportArray = string.split(";");
        for (int i = 0; i < transportArray.length; i++) {
            String p = transportArray[i].trim();
            if (i == 0) {
                // Parse the transport-spec. It is always first.
                t.parseTransportSpec(p);
            } else {
                // Parse any other transport parameter
                try {
                    t.parseParameter(p);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Problem parsing Transport parameter: " + p);
                }
            }
        }
        return t;
    }

    public static TransportHeader fromRtpStream(RtpStream s) {
        TransportHeader t = new TransportHeader();

        // transport-spec
        // TODO: Should this be determined elsewhere? At least in a static constant?
        t.transport = "RTP/AVP";

        // Protocol specific parameters
        t.rtpProtocol = s.getRtpProtocol();
        switch (s.getRtpProtocol()) {
            case UDP:
                // Delivery method
                t.delivery = s.getDelivery();

                switch (s.getDelivery()) {
                    case MULTICAST:
                        // TODO: Should this be determined elsewhere? At least in a static constant?
                        t.ttl = 127;

                        // TODO: Should this be determined elsewhere? At least in a static constant?
                        t.layers = 1;
                        break;
                    case UNICAST:
                        // TODO: Destination
                        // t.destination =

                        // Client and Server ports
                        Log.d(TAG, "fromRtpStream: LOCAL " + (s.getLocalRtpPorts() == null ? "NULL" : "SET") + " REMOTE " + (s.getRemoteRtpPorts() == null ? "NULL" : "SET"));

                        switch (s.getStreamType()) {
                            case CLIENT:
                                t.clientRtpPorts = s.getLocalRtpPorts();
                                t.serverRtpPorts = s.getRemoteRtpPorts();
                                break;
                            case SERVER:
                                t.clientRtpPorts = s.getRemoteRtpPorts();
                                t.serverRtpPorts = s.getLocalRtpPorts();
                                break;
                        }
                        break;
                }

                // TODO
                // t.source =

                break;
            case TCP:
                // Interleaved specific
                t.interleavedChannels = s.getInterleavedRtpChannels();
                break;
        }

        // Delivery and protocol independent
        // TODO: Should this be determined elsewhere? At least in a static constant?
        t.mode = Rtsp.Method.PLAY;

        return t;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        switch (rtpProtocol) {
            case UDP:
                sb.append(transport).append(";"); // Don't need to add "/UDP" (RFC 2326, p. 59)
                sb.append(delivery.toString().toLowerCase()).append(";");

                if (delivery == null || delivery == RtpStream.Delivery.MULTICAST) {
                    // Default delivery is multicast (RFC 2326, p. 59)
                    if (ttl != null) sb.append("ttl").append("=").append(ttl).append(";");
                    if (layers != null) sb.append("layers").append("=").append(layers).append(";");
                } else {
                    // Delivery is unicast
                    if (destination != null) sb.append("destination=").append(destination);
                    if (source != null) sb.append("source=").append(source);
                    if (clientRtpPorts != null && clientRtpPorts.first != null && clientRtpPorts.second != null) {
                        sb.append("client_port=").append(clientRtpPorts.first).append("-").append(clientRtpPorts.second).append(";");
                    }
                    if (serverRtpPorts != null && serverRtpPorts.first != null && serverRtpPorts.second != null) {
                        sb.append("server_port=").append(serverRtpPorts.first).append("-").append(serverRtpPorts.second).append(";");
                    }
                }
                break;
            case TCP:
                sb.append(transport).append("/").append(RtpStream.RtpProtocol.TCP).append(";");

                // Interleaved
                sb.append("interleaved").append("=").append(interleavedChannels.first);
                if (interleavedChannels.second != null) {
                    sb.append("-").append(interleavedChannels.second);
                }
                sb.append(";");
                break;
        }

        if (mode != null) {
            sb.append("method").append("=").append("\"").append(mode).append("\"");
        }

        return sb.toString();
    }

    /**
     * Parses Transport header transport-spec value
     * <p>
     * Example: transport-protocol/profile/lower-transport
     *
     * @param s Transport header transport-spec value
     */
    private void parseTransportSpec(String s) {
        String[] transportArray = s.split("/");

        if (transportArray.length >= 2) {
            transport = transportArray[0] + "/" + transportArray[1];

            // Try to parse RtpProtocol. If it is not present then assume UDP (RFC 2326, p. 59)
            if (transportArray.length == 3) {
                rtpProtocol = RtpStream.RtpProtocol.valueOf(transportArray[2].toUpperCase());
            } else {
                rtpProtocol = RtpStream.RtpProtocol.UDP;
            }
        }
    }

    /**
     * Parses Transport header parameter
     *
     * @param s Transport header parameter
     * @throws IllegalArgumentException Unable to parse input or unexpected input
     */
    private void parseParameter(String s) throws IllegalArgumentException {
        Pair<String, String> keyValue = parseParameterIntoKeyValuePair(s);
        if (keyValue != null) {
            switch (keyValue.first) {
                case "unicast":
                case "multicast":
                    parseDelivery(keyValue.first);
                    break;
                case "interleaved":
                    if (keyValue.second != null) parseInterleaved(keyValue.second);
                    break;
                case "ttl":
                    if (keyValue.second != null) parseTtl(keyValue.second);
                    break;
                case "mode":
                    if (keyValue.second != null) parseMode(keyValue.second);
                    break;
                case "source":
                    if (keyValue.second != null) parseSource(keyValue.second);
                    break;
                case "destination":
                    if (keyValue.second != null) parseDestination(keyValue.second);
                    break;
                case "client_port":
                    if (keyValue.second != null) parseClientPorts(keyValue.second);
                    break;
                case "server_port":
                    if (keyValue.second != null) parseServerPorts(keyValue.second);
                    break;
                case "ssrc":
                    if (keyValue.second != null) parseSsrc(keyValue.second);
                    break;
                case "layers":
                    if (keyValue.second != null) parseLayers(keyValue.second);
                    break;
                default:
                    Log.w(TAG, "Unexpected Transport parameter: <" + keyValue.first + ">");
                    break;
            }
        }
    }

    private Pair<String, String> parseParameterIntoKeyValuePair(String s) {
        Pair<String, String> p = null;
        String[] sArray = s.split("=");
        if (sArray.length > 0) {
            String k = sArray[0].toLowerCase();
            String v = null;
            if (sArray.length >= 2) {
                v = sArray[1].toLowerCase();
            }
            p = new Pair<>(k, v);
        }
        return p;
    }

    /**
     * Example: unicast
     * Example: multicast
     *
     * @param s Delivery method value as string
     * @throws IllegalArgumentException Unable to parse input or unexpected input
     */
    private void parseDelivery(@NonNull String s) throws IllegalArgumentException {
        delivery = RtpStream.Delivery.valueOf(s.toUpperCase());
    }

    /**
     * Example: interleaved=0
     * Example: interleaved=0-1
     *
     * @param s Interleaved value as string
     * @throws IllegalArgumentException Unable to parse input or unexpected input
     */
    private void parseInterleaved(@NonNull String s) throws IllegalArgumentException {
        String[] a = s.split("-");
        if (a.length >= 1) {
            try {
                Integer rtp = Integer.parseInt(a[0]);
                Integer rtcp = null;
                if (a.length == 2) rtcp = Integer.parseInt(a[1]);
                interleavedChannels = new Pair<>(rtp, rtcp);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Problem parsing Transport parameter: client_ports");
            }
        }
    }

    /**
     * Example: 127
     *
     * @param s TTL value as string
     * @throws IllegalArgumentException Unable to parse input or unexpected input
     */
    private void parseTtl(@NonNull String s) throws IllegalArgumentException {
        try {
            ttl = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Problem parsing Transport parameter: TTL");
        }
    }

    /**
     * Example: mode="PLAY"
     * Example: mode="RECORD"
     * Example: mode=play
     * Example: mode=record
     *
     * @param s Mode method value as string
     * @throws IllegalArgumentException Unable to parse input or unexpected input
     */
    private void parseMode(@NonNull String s) throws IllegalArgumentException {
        String method = s.replace("\"", "");
        mode = Rtsp.Method.valueOf(method.toUpperCase());
    }

    /**
     * Example: source=10.0.0.8
     * Example: source=10.0.0.8
     *
     * @param s Source value as string
     * @throws IllegalArgumentException Unable to parse input or unexpected input
     */
    private void parseSource(@NonNull String s) throws IllegalArgumentException {
        source = s;
    }

    /**
     * Example: destination=10.0.0.8
     * Example: destination=224.0.1.11
     *
     * @param s Destination value as string
     * @throws IllegalArgumentException Unable to parse input or unexpected input
     */
    private void parseDestination(@NonNull String s) throws IllegalArgumentException {
        destination = s;
    }

    /**
     * Example: client_port=4588-4589
     *
     * @param s Client port as string
     * @throws IllegalArgumentException Unable to parse input or unexpected input
     */
    private void parseClientPorts(@NonNull String s) throws IllegalArgumentException {
        Log.d(TAG, "parseClientPorts: ");
        String[] a = s.split("-");
        if (a.length == 2) {
            try {
                int rtp = Integer.parseInt(a[0]);
                int rtcp = Integer.parseInt(a[1]);
                clientRtpPorts = new Pair<>(rtp, rtcp);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Problem parsing Transport parameter: client_ports");
            }
        }
    }

    /**
     * Example: server_port=6970-6971
     *
     * @param s Server port as string
     * @throws IllegalArgumentException Unable to parse input or unexpected input
     */
    private void parseServerPorts(@NonNull String s) throws IllegalArgumentException {
        Log.d(TAG, "parseServerPorts: ");
        String[] a = s.split("-");
        if (a.length == 2) {
            try {
                int rtp = Integer.parseInt(a[0]);
                int rtcp = Integer.parseInt(a[1]);
                serverRtpPorts = new Pair<>(rtp, rtcp);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Problem parsing Transport parameter: server_ports");
            }
        }
    }

    /**
     * Example: ssrc=1234ABCD
     *
     * @param s SSRC as string
     * @throws IllegalArgumentException Unable to parse input or unexpected input
     */
    private void parseSsrc(@NonNull String s) throws IllegalArgumentException {
        // TODO
    }

    /**
     * Example: layers=2
     *
     * @param s Multicast layers as string
     * @throws IllegalArgumentException Unable to parse input or unexpected input
     */
    private void parseLayers(@NonNull String s) throws IllegalArgumentException {
        try {
            layers = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Problem parsing Transport parameter: layers");
        }
    }
}
