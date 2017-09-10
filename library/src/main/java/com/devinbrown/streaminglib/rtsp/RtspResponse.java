package com.devinbrown.streaminglib.rtsp;

import com.devinbrown.streaminglib.rtp.RtpStream;
import com.devinbrown.streaminglib.rtsp.headers.SessionHeader;
import com.devinbrown.streaminglib.rtsp.headers.TransportHeader;
import com.devinbrown.streaminglib.sdp.SessionDescription;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import static com.devinbrown.streaminglib.Constants.CRLF;

/**
 * Rtsp RtspResponse Message
 */
public final class RtspResponse extends RtspMessage {

    private String rtspVersion = "RTSP/1.0";
    private RtspStatus statusCode;

    private RtspResponse() {
    }

    public static RtspResponse parseResponse(String firstLine, BufferedReader b) throws IOException {
        RtspResponse r = new RtspResponse();
        r.parseMessage(firstLine, b);
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
    String getFirstLine() throws IllegalArgumentException {
        StringBuilder sb = new StringBuilder();
        sb.append(rtspVersion).append(" ");
        sb.append(statusCode.code).append(" ");
        sb.append(statusCode.reasonPhrase).append(CRLF);
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

    static RtspResponse buildOptionsResponse(RtspRequest req, Rtsp.Method[] supportedMethods) {
        RtspResponse r = new RtspResponse();
        r.setCseq(req.getCseq());

        r.setOptions(supportedMethods);

        r.statusCode = RtspStatus.OK;
        return r;
    }

    static RtspResponse buildDescribeResponse(RtspRequest req, List<RtspInputStream> inputs) {
        RtspResponse r = new RtspResponse();
        r.setCseq(req.getCseq());

        // Check that the request is accepting an SDP
        if(req.getAccept().equalsIgnoreCase("application/sdp")) {
            // Build the MediaDescription for this server
            SessionDescription sd = SessionDescription.fromRtspServerInputStreams(inputs);
            r.setBodyContent("application/sdp", sd.toString());
        }

        r.statusCode = RtspStatus.OK;
        return r;
    }

    static RtspResponse buildSetupResponse(RtspRequest req, RtpStream s) {
        RtspResponse r = new RtspResponse();
        r.setCseq(req.getCseq());

        // TODO: A timeout should be passed in
        if (s.getSessionId() != null) r.setSession(new SessionHeader(s.getSessionId(), null));
        r.setTransport(TransportHeader.fromRtpStream(s).toString());

        r.statusCode = RtspStatus.OK;
        return r;
    }

    static RtspResponse buildPlayResponse(RtspRequest req, RtpStream s) {
        RtspResponse r = new RtspResponse();
        r.setCseq(req.getCseq());

        if (s != null) r.setSession(SessionHeader.fromRtpSession(s));
        // TODO: Set Range header

        r.statusCode = RtspStatus.OK;
        return r;
    }

    static RtspResponse buildPauseResponse(RtspRequest req, RtpStream s) {
        RtspResponse r = new RtspResponse();
        r.setCseq(req.getCseq());

        if (s != null) r.setSession(SessionHeader.fromRtpSession(s));

        r.statusCode = RtspStatus.OK;
        return r;
    }

    static RtspResponse buildTeardownResponse(RtspRequest req, RtpStream s) {
        RtspResponse r = new RtspResponse();
        r.setCseq(req.getCseq());

        if (s != null) r.setSession(SessionHeader.fromRtpSession(s));

        r.statusCode = RtspStatus.OK;
        return r;
    }
}
