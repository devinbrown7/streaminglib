package com.devinbrown.streaminglib.sdp;

/**
 * Reference: https://tools.ietf.org/html/rfc4566#page-25
 * Format: a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
 */

public class Rtpmap {
    public int payloadType;
    public String mimeSubType;
    public int clockRate;
    public int channelCount;

    private Rtpmap(int payloadType, String mimeSubType, int clockRate, int channelCount) {
        this.payloadType = payloadType;
        this.mimeSubType = mimeSubType;
        this.clockRate = clockRate;
        this.channelCount = channelCount;
    }

    public static Rtpmap fromString(String s) throws IllegalArgumentException {
        Rtpmap r = null;

        int payloadType;
        String mimeSubType;
        int clockRate;
        int channelCount = 1; // If channel count is not provided it is assumed to be mono

        String[] sSplit = s.split(" ");
        if (sSplit.length == 2) {
            try {
                payloadType = Integer.parseInt(sSplit[0]);
                String encoding = sSplit[1];
                if (encoding != null && !encoding.isEmpty()) {
                    String[] encodingSplit = encoding.split("/");
                    if (encodingSplit.length >= 2) {
                        mimeSubType = encodingSplit[0];
                        clockRate = Integer.parseInt(encodingSplit[1]);
                        if (encodingSplit.length >= 3) {
                            channelCount = Integer.parseInt(encodingSplit[2]);
                        }
                        r = new Rtpmap(payloadType, mimeSubType, clockRate, channelCount);
                    }
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }

        return r;
    }
}
