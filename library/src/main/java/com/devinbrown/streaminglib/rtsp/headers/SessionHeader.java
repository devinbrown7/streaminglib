package com.devinbrown.streaminglib.rtsp.headers;

import android.util.Log;

import com.devinbrown.streaminglib.rtp.RtpStream;

/**
 * Created by devinbrown on 9/2/17.
 */

public class SessionHeader {
    private static final String TAG = "SessionHeader";

    public String sessionId;
    public Integer timeout;

    public SessionHeader(String sessionId, Integer timeout) {
        this.sessionId = sessionId;
        this.timeout = timeout;
    }

    public static SessionHeader fromString(String string) {
        SessionHeader s = null;
        String sessionId;
        Integer timeout = null;

        String[] sessionArray = string.split(";");

        if (sessionArray.length >= 2) {
            sessionId = sessionArray[0].trim();
            String trimmedTimeoutString = sessionArray[1].trim();
            String[] trimmedTimeoutArray = trimmedTimeoutString.split("=");

            if (trimmedTimeoutArray.length == 2) {
                try {
                    timeout = Integer.parseInt(trimmedTimeoutArray[1]);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Problem parsing int <" + trimmedTimeoutString + ">: " + e.getMessage());
                }
            }

            s = new SessionHeader(sessionId, timeout);
        }

        return s;
    }

    public static SessionHeader fromRtpSession(RtpStream s) {
        return new SessionHeader(s.getSessionId(), s.getTimeout());
    }
}
