package com.devinbrown.streaminglib.sdp;

/**
 * Specification: https://tools.ietf.org/html/rfc4566#section-5.8
 * Format: b=<bwtype>:<bandwidth>
 */
public class Bandwidth {
    public String bandwidthType;
    public int bandwidth;

    private Bandwidth(String bt, int b) {
        bandwidthType = bt;
        bandwidth = b;
    }

    public static Bandwidth fromString(String s) {
        String bandwidthType = "";
        int bandwidth = 0;

        String[] bandwitdhArray = s.split(" ");
        if (bandwitdhArray.length == 2) {
            bandwidthType = bandwitdhArray[0];
            bandwidth = Integer.parseInt(bandwitdhArray[1]);
        }

        return new Bandwidth(bandwidthType, bandwidth);
    }

    @Override
    public String toString() {
        return "b=" + bandwidthType + ":" + bandwidth;
    }
}
