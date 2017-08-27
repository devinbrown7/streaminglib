package com.devinbrown.streaminglib.rtsp;

import com.devinbrown.streaminglib.rtsp.Rtsp.Method;

import java.util.HashMap;
import java.util.Map;

import static com.devinbrown.streaminglib.rtsp.Rtsp.Method.*;

public enum RtspHeader {
    // General
    CACHE_CONTROL("Cache-Control", Type.GENERAL, Requirement.OPT, new Method[]{SETUP}),
    CONNECTION("Connection", Type.GENERAL, Requirement.REQ, Method.values()),
    CSEQ("CSeq", Type.GENERAL, Requirement.REQ, Method.values()),
    DATE("Date", Type.GENERAL, Requirement.OPT, Method.values()),
    VIA("Via", Type.GENERAL, Requirement.OPT, Method.values()),

    // Entity
    CONTENT_BASE("Content-Base", Type.ENTITY, Requirement.OPT, null),
    CONTENT_ENCODING("Content-Encoding", Type.ENTITY, Requirement.REQ, new Method[]{SET_PARAMETER, DESCRIBE, ANNOUNCE}),
    CONTENT_LANGUAGE("Content-Language", Type.ENTITY, Requirement.REQ, new Method[]{DESCRIBE, ANNOUNCE}),
    CONTENT_LENGTH("Content-Length", Type.ENTITY, Requirement.REQ, new Method[]{SET_PARAMETER}),
    CONTENT_LOCATION("Content-Location", Type.ENTITY, Requirement.OPT, null),
    CONTENT_TYPE("Content-Type", Type.ENTITY, Requirement.REQ, new Method[]{SET_PARAMETER, ANNOUNCE}),
    EXPIRES("Expires", Type.ENTITY, Requirement.OPT, new Method[]{DESCRIBE, ANNOUNCE}),
    LAST_MODIFIED("Last-Modified", Type.ENTITY, Requirement.OPT, null),

    // RtspRequest
    ACCEPT("Accept", Type.REQUEST, Requirement.OPT, null),
    ACCEPT_ENCODING("Accept-Encoding", Type.REQUEST, Requirement.OPT, null),
    ACCEPT_LANGUAGE("Accept-Language", Type.REQUEST, Requirement.OPT, Method.values()),
    AUTHORIZATION("Authorization", Type.REQUEST, Requirement.OPT, Method.values()),
    BANDWIDTH("Bandwidth", Type.REQUEST, Requirement.OPT, Method.values()),
    BLOCKSIZE("Blocksize", Type.REQUEST, Requirement.OPT, new Method[]{DESCRIBE, ANNOUNCE, SETUP, PLAY, PAUSE, GET_PARAMETER, SET_PARAMETER, REDIRECT, RECORD}),
    CONFERENCE("Conference", Type.REQUEST, Requirement.OPT, new Method[]{SETUP}),
    FROM("From", Type.REQUEST, Requirement.OPT, Method.values()),
    IF_MODIFIED_SINCE("If-Modified-Since", Type.REQUEST, Requirement.OPT, new Method[]{DESCRIBE, SETUP}),
    PROXY_REQUIRE("Proxy-Require", Type.REQUEST, Requirement.REQ, Method.values()),
    REFERER("Referer", Type.REQUEST, Requirement.OPT, Method.values()),
    REQUIRE("Require", Type.REQUEST, Requirement.REQ, Method.values()),
    USER_AGENT("User-Agent", Type.REQUEST, Requirement.OPT, Method.values()),

    // RtspResponse
    ALLOW("Allow", Type.RESPONSE, Requirement.OPT, Method.values()),
    PUBLIC("Public", Type.RESPONSE, Requirement.OPT, Method.values()),
    RETRY_AFTER("Retry-After", Type.RESPONSE, Requirement.OPT, Method.values()),
    RTP_INFO("RTP-Info", Type.RESPONSE, Requirement.REQ, new Method[]{PLAY}),
    SERVER("Server", Type.RESPONSE, Requirement.OPT, Method.values()),
    UNSUPPORTED("Unsupported", Type.RESPONSE, Requirement.REQ, Method.values()),
    WWW_AUTHENTICATE("WWW-Authenticate", Type.RESPONSE, Requirement.OPT, Method.values()),

    // RtspRequest and RtspResponse
    RANGE("Range", Type.REQUEST_RESPONSE, Requirement.OPT, new Method[]{PLAY, PAUSE, RECORD}),
    SCALE("Scale", Type.REQUEST_RESPONSE, Requirement.OPT, new Method[]{PLAY, RECORD}),
    SESSION("Session", Type.REQUEST_RESPONSE, Requirement.REQ, new Method[]{DESCRIBE, ANNOUNCE, PLAY, PAUSE, TEARDOWN, GET_PARAMETER, SET_PARAMETER, REDIRECT, RECORD}),
    SPEED("Speed", Type.REQUEST_RESPONSE, Requirement.OPT, new Method[]{PLAY}),
    TRANSPORT("Transport", Type.REQUEST_RESPONSE, Requirement.REQ, new Method[]{SETUP});

    enum Type {GENERAL, ENTITY, REQUEST, RESPONSE, REQUEST_RESPONSE}

    enum Requirement {OPT, REQ}

    public String key;
    public Type type;
    public Requirement requirement;
    public Method[] methods;

    private static HashMap<String, RtspHeader> map = new HashMap<>();

    static {
        for (RtspHeader h : RtspHeader.values()) {
            map.put(h.key, h);
        }
    }

    RtspHeader(String k, Type h, Requirement r, Method[] m) {
        key = k;
        type = h;
        requirement = r;
        methods = m;
    }

    public static RtspHeader fromKey(String s) {
        RtspHeader match = null;
        for (Map.Entry<String, RtspHeader> h : map.entrySet()) {
            if (s.equals(h.getKey())) {
                match = h.getValue();
                break;
            }
        }
        return match;
    }
}
