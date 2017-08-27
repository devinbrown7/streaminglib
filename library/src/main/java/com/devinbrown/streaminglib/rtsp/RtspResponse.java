package com.devinbrown.streaminglib.rtsp;

import java.io.IOException;
import java.io.InputStream;

import static com.devinbrown.streaminglib.Constants.CRLF;

/**
 * Rtsp RtspResponse Message
 */
public final class RtspResponse extends RtspMessage {

    private String rtspVersion;
    private RtspStatus statusCode;

    private RtspResponse() {

    }

    /**
     * Parses Rtsp RtspResponse Message coming through a socket via BufferedReader
     *
     * @param i InputStream attached to socket
     * @return Rtsp RtspResponse read from socket
     */
    public static RtspResponse parseResponse(InputStream i) throws IOException {
        RtspResponse r = new RtspResponse();
        r.parseMessage(i);
        return r;
    }

    public RtspStatus getStatus() {
        return statusCode;
    }

    /**
     * Specification: RTSP-Version SP Status-Code SP Reason-Phrase CRLF
     * Reference: https://tools.ietf.org/html/rfc2326#section-7.1
     *
     * @return String representation of the Status-Line
     */
    @Override
    String getFirstLine() {
        StringBuilder sb = new StringBuilder();

        // RTSP-Version
        sb.append(rtspVersion);

        // SP
        sb.append(" ");

        // Status-Code
        sb.append(statusCode.code);

        // SP
        sb.append(" ");

        // Reason-Phrase
        sb.append(statusCode.reasonPhrase);

        // CRLF
        sb.append(CRLF);

        return sb.toString();
    }

    @Override
    void parseFirstLine(String f) {
        String[] statusLineArray = f.split(" ");

        // RTSP Version
        String rtspVersion = statusLineArray[0];
        if (rtspVersion != null) {
            this.rtspVersion = rtspVersion;
        }

        // RTSP RtspResponse Status-Code
        String statusCodeString = statusLineArray[1];
        if (statusCodeString != null) {
            int statusCode = Integer.parseInt(statusCodeString);
            this.statusCode = RtspStatus.map.get(statusCode);
        }
    }
}
