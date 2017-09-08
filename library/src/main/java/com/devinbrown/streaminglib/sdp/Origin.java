package com.devinbrown.streaminglib.sdp;

import com.devinbrown.streaminglib.Utils;

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

    public Origin(String username,
                   String sessionId,
                   String sessionVersion,
                   String netType,
                   String addressType,
                   String ipAddress) {
        this.username = username;
        this.sessionId = sessionId;
        this.sessionVersion = sessionVersion;
        this.netType = netType;
        this.addressType = addressType;
        this.ipAddress = ipAddress;
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

    @Override
    public String toString() {
        return "o=" + Utils.join(new String[]{username, sessionId, sessionVersion, netType, addressType, ipAddress}, " ");
    }
}
