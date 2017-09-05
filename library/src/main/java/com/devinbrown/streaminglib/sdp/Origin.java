package com.devinbrown.streaminglib.sdp;

/**
 * Session Description Origin field as defined in RFC 4566
 * Template: o=<username> <sess-id> <sess-version> <nettype> <addrtype> <unicast-address>
 * Specification: https://tools.ietf.org/html/rfc4566#section-5.2
 */
public class Origin {
    public String username;
    public String sessionId;
    public String sessionVersion;
    public String netType;
    public String addressType;
    public String ipAddress;

    private Origin(String u, String si, String sv, String n, String a, String i) {
        username = u;
        sessionId = si;
        sessionVersion = sv;
        netType = n;
        addressType = a;
        ipAddress = i;
    }

    public static Origin fromString(String s) {
        Origin o = null;

        String username = "";
        String sessionId = "";
        String sessionVersion = "";
        String netType = "";
        String addressType = "";
        String ipAddress = "";

        String[] originArray = s.split(" ");
        if (originArray.length == 6) {
            username = originArray[0];
            sessionId = originArray[1];
            sessionVersion = originArray[2];
            netType = originArray[3];
            addressType = originArray[4];
            ipAddress = originArray[5];
            o = new Origin(username, sessionId, sessionVersion, netType, addressType, ipAddress);
        }

        return o;
    }
}
