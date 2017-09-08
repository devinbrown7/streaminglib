package com.devinbrown.streaminglib.sdp;

/**
 * Specification: https://tools.ietf.org/html/rfc4566#section-5.8
 * Format: t=<start-time> <stop-time>
 * Example: t=0 0 (unbounded)
 * Example: t=0 1234567 (unbounded start, defined end)
 * Example: t=1234567 0 (defined start, unbounded end)
 */
public class Timing {
    private int startTime;
    private int stopTime;

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("t=").append(startTime).append(" ").append(stopTime);
        return sb.toString();
    }
}
