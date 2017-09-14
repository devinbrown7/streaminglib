package com.devinbrown.streaminglib.rtsp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * RTSP Version 1.0
 * <p>
 * Specification: https://tools.ietf.org/html/rfc2326
 */

public abstract class Rtsp {

    private static final String TAG = "RTSP";

    public enum Method {OPTIONS, DESCRIBE, ANNOUNCE, SETUP, PLAY, PAUSE, TEARDOWN, GET_PARAMETER, SET_PARAMETER, REDIRECT, RECORD, INTERLEAVED_DATA}

    public static Rtsp parseRtspInput(InputStream i) throws IOException {
        Rtsp rtsp = null;

        char firstByte = (char) i.read();

        // Check if message is interleaved binary data
        if (firstByte == '$') {
            Log.d(TAG, "DATA MESSAGE");
            // TODO: Parse RTSP interleaved data
            rtsp = RtspInterleavedData.parseInterleavedData(i);
        } else {
            Log.d(TAG, "RTSP MESSAGE");

            // Get rest of first line
            BufferedReader b = new BufferedReader(new InputStreamReader(i));
            String line = b.readLine();
            String firstLine = firstByte + line;

            if (line != null) {
                // Determine if request or response
                if (firstLine.startsWith("RTSP")) {
                    Log.d(TAG, "RTSP RESPONSE");
                    rtsp = RtspResponse.parseResponse(firstLine, b);
                } else {
                    Log.d(TAG, "RTSP REQUEST");
                    rtsp = RtspRequest.parseRequest(firstLine, b);
                }
            } else {
                Log.d(TAG, "Server Disconnected");
            }
        }

        return rtsp;
    }
}
