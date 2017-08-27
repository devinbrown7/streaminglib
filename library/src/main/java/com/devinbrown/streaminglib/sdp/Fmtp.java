package com.devinbrown.streaminglib.sdp;

/**
 * Reference: https://tools.ietf.org/html/rfc4566#page-29
 * Format: a=fmtp:<format> <format specific parameters>
 */

public class Fmtp {
    public int payloadType;
    public String formatSpecificParams;

    private Fmtp(int payloadType, String formatSpecificParams) {
        this.payloadType = payloadType;
        this.formatSpecificParams = formatSpecificParams;
    }

    public static Fmtp fromString(String s) throws IllegalArgumentException {
        Fmtp f = null;

        int payloadType;
        String formatSpecificParams;

        String[] sSplit = s.split(" ");
        if (sSplit.length == 2) {
            try {
                payloadType = Integer.parseInt(sSplit[0]);
                formatSpecificParams = sSplit[1];
                f = new Fmtp(payloadType, formatSpecificParams);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }

        return f;
    }
}
