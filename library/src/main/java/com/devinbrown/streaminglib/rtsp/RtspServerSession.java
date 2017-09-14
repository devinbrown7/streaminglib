package com.devinbrown.streaminglib.rtsp;

import android.util.Log;

import com.devinbrown.streaminglib.Utils;
import com.devinbrown.streaminglib.media.RtpMedia;
import com.devinbrown.streaminglib.rtp.RtpServerStream;
import com.devinbrown.streaminglib.rtp.RtpStream;
import com.devinbrown.streaminglib.rtsp.headers.SessionHeader;
import com.devinbrown.streaminglib.rtsp.headers.TransportHeader;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class RtspServerSession extends RtspSession {
    private static final String TAG = "RtspServerSession";

    private Rtsp.Method[] supportedMethods = new Rtsp.Method[]{
            Rtsp.Method.OPTIONS,
            Rtsp.Method.DESCRIBE,
            Rtsp.Method.SETUP,
            Rtsp.Method.PLAY,
            Rtsp.Method.TEARDOWN};

    private RtspServer rtspServer;

    public RtspServerSession(RtspServer rtspServer, final Socket socket) throws IOException {
        name = "Android RTSP Server";
        this.rtspServer = rtspServer;
        this.socket = socket;
        input = socket.getInputStream();
        output = socket.getOutputStream();
        eventBus = new EventBus();
        eventBus.register(this);

        new Thread(new RtspInputListener()).start();
    }

    @Override
    RtspAuth.AuthParams getAuth() {
        return rtspServer.getAuth();
    }

    @Override
    RtpStream.StreamType getStreamType() {
        return RtpStream.StreamType.SERVER;
    }

    @Override
    RtpStream initializeRtpStream(RtpStream.RtpProtocol p, RtpMedia m, TransportHeader t) throws SocketException {
        RtpServerStream s = new RtpServerStream(m);
        switch (p) {
            case UDP:
                s.initializeUdp();
                break;
            case TCP:
                s.initializeTcp(t.interleavedChannels);
                break;
        }
        return s;
    }

    @Override
    void configureRtpStream(RtpStream s, String sessionId, TransportHeader t) {
        s.setSessionId(sessionId);
        switch (s.getRtpProtocol()) {
            case UDP:
                s.configureUdp(t.clientRtpPorts);
                break;
            case TCP:
                 s.configureTcp();
                break;
        }
    }

    // RTSP Request handlers

    @Override
    void handleOptionsRequest(RtspRequest r) {
        RtspResponse res = RtspResponse.buildOptionsResponse(r, supportedMethods);
        eventBus.post(new RtspSessionEvent.SendResponse(r, res));
    }

    @Override
    void handleDescribeRequest(RtspRequest r) {
        RtspResponse res = RtspResponse.buildDescribeResponse(r, rtspServer.getInputStreams());
        eventBus.post(new RtspSessionEvent.SendResponse(r, res));
    }

    @Override
    void handleAnnounceRequest(RtspRequest r) {
        Log.d(TAG, "handleAnnounceRequest");
    }

    @Override
    void handleSetupRequest(RtspRequest r) {
        // Get the stream that this request is referring to
        String control = r.getUri().getLastPathSegment();
        RtspInputStream input = rtspServer.getRtspServerInputStreamForControl(control);

        try {
            // TODO: Determine protocol: UDP/TCP
            TransportHeader t = TransportHeader.fromString(r.getTransport());
            RtpStream stream = initializeRtpStream(t.rtpProtocol, input.getRtpMedia(), t);
            configureRtpStream(stream, Utils.getNewSsrc(), t);
            RtspResponse res = RtspResponse.buildSetupResponse(r, stream);
            eventBus.post(new RtspSessionEvent.SendResponse(r, res));
        } catch (SocketException e) {
            // TODO: Handle exception
            e.printStackTrace();
        }
    }

    @Override
    void handlePlayRequest(RtspRequest r) {
        Log.d(TAG, "handlePlayRequest");
        SessionHeader session = r.getSession();
        RtspResponse res = RtspResponse.buildPlayResponse(r, session);
        eventBus.post(new RtspSessionEvent.SendResponse(r, res));
    }

    @Override
    void handlePauseRequest(RtspRequest r) {
        Log.d(TAG, "handlePauseRequest");
    }

    @Override
    void handleTeardownRequest(RtspRequest r) {
        Log.d(TAG, "handleTeardownRequest");
        SessionHeader session = r.getSession();
        RtspResponse res = RtspResponse.buildTeardownResponse(r, session);
        eventBus.post(new RtspSessionEvent.SendResponse(r, res));
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

    private RtpStream getStreamFromSession(String sessionId) {
        RtpStream r = null;
        for (RtpStream s : streams) {
            if (s.getSessionId().equalsIgnoreCase(sessionId)) {
                r = s;
            }
        }
        return r;
    }
}
