package com.devinbrown.streaminglib.rtsp;

import android.media.MediaFormat;
import android.util.Log;

import com.devinbrown.streaminglib.media.RtpMedia;
import com.devinbrown.streaminglib.rtp.RtpStream;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class RtspServerSession extends RtspSession {
    private static final String TAG = "RtspServerSession";

    private Rtsp.Method[] supportedMethods = new Rtsp.Method[]{
            Rtsp.Method.OPTIONS,
            Rtsp.Method.DESCRIBE,
            Rtsp.Method.SETUP,
            Rtsp.Method.PLAY,
            Rtsp.Method.TEARDOWN};

    /**
     * Constructor
     */
    public RtspServerSession(final Socket s) throws IOException {
        socket = s;
        input = s.getInputStream();
        output = s.getOutputStream();
        eventBus = new EventBus();
        eventBus.register(this);

        new Thread(new RtspInputListener()).start();}

    @Override
    RtpStream.StreamType getStreamType() {
        return RtpStream.StreamType.SERVER;
    }

    @Override
    RtpStream initializeRtpStream(RtpStream.RtpProtocol p, RtpMedia m) throws SocketException {
        Log.d(TAG, "initializeRtpStream");
        return null;
    }

    // RTSP Request handlers

    @Override
    void handleOptionsRequest(RtspRequest r) {
        RtspResponse res = RtspResponse.buildOptionsResponse(r, supportedMethods);
        eventBus.post(new RtspSessionEvent.SendResponse(r, res));
    }

    @Override
    void handleDescribeRequest(RtspRequest r) {
        List<MediaFormat> m = new ArrayList<>();

        for (RtspServerInputStream s : RtspServer.getDefault().getInputStreams()) {
            m.add(s.getFormat());
        }

        RtspResponse res = RtspResponse.buildDescribeResponse(r, m);
        eventBus.post(new RtspSessionEvent.SendResponse(r, res));
    }

    @Override
    void handleAnnounceRequest(RtspRequest r) {
        Log.d(TAG, "handleAnnounceRequest");
    }

    @Override
    void handleSetupRequest(RtspRequest r) {
        Log.d(TAG, "handleSetupRequest");
    }

    @Override
    void handlePlayRequest(RtspRequest r) {
        Log.d(TAG, "handlePlayRequest");
    }

    @Override
    void handlePauseRequest(RtspRequest r) {
        Log.d(TAG, "handlePauseRequest");
    }

    @Override
    void handleTeardownRequest(RtspRequest r) {
        Log.d(TAG, "handleTeardownRequest");
    }

    @Override
    void handleGetParameterRequest(RtspRequest r) {
        Log.d(TAG, "handleGetParameterRequest");
    }

    @Override
    void handleSetParameterRequest(RtspRequest r) {
        Log.d(TAG, "handleSetParameterRequest");
    }

    @Override
    void handleRedirectRequest(RtspRequest r) {
        Log.d(TAG, "handleRedirectRequest");
    }

    @Override
    void handleRecordRequest(RtspRequest r) {
        Log.d(TAG, "handleRecordRequest");
    }

    // RTSP Response handlers

    @Override
    void handleOptionsResponse(RtspResponse r) {
        Log.d(TAG, "handleOptionsResponse");
    }

    @Override
    void handleDescribeResponse(RtspRequest req, RtspResponse res) {
        Log.d(TAG, "handleDescribeResponse");
    }

    @Override
    void handleAnnounceResponse(RtspResponse r) {
        Log.d(TAG, "handleAnnounceResponse");
    }

    @Override
    void handleSetupResponse(RtspResponse r, RtpStream s) {
        Log.d(TAG, "handleSetupResponse");
    }

    @Override
    void handlePlayResponse(RtspResponse r) {
        Log.d(TAG, "handlePlayResponse");
    }

    @Override
    void handlePauseResponse(RtspResponse r) {
        Log.d(TAG, "handlePauseResponse");
    }

    @Override
    void handleTeardownResponse(RtspResponse r) {
        Log.d(TAG, "handleTeardownResponse");
    }

    @Override
    void handleGetParameterResponse(RtspResponse r) {
        Log.d(TAG, "handleGetParameterResponse");
    }

    @Override
    void handleSetParameterResponse(RtspResponse r) {
        Log.d(TAG, "handleSetParameterResponse");
    }

    @Override
    void handleRedirectResponse(RtspResponse r) {
        Log.d(TAG, "handleRedirectResponse");
    }

    @Override
    void handleRecordResponse(RtspResponse r) {
        Log.d(TAG, "handleRecordResponse");
    }

    @Override
    void handleInterleavedData(RtspRequest r) {
        Log.d(TAG, "handleInterleavedData");
    }

    @Override
    void handleNonOkResponse(RtspSessionEvent.ReceivedResponse e) {
        Log.d(TAG, "handleNonOkResponse");
    }
}
