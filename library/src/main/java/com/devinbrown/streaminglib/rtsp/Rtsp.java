package com.devinbrown.streaminglib.rtsp;

/**
 * RTSP Version 1.0
 *
 * Specification: https://tools.ietf.org/html/rfc2326
 */

public class Rtsp {
    public enum Method {OPTIONS, DESCRIBE, ANNOUNCE, SETUP, PLAY, PAUSE, TEARDOWN, GET_PARAMETER, SET_PARAMETER, REDIRECT, RECORD, INTERLEAVED_DATA}
}
