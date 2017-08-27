package com.devinbrown.streaminglib.rtsp;

import java.io.BufferedReader;
import java.net.URI;

import static com.devinbrown.streaminglib.Constants.CRLF;

/**
 * Rtsp RtspRequest Message
 */
public final class RtspRequest extends RtspMessage {

    private Rtsp.Method method;
    private URI uri;
    private String version = "RTSP/1.0";

    private RtspRequest() {

    }

    public static RtspRequest parseRequest(BufferedReader b) {
        RtspRequest r = new RtspRequest();
        return r;
    }

    /**
     * Specification: Method SP RtspRequest-URI SP RTSP-Version CRLF
     * Reference: https://tools.ietf.org/html/rfc2326#section-6.1
     *
     * @return String representation of the RtspRequest-Line
     */
    @Override
    String getFirstLine() {
        StringBuilder sb = new StringBuilder();

        // Method
        sb.append(method.name());

        // SP
        sb.append(" ");

        // RtspRequest-URI
        sb.append(uri.getScheme()).append("://").append(uri.getHost()).append(uri.getPath());

        // SP
        sb.append(" ");

        // RTSP-Version
        sb.append(version);

        // CRLF
        sb.append(CRLF);

        return sb.toString();
    }

    @Override
    void parseFirstLine(String f) {

    }

    public static RtspRequest buildOptionsRequest(int cSeq, URI u) {
        RtspRequest r = new RtspRequest();
        r.method = Rtsp.Method.OPTIONS;
        r.uri = u;
        r.setCseq(cSeq);

        return r;
    }

    public static RtspRequest buildDescribeRequest(int cSeq, URI u) {
        RtspRequest r = new RtspRequest();
        r.method = Rtsp.Method.DESCRIBE;
        r.uri = u;
        r.setCseq(cSeq);

        return r;
    }

    public Rtsp.Method getMethod() {
        return method;
    }
}