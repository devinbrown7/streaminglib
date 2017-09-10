package com.devinbrown.streaminglib.rtsp;

import android.net.Uri;
import android.util.Log;
import android.util.Pair;

import com.devinbrown.streaminglib.RtspClientStreamEvent;
import com.devinbrown.streaminglib.media.MediaFormatHelper;
import com.devinbrown.streaminglib.media.RtpMedia;
import com.devinbrown.streaminglib.rtp.RtpClientStream;
import com.devinbrown.streaminglib.rtp.RtpStream;
import com.devinbrown.streaminglib.rtsp.headers.TransportHeader;
import com.devinbrown.streaminglib.sdp.SessionDescription;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class RtspClientSession extends RtspSession {
    private static final String TAG = "RtspClientSession";

    /**
     * Initiates an RTSP session with the provided URI
     *
     * @param event RtspClientConnectionRequestEvent
     * @throws IOException The session could not be made with the provided URI
     */
    RtspClientSession(RtspClientStreamEvent.ConnectionRequest event) throws IOException {
        eventBus = event.eventBus;
        uri = event.uri;
        cSeq = 0;
        int port = uri.getPort();
        if (port < 0) port = DEFAULT_PORT;
        socket = new Socket(uri.getHost(), port);
        input = socket.getInputStream();
        output = socket.getOutputStream();
        eventBus.register(this);

        new Thread(new RtspInputListener()).start();
    }

    @Override
    RtpStream.StreamType getStreamType() {
        return RtpStream.StreamType.CLIENT;
    }

    @Override
    RtpStream initializeRtpStream(RtpStream.RtpProtocol p, RtpMedia m) throws SocketException {
        RtpClientStream s = new RtpClientStream(m);
        switch (p) {
            case UDP:
                s.initializeUdp();
                break;
            case TCP:
                s.initializeTcp(getNewInterleavedChannels());
                break;
        }
        return s;
    }

    @Override
    void configureRtpStream(RtpStream s, String sessionId, TransportHeader t) {
        s.setSessionId(sessionId);
        switch (s.getRtpProtocol()) {
            case UDP:
                s.configureUdp(t.serverRtpPorts);
                break;
            case TCP:
                s.configureTcp();
                break;
        }
    }

    @Override
    void handleOptionsRequest(RtspRequest r) {
        Log.d(TAG, "handleOptionsRequest");
    }

    @Override
    void handleDescribeRequest(RtspRequest r) {
        Log.d(TAG, "handleDescribeRequest");
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

    // External EventBus handlers

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void handleEvent(RtspClientStreamEvent.SetupStreamRequest event) {
        sendRtspSetupRequest(event.rtpProtocol, event.media);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void handleEvent(RtspClientStreamEvent.PlayStreamRequest event) {
        sendRtspPlayRequest(event.stream);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void handleEvent(RtspClientStreamEvent.PauseStreamRequest event) {
        sendRtspPauseRequest(event.stream);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void handleEvent(RtspClientStreamEvent.StopStreamRequest event) {
        sendRtspTeardownRequest(event.stream);
    }

    @Override
    void handleOptionsResponse(RtspResponse r) {
        List<Rtsp.Method> methods = new ArrayList<>();
        String[] optionsArray = r.getOptions();
        if (optionsArray != null && optionsArray.length >= 1) {
            for (String s : optionsArray) {
                methods.add(Rtsp.Method.valueOf(s));
            }
        }
        supportedMethods = methods;
        eventBus.post(new RtspSessionEvent.UpdatedMethods(this));
    }

    @Override
    void handleDescribeResponse(RtspRequest req, RtspResponse res) {
        try {
            SessionDescription sd = SessionDescription.fromString(res.body);
            Uri baseUri = extractBaseUri(req, res);
            RtpMedia[] media = MediaFormatHelper.parseSdp(baseUri, sd);
            eventBus.post(new RtspClientStreamEvent.ConnectionResponse(media));
        } catch (URISyntaxException e) {
            eventBus.post(new RtspClientStreamEvent.Exception(e));
        }
    }

    @Override
    void handleAnnounceResponse(RtspResponse r) {
    }

    @Override
    void handleSetupResponse(RtspResponse r, RtpStream s) {
        RtpClientStream stream = (RtpClientStream) s;
        configureRtpStream(stream, r.getSession().sessionId, TransportHeader.fromString(r.getTransport()));
        eventBus.post(new RtspClientStreamEvent.SetupStreamResponse(stream));
    }

    @Override
    void handlePlayResponse(RtspResponse r) {
        // TODO: Play RtpClientStream
        RtpClientStream s = null;
        eventBus.post(new RtspClientStreamEvent.PlayStreamResponse(s));
    }

    @Override
    void handlePauseResponse(RtspResponse r) {
        // TODO: Pause RtpClientStream
        RtpClientStream s = null;
        eventBus.post(new RtspClientStreamEvent.PauseStreamResponse(s));
    }

    @Override
    void handleTeardownResponse(RtspResponse r) {
    }

    @Override
    void handleGetParameterResponse(RtspResponse r) {
    }

    @Override
    void handleSetParameterResponse(RtspResponse r) {
    }

    @Override
    void handleRedirectResponse(RtspResponse r) {
    }

    @Override
    void handleRecordResponse(RtspResponse r) {
    }

    @Override
    void handleInterleavedData(RtspRequest r) {
        Log.d(TAG, "handleInterleavedData");
    }

    @Override
    void handleNonOkResponse(RtspSessionEvent.ReceivedResponse event) {
        // TODO: better error messages
        eventBus.post(new RtspClientStreamEvent.StreamNotFound());
    }

    private Pair<Integer, Integer> getNewInterleavedChannels() {
        // Determine which RTP channels are used
        List<Integer> takenRtpChannels = new ArrayList<>();
        for (RtpStream s : streams) {
            if (s.getRtpProtocol() == RtpStream.RtpProtocol.TCP) {
                Pair<Integer, Integer> i = s.getInterleavedRtpChannels();
                if (i != null) takenRtpChannels.add(i.first);
            }
        }

        // Determine the lowest available RTP channels
        Integer rtpChannel = null, rtcpChannel = null;
        for (int i = 0; i < Integer.MAX_VALUE; i += 2) {
            if (!takenRtpChannels.contains(i)) {
                rtpChannel = i;
                rtcpChannel = i + 1;
            }
        }
        return new Pair<>(rtpChannel, rtcpChannel);
    }
}
