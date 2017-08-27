package com.devinbrown.streaminglib.sdp;

import java.util.ArrayList;
import java.util.List;

/**
 * Specification: https://tools.ietf.org/html/rfc4566#section-5.11
 * Format: z=<adjustment time> <offset> <adjustment time> <offset> ....
 */

public class TimeZoneAdjustments {
    static class TimeZoneAdjustment {
        int adjustmentTime;
        int offset;

        TimeZoneAdjustment(int a, int o) {
            adjustmentTime = a;
            offset = a;
        }
    }

    public List<TimeZoneAdjustment> adjustments = new ArrayList<>();

    private TimeZoneAdjustments(List<TimeZoneAdjustment> a) {
        adjustments = a;
    }

    public static TimeZoneAdjustments fromString(String s) {
        List<TimeZoneAdjustment> adjustments = new ArrayList<>();

        String[] tzaArray = s.split(" ");
        if (tzaArray.length % 2 == 0) {
            for (int i = 0; i < tzaArray.length / 2; i++) {
                int adjustmentTime = Integer.parseInt(tzaArray[i]);
                int offset = Integer.parseInt(tzaArray[i + 1]);
                adjustments.add(new TimeZoneAdjustment(adjustmentTime, offset));
            }
        }

        return new TimeZoneAdjustments(adjustments);
    }
}
