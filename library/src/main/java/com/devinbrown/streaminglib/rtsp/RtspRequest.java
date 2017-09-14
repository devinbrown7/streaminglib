package com.devinbrown.streaminglib.rtsp;

import android.net.Uri;

import com.devinbrown.streaminglib.rtp.RtpStream;
import com.devinbrown.streaminglib.rtsp.headers.SessionHeader;
import com.devinbrown.streaminglib.rtsp.headers.TransportHeader;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;

import static com.devinbrown.streaminglib.Constants.CRLF;

/**
 * Rtsp RtspRequest Message
 */
public final class RtspRequest extends RtspMessage {
    private static final String TAG = "RtspRequest";
    private Rtsp.Method method;
    private Uri uri;
    private String version = "RTSP/1.0";

    private RtspRequest() {
    }

    public static RtspRequest parseRequest(String firstLine, BufferedReader b) throws IOException {
        RtspRequest r = new RtspRequest();
        r.parseMessage(firstLine, b);
        return r;
    }

    // RTSP values

    public Uri getUri() {
        return uri;
    }

    Rtsp.Method getMethod() {
        return method;
    }

    // RTSP Message methods

    /**
     * Specification: Method SP RtspRequest-URI SP RTSP-Version CRLF
     * Reference: https://tools.ietf.org/html/rfc2326#section-6.1
     *
     * @return String representation of the RtspRequest-Line
     */
    @Override
    String getFirstLine() {
        StringBuilder sb = new StringBuilder();
        sb.append(method.name()).append(" ");
        sb.append(uri.getScheme()).append("://").append(uri.getHost()).append(uri.getPath());
        sb.append(" ").append(version).append(CRLF);
        return sb.toString();
    }

    @Override
    void parseFirstLine(String f) throws IllegalArgumentException {
        String[] requestLineArray = f.split(" ");
        if (requestLineArray.length == 3) {
            method = Rtsp.Method.valueOf(requestLineArray[0]);
            uri = Uri.parse(requestLineArray[1]);
            version = requestLineArray[2];
        } else {
            throw new IllegalArgumentException("RTSP Request has invalid first line: " + f);
        }
    }

    // Requests

    static RtspRequest buildOptionsRequest(int cSeq, Uri u) {
        RtspRequest r = new RtspRequest();
        r.method = Rtsp.Method.OPTIONS;
        r.uri = u;
        r.setCseq(cSeq);
        return r;
    }

    static RtspRequest buildDescribeRequest(int cSeq, Uri u) {
        RtspRequest r = new RtspRequest();
        r.method = Rtsp.Method.DESCRIBE;
        r.uri = u;
        r.setCseq(cSeq);
        return r;
    }

    static RtspRequest buildSetupRequest(int cSeq, Uri u, TransportHeader t) throws URISyntaxException {
        RtspRequest r = new RtspRequest();
        r.method = Rtsp.Method.SETUP;
        r.uri = u;
        r.setCseq(cSeq);
        r.setTransport(t.toString());
        return r;
    }

    static RtspRequest buildPlayRequest(int cSeq, Uri u, RtpStream s) {
        RtspRequest r = new RtspRequest();
        r.method = Rtsp.Method.PLAY;
        r.uri = u;
        r.setCseq(cSeq);
        if (s != null) r.setSession(SessionHeader.fromRtpSession(s));
        // TODO: Set Range header
        return r;
    }

    static RtspRequest buildPauseRequest(int cSeq, Uri u, RtpStream s) {
        RtspRequest r = new RtspRequest();
        r.method = Rtsp.Method.PAUSE;
        r.uri = u;
        r.setCseq(cSeq);
        if (s != null) r.setSession(SessionHeader.fromRtpSession(s));
        return r;
    }

    static RtspRequest buildTeardownRequest(int cSeq, Uri u, RtpStream s) {
        RtspRequest r = new RtspRequest();
        r.method = Rtsp.Method.TEARDOWN;
        r.uri = u;
        r.setCseq(cSeq);
        if (s != null) r.setSession(SessionHeader.fromRtpSession(s));
        return r;
    }
}
