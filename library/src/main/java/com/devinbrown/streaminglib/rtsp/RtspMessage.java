package com.devinbrown.streaminglib.rtsp;

import android.util.Log;

import com.devinbrown.streaminglib.Utils;
import com.devinbrown.streaminglib.rtsp.headers.SessionHeader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.devinbrown.streaminglib.Constants.CRLF;

/**
 * Generic RTSP Message
 */
public abstract class RtspMessage extends Rtsp {
    private static final String TAG = "RtspMessage";

    /* RtspRequest Line (RtspRequest) or Status Line (RtspResponse) */
    String firstLine = null;

    /* General RtspHeader */
    RtspHeaders generalHeader = new RtspHeaders();

    /* RtspRequest or RtspResponse RtspHeader */
    RtspHeaders messageHeader = new RtspHeaders();

    /* Entity RtspHeader */
    RtspHeaders entityHeader = new RtspHeaders();

    /* Message body*/
    String body = "";

    /* Interleaved Binary Data */
    byte[] data;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Add first line
        sb.append(getFirstLine());

        // Add headers
        sb.append(headerToString(generalHeader));
        sb.append(headerToString(messageHeader));
        sb.append(headerToString(entityHeader));

        // Add body
        if (body != null) {
            sb.append(body).append(CRLF);
        }

        return sb.toString();
    }

    public byte[] getBytes() {
        return toString().getBytes();
    }

    abstract String getFirstLine();

//    private void setGeneralHeaderAttribute(RtspHeader h, String v) {
//        insertHeaderAttribute(generalHeader, h, v);
//    }
//
//    private void setMessageHeaderAttribute(RtspHeader h, String v) {
//        insertHeaderAttribute(messageHeader, h, v);
//    }
//
//    private void setEntityHeaderAttribute(RtspHeader h, String v) {
//        insertHeaderAttribute(entityHeader, h, v);
//    }

    /**
     * Gets the values for the given header type
     *
     * @param h
     * @return
     */
    public List<String> getHeaderValues(RtspHeader h) {
        RtspHeaders headers = getHeadersForType(h.type);
        return headers.get(h);
    }

    // Public methods

    public void setOptions(Rtsp.Method[] methods) {
        insertHeaderAttribute(RtspHeader.PUBLIC, Utils.join(methods, ", "));
    }

    public String[] getOptions() {
        List<String> optionsList = new ArrayList<>();
        List<String> values = getHeaderValues(RtspHeader.PUBLIC);

        if (values != null && !values.isEmpty()) {
            String optionsString = values.get(0);
            String[] optionsArray = optionsString.split(",");
            for (String s : optionsArray) {
                optionsList.add(s.trim());
            }
        }

        return optionsList.toArray(new String[optionsList.size()]);
    }

    public void setCseq(int cseq) {
        insertHeaderAttribute(RtspHeader.CSEQ, String.valueOf(cseq));
    }

    public Integer getCseq() {
        Integer cseq = null;
        List<String> values = getHeaderValues(RtspHeader.CSEQ);
        if (values != null && !values.isEmpty()) {
            cseq = Integer.parseInt(values.get(0));
        }
        return cseq;
    }

    public String getContentBase() {
        String contentBase = null;
        List<String> values = getHeaderValues(RtspHeader.CONTENT_BASE);
        if (values != null && !values.isEmpty()) {
            contentBase = values.get(0);
        }
        return contentBase;
    }

    public String getContentLocation() {
        String contentLocation = null;
        List<String> values = getHeaderValues(RtspHeader.CONTENT_LOCATION);
        if (values != null && !values.isEmpty()) {
            contentLocation = values.get(0);
        }
        return contentLocation;
    }

    public String getTransport() {
        String transport = null;
        List<String> values = getHeaderValues(RtspHeader.TRANSPORT);
        if (values != null && !values.isEmpty()) {
            transport = values.get(0);
        }
        return transport;
    }

    public void setSession(SessionHeader s) {
        StringBuilder sb = new StringBuilder();
        sb.append(s.sessionId);
        if (s.timeout != null) {
            // TODO: replace strings with constants
            sb.append("; timeout=").append(s.timeout);
        }
        insertHeaderAttribute(RtspHeader.SESSION, sb.toString());
    }

    // Session ID needs to be parsed from the SessionHeader header entry
    public SessionHeader getSession() {
        SessionHeader s = null;
        List<String> sessionHeaders = getHeaderValues(RtspHeader.SESSION);
        if (sessionHeaders.size() > 0) {
            String sessionHeaderString = sessionHeaders.get(0);
            if (sessionHeaderString != null) {
                s = SessionHeader.fromString(sessionHeaderString);
            }
        }

        return s;
    }

    public void setTransport(String transport) {
        insertHeaderAttribute(RtspHeader.TRANSPORT, transport);
    }

    /**
     * Parses Rtsp Message
     *
     * @param b BufferedReader attached to socket
     * @throws IOException Error reading stream from socket
     */
    void parseMessage(String firstLine, BufferedReader b) throws IOException {
        parseFirstLine(firstLine);
        parseHeaders(b);
        parseBody(b);
    }

    /* Parsing RtspRequest-Line (RtspRequest) or Status-Line (RtspResponse) */
    abstract void parseFirstLine(String f) throws IllegalArgumentException;

    private void parseHeaders(BufferedReader r) throws IOException {
        String line;
        while (r.ready()) {
            line = r.readLine();
            if (line == null || line.isEmpty()) {
                break;
            }

            String[] sSplit = line.split(":", 2);
            if (sSplit.length >= 2) {
                String k = sSplit[0];
                String v = sSplit[1];
                if (k != null && v != null) {
                    v = v.trim();

                    // Determine which header this belongs
                    RtspHeader header = RtspHeader.fromKey(k);
                    if (header != null) {
                        insertHeaderAttribute(header, v);
                    } else {
                        Log.e(TAG, "Unsupported RTSP header attribute: <" + k + "> value: <" + v + ">");
                    }
                }
            }
        }
    }

    private void parseBody(BufferedReader r) throws IOException {
        // Check if there is a body to read
        int contentLength = 0;
        List<String> lengthValuesArray = getHeaderValues(RtspHeader.CONTENT_LENGTH);
        if (lengthValuesArray != null && lengthValuesArray.size() >= 1) {
            String lengthString = lengthValuesArray.get(0);
            if (lengthString != null) {
                contentLength = Integer.valueOf(lengthString.trim());
            }
        }

        // Read the body
        String line;
        while (contentLength > body.length() && r.ready()) {
            line = r.readLine();
            if (line == null || line.isEmpty()) {
                break;
            }
            body += line + CRLF;
        }
    }

    /**
     * Gets the appropriate headers for the given type
     *
     * @param t the given RtspHeader.Type
     * @return appropriate RtspHeaders
     */
    private RtspHeaders getHeadersForType(RtspHeader.Type t) {
        RtspHeaders headers = null;
        switch (t) {
            case GENERAL:
                headers = generalHeader;
                break;
            case ENTITY:
                headers = entityHeader;
                break;
            case REQUEST:
            case RESPONSE:
            case REQUEST_RESPONSE:
                headers = messageHeader;
                break;
        }
        return headers;
    }

    /**
     * Inserts key-value into the given header HashMap
     *
     * @param header Attribute key
     * @param value  Attribute value
     */
    private void insertHeaderAttribute(RtspHeader header, String value) {
        RtspHeaders headers = getHeadersForType(header.type);
        List<String> l = headers.get(header);

        // If this key-value pair does exist yet then create an empty one
        if (l == null) {
            l = new ArrayList<>();
            headers.put(header, l);
        }

        l.add(value);
    }

    /**
     * Converts an Rtsp header HashMap into a string for sending
     *
     * @param h Rtsp Message header
     * @return String representation of the given Rtsp Message header
     */
    private String headerToString(RtspHeaders h) {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<RtspHeader, List<String>> entry : h.entrySet()) {
            RtspHeader k = entry.getKey();
            List<String> v = entry.getValue();
            for (String vv : v) {
                sb.append(k.key).append(": ").append(vv).append(CRLF);
            }
        }
        return sb.toString();
    }
}
