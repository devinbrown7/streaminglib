package com.devinbrown.streaminglib.rtsp.headers;

import android.util.Log;

/**
 * Created by devinbrown on 9/2/17.
 */

public class SessionHeader {
    private static final String TAG = "SessionHeader";

    public String sessionId;
    public Integer timeout;

    private SessionHeader(String sessionId, Integer timeout) {
        this.sessionId = sessionId;
        this.timeout = timeout;
    }

    public static SessionHeader fromString(String string) {
        SessionHeader s = null;
        String sessionId = null;
        Integer timeout = null;


        String[] sessionArray = string.split(";");

        if (sessionArray.length >= 2) {
            sessionId = sessionArray[0].trim();
            String trimmedTimeoutString = sessionArray[1].trim();

            try {
                timeout = Integer.parseInt(trimmedTimeoutString);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Problem parsing int <" + trimmedTimeoutString + ">: " + e.getMessage());
            }

            s = new SessionHeader(sessionId, timeout);
        }

        return s;
    }
}
