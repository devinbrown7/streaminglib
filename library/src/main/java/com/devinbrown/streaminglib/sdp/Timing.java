package com.devinbrown.streaminglib.sdp;

/**
 * Specification: https://tools.ietf.org/html/rfc4566#section-5.8
 * Format: t=<start-time> <stop-time>
 */
public class Timing {
    public int startTime;
    public int stopTime;

    private Timing(int st, int sp) {
        startTime = st;
        stopTime = sp;
    }

    public static Timing fromString(String s) {
        int startTime = 0;
        int stopTime = 0;

        String[] timingArray = s.split(" ");
        if (timingArray.length == 2) {
            startTime = Integer.parseInt(timingArray[0]);
            stopTime = Integer.parseInt(timingArray[1]);
        }

        return new Timing(startTime, stopTime);
    }
}
