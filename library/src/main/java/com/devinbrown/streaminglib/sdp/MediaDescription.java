package com.devinbrown.streaminglib.sdp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.devinbrown.streaminglib.Constants.CRLF;

/**
 * Specification: https://tools.ietf.org/html/rfc4566#section-5.14
 * Format: m=<media> <port> </number> <proto> <fmt> ...
 */

public class MediaDescription extends Description {
    public String media;
    public Integer port;
    public Integer numberOfPorts;
    public String proto;
    public List<Integer> payloadTypes = new ArrayList<>();

    private MediaDescription(String m, Integer po, Integer n, String pr, List<Integer> pt) {
        media = m;
        port = po;
        numberOfPorts = n;
        proto = pr;
        payloadTypes = pt;
    }

    public static MediaDescription fromString(String s) {
        MediaDescription m = null;

        String media = null;
        int port = 0;
        int numberOfPorts = 1;
        String proto = null;
        List<Integer> payloadTypes = new ArrayList<>();

        String[] mediaArray = s.split(" ", 4);
        if (mediaArray.length >= 4) {

            // media
            media = mediaArray[0];

            // port /number
            String[] portAndNumber = mediaArray[1].split("/");
            port = Integer.parseInt(portAndNumber[0]);
            if (portAndNumber.length == 2) {
                numberOfPorts = Integer.parseInt(portAndNumber[1]);
            }

            // proto
            proto = mediaArray[2];

            // fmts
            for (String payloadTypesString : Arrays.asList(mediaArray[3].split(" "))) {
                payloadTypes.add(Integer.valueOf(payloadTypesString));
            }

            m = new MediaDescription(media, port, numberOfPorts, proto, payloadTypes);
        }

        return m;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Media
        sb.append("m=").append(" ").append(port);
        if (numberOfPorts > 1) {
            sb.append("/").append(numberOfPorts);
        }
        sb.append(" ").append(proto);
        for (int p : payloadTypes) {
            sb.append(" ").append(p);
        }
        sb.append(CRLF);

        // Information
        if (information != null) {
            sb.append(information).append(CRLF);
        }

        // Connection
        if (connection != null) {
            sb.append(connection).append(CRLF);
        }

        // Bandwidth
        for (Bandwidth b : bandwidths) {
            sb.append(b).append(CRLF);
        }

        // Key
        if (key != null) {
            sb.append(key).append(CRLF);
        }

        // Attributes
        for (Attribute a : attributes) {
            sb.append(a).append(CRLF);
        }

        return sb.toString();
    }
}
