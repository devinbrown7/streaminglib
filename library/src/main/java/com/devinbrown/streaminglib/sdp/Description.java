package com.devinbrown.streaminglib.sdp;

import java.util.ArrayList;
import java.util.List;

public abstract class Description {
    public String information;
    public Connection connection;
    public List<Bandwidth> bandwidths = new ArrayList<>();
    public Key key;
    public List<Attribute> attributes = new ArrayList<>();

    public List<String> getAttributeValues(String key) {
        List<String> values = new ArrayList<>();
        for (Attribute a : attributes) {
            if (a.attribute.toLowerCase().equals(key.toLowerCase())) {
                values.add(a.value);
            }
        }
        return values;
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
