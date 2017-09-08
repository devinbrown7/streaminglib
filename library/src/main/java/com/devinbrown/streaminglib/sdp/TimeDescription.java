package com.devinbrown.streaminglib.sdp;

import java.util.List;

import static com.devinbrown.streaminglib.Constants.CRLF;

/**
 * Combination of Timing and RepeatTimes
 */

public class TimeDescription {
    Timing timing;
    List<RepeatTimes> repeatTimes;

    TimeDescription(Timing t) {
        timing = t;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(timing).append(CRLF);
        for (RepeatTimes t : repeatTimes) sb.append(t);
        return sb.toString();
    }
}
