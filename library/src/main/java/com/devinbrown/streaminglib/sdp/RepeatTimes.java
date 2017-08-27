package com.devinbrown.streaminglib.sdp;

import java.util.ArrayList;
import java.util.List;

/**
 * Specification: https://tools.ietf.org/html/rfc4566#section-5.10
 * Format: r=<repeat interval> <active duration> <offsets from start-time>
 */
public class RepeatTimes {
    public int repeatInterval;
    public int activeDuration;
    public List<Integer> offsetsFromStart;

    private RepeatTimes(int r, int a, List<Integer> o) {
        repeatInterval = r;
        activeDuration = a;
        offsetsFromStart = o;
    }

    public static RepeatTimes fromString(String s) {
        int repeatInterval = 0;
        int activeDuration = 0;
        List<Integer> offsetsFromStart = new ArrayList<>();

        String[] repeatTimesArray = s.split(" ");
        if (repeatTimesArray.length >= 3) {
            repeatInterval = Integer.parseInt(repeatTimesArray[0]);
            activeDuration = Integer.parseInt(repeatTimesArray[1]);
            for (int i = 2; i < repeatTimesArray.length; i++) {
                offsetsFromStart.add(Integer.parseInt(repeatTimesArray[i]));
            }
        }

        return new RepeatTimes(repeatInterval, activeDuration, offsetsFromStart);
    }
}
