package com.devinbrown.streaminglib.sdp;

import android.media.MediaFormat;

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

    private MediaDescription(String media, Integer port, Integer numberOfPorts, String proto, List<Integer> payloadTypes) {
        this.media = media;
        this.port = port;
        this.numberOfPorts = numberOfPorts;
        this.proto = proto;
        this.payloadTypes = payloadTypes;
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
        if (numberOfPorts != null && numberOfPorts > 1) sb.append("/").append(numberOfPorts);
        sb.append(" ").append(proto);
        for (int p : payloadTypes) sb.append(" ").append(p);
        sb.append(CRLF);

        // Information
        if (information != null) sb.append(information).append(CRLF);

        // Connection
        if (connection != null) sb.append(connection).append(CRLF);

        // Bandwidth
        for (Bandwidth b : bandwidths) sb.append(b).append(CRLF);

        // Key
        if (key != null) sb.append(key).append(CRLF);

        // Attributes
        for (Attribute a : attributes) sb.append(a).append(CRLF);

        return sb.toString();
    }

    public static MediaDescription fromMedia(MediaFormat m, int payloadType) {
        MediaDescription md = null;
        String media = null;
        Integer port = 0; // This means there is no port preference
        Integer numberOfPorts = null; // This means there is no port preference
        String proto = "RTP/AVP";
        List<Integer> payloadTypes = null;

        // MIME type
        String mimeType = null;
        if (m.containsKey(MediaFormat.KEY_MIME)) {
            mimeType = m.getString(MediaFormat.KEY_MIME);
        }

        // Sample rate
        Integer rate = null;
        if (m.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
            rate = m.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        }

        // Channel count
        Integer channelCount = 1; // Default to mono
        if (m.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
            channelCount = m.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
        }

        if (mimeType != null) {
            String[] mediaTypeArray = mimeType.split("/");
            String mimeSubType = null;
            if (mediaTypeArray.length == 2) {
                media = mediaTypeArray[0];
                mimeSubType = mediaTypeArray[1];
            }

            // Create MediaDescription and Rtpmap from parsed values
            md = new MediaDescription(media, port, numberOfPorts, proto, payloadTypes);
            Rtpmap map = new Rtpmap(payloadType, mimeSubType, rate, channelCount);
            md.setAttributeValue("rtpmap", map.toString());
        }

        return md;
    }


    public Rtpmap getRtpmapWithFormat(int format) {
        Rtpmap r = null;
        List<Rtpmap> rtpmaps = getRtpmaps();
        for (Rtpmap rtpmap : rtpmaps) {
            if (rtpmap.payloadType == format) {
                r = rtpmap;
                break;
            }
        }
        return r;
    }

    public Fmtp getFmtpWithFormat(int format) {
        Fmtp f = null;
        List<Fmtp> fmtps = getFmtps();
        for (Fmtp fmtp : fmtps) {
            if (fmtp.payloadType == format) {
                f = fmtp;
                break;
            }
        }
        return f;
    }

    public List<Rtpmap> getRtpmaps() {
        List<String> rtpmapStrings = getAttributeValues("rtpmap");
        List<Rtpmap> rtpmaps = new ArrayList<>();

        for (String rtpmapString : rtpmapStrings) {
            rtpmaps.add(Rtpmap.fromString(rtpmapString));
        }

        return rtpmaps;
    }

    public List<Fmtp> getFmtps() {
        List<String> fmtpStrings = getAttributeValues("rtpmap");
        List<Fmtp> fmtps = new ArrayList<>();

        for (String fmtpString : fmtpStrings) {
            fmtps.add(Fmtp.fromString(fmtpString));
        }

        return fmtps;
    }
}
