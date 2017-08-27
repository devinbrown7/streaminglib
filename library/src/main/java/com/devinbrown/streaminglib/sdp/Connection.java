package com.devinbrown.streaminglib.sdp;

/**
 * Format: c=<nettype> <addrtype> <connection-address>
 * Specification: https://tools.ietf.org/html/rfc4566#section-5.7
 */
public class Connection {
    public String netType;
    public String addressType;
    public String connectionAddress;

    private Connection(String n, String a, String c) {
        netType = n;
        addressType = a;
        connectionAddress = c;
    }

    public static Connection fromString(String s) {
        String netType = "";
        String addressType = "";
        String connectionAddress = "";

        String[] connectionArray = s.split(" ");
        if (connectionArray.length == 3) {
            netType = connectionArray[0];
            addressType = connectionArray[1];
            connectionAddress = connectionArray[2];
        }

        return new Connection(netType, addressType, connectionAddress);
    }

    @Override
    public String toString() {
        return "c=" + netType + " " + addressType + " " + connectionAddress;
    }
}
