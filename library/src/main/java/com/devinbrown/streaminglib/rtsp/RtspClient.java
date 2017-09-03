package com.devinbrown.streaminglib.rtsp;

import android.media.MediaFormat;
import android.util.Pair;

import com.devinbrown.streaminglib.RtspClientStreamEvent;
import com.devinbrown.streaminglib.media.MediaFormatHelper;
import com.devinbrown.streaminglib.rtp.RtpClientStream;
import com.devinbrown.streaminglib.rtp.RtpStream;
import com.devinbrown.streaminglib.rtsp.headers.TransportHeader;
import com.devinbrown.streaminglib.sdp.SessionDescription;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class RtspClient {
    private static final String TAG = "RtspClient";

    List<Session> sessions = new ArrayList<>();

    private static RtspClient sharedInstance;

    /**
     * Blocking default constructor for Singleton use
     */
    private RtspClient() {
        EventBus.getDefault().register(this);
    }

    /**
     * Get RtspClient singleton
     *
     * @return shared RtspClient instance
     */
    public static RtspClient getDefault() {
        if (sharedInstance == null) {
            sharedInstance = new RtspClient();
        }
        return sharedInstance;
    }

    // Default bus events

    /**
     * RtspClientConnectionRequestEvent handler
     *
     * @param event RtspServerConnection event
     */
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void handleEvent(RtspClientStreamEvent.ConnectionRequest event) {
        try {
            Session s = new Session(event);
            sessions.add(s);
            event.eventBus.post(new RtspClientEvent.SessionConnected(s));
        } catch (IOException e) {
            event.eventBus.post(new RtspClientEvent.ConnectionError(e));
        }
    }

    public class Session extends RtspSession {
        private static final String TAG = "RtspClientSession";

        private EventBus sessionEventBus;
        private URI uri;
        private int cSeq;
        private List<Rtsp.Method> supportedMethods = new ArrayList<>();
        private List<RtpClientStream> streams = new ArrayList<>();

        /**
         * Initiates an RTSP session with the provided URI
         *
         * @param event RtspClientConnectionRequestEvent
         * @throws IOException The session could not be made with the provided URI
         */
        Session(RtspClientStreamEvent.ConnectionRequest event) throws IOException {
            sessionEventBus = event.eventBus;
            uri = event.uri;
            cSeq = 0;
            supportedMethods = new ArrayList<>();

            mSocket = new Socket(uri.getHost(), uri.getPort());
            mInput = mSocket.getInputStream();
            mOutput = mSocket.getOutputStream();
            sessionEventBus.register(this);
        }

        /**
         * RTSP OPTIONS
         * Gets available streams from the RTSP server
         */
        private void getAvailableMethods() {
            RtspRequest r = RtspRequest.buildOptionsRequest(++cSeq, uri);
            sessionEventBus.post(new RtspClientEvent.Request(r));
        }

        /**
         * RTSP DESCRIBE
         * Gets available streams from the RTSP server
         */
        private void getAvailableStreams() {
            RtspRequest r = RtspRequest.buildDescribeRequest(++cSeq, uri);
            sessionEventBus.post(new RtspClientEvent.Request(r));
        }

        /**
         * RTSP SETUP
         * Setup stream
         */
        private void setupStream(RtpStream.RtpProtocol p, MediaFormat f) {
            try {
                RtpClientStream s = initializeRtpClientStream(p, f);
                RtspRequest r = RtspRequest.buildSetupRequest(++cSeq, uri, s);
                sessionEventBus.post(new RtspClientEvent.Request(r, s));
            } catch (SocketException e) {
                sessionEventBus.post(new RtspClientStreamEvent.Exception(e));
            }
        }

        /**
         * RTSP PLAY
         * Play the stream
         */
        private void playStream(RtpClientStream s) {
            RtspRequest r = RtspRequest.buildPlayRequest(++cSeq, uri, s);
            sessionEventBus.post(new RtspClientEvent.Request(r));
        }

        /**
         * RTSP PAUSE
         * Pause the stream
         */
        private void pauseStream(RtpClientStream s) {
            RtspRequest r = RtspRequest.buildPauseRequest(++cSeq, uri, s);
            sessionEventBus.post(new RtspClientEvent.Request(r));
        }

        /**
         * RTSP TEARDOWN
         * Stops the stream
         */
        private void teardownStream(RtpClientStream s) {
            RtspRequest r = RtspRequest.buildPauseRequest(++cSeq, uri, s);
            sessionEventBus.post(new RtspClientEvent.Request(r));
        }

        // External EventBus handlers

        @Subscribe(threadMode = ThreadMode.ASYNC)
        public void handleEvent(RtspClientStreamEvent.SetupStreamRequest event) {
            setupStream(event.rtpProtocol, event.format);
        }

        @Subscribe(threadMode = ThreadMode.ASYNC)
        public void handleEvent(RtspClientStreamEvent.PlayStreamRequest event) {
            playStream(event.stream);
        }

        @Subscribe(threadMode = ThreadMode.ASYNC)
        public void handleEvent(RtspClientStreamEvent.PauseStreamRequest event) {
            pauseStream(event.stream);
        }

        @Subscribe(threadMode = ThreadMode.ASYNC)
        public void handleEvent(RtspClientStreamEvent.StopStreamRequest event) {
            pauseStream(event.stream);
        }

        // Internal EventBus handlers

        @Subscribe(threadMode = ThreadMode.ASYNC)
        public void handleEvent(RtspClientEvent.Request event) {
            try {
                // Send Rtsp Message
                sendRtspMessage(event.rtspRequest);

                // Read Rtsp RtspResponse from socket
                RtspResponse r = RtspResponse.parseResponse(mInput);

                // Post RtspResponse
                sessionEventBus.post(new RtspClientEvent.Response(event.rtspRequest, r, event.stream));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Subscribe(threadMode = ThreadMode.ASYNC)
        public void handleEvent(RtspClientEvent.SessionConnected event) {
            getAvailableMethods();
        }

        @Subscribe(threadMode = ThreadMode.ASYNC)
        public void handleEvent(RtspClientEvent.UpdatedMethods event) {
            getAvailableStreams();
        }

        @Subscribe(threadMode = ThreadMode.ASYNC)
        public void handleEvent(RtspClientEvent.Response event) {
            // Handle Rtsp Server RtspResponse
            RtspStatus status = event.rtspResponse.getStatus();

            if (status != RtspStatus.OK) {
                handleNonOkResponse();
            } else {
                // Handle rtspResponse based on what mode it was responding to
                switch (event.rtspRequest.getMethod()) {
                    case OPTIONS:
                        handleOptionsResponse(event.rtspResponse);
                        break;
                    case DESCRIBE:
                        handleDescribeResponse(event.rtspResponse);
                        break;
                    case ANNOUNCE:
                        break;
                    case SETUP:
                        handleSetupResponse(event.rtspResponse, event.stream);
                        break;
                    case PLAY:
                        handlePlayResponse(event.rtspResponse);
                        break;
                    case PAUSE:
                        handlePauseResponse(event.rtspResponse);
                        break;
                    case TEARDOWN:
                        break;
                    case GET_PARAMETER:
                        break;
                    case SET_PARAMETER:
                        break;
                    case REDIRECT:
                        break;
                    case RECORD:
                        break;
                    case INTERLEAVED_DATA:
                        break;
                }
            }
        }

        // RTSP Response Handlers

        private void handleOptionsResponse(RtspResponse r) {
            List<Rtsp.Method> methods = new ArrayList<>();
            String[] optionsArray = r.getOptions();
            if (optionsArray != null && optionsArray.length >= 1) {
                for (String s : optionsArray) {
                    methods.add(Rtsp.Method.valueOf(s));
                }
            }
            supportedMethods = methods;
            sessionEventBus.post(new RtspClientEvent.UpdatedMethods(this));
        }

        private void handleDescribeResponse(RtspResponse r) {
            SessionDescription sd = SessionDescription.fromString(r.body);
            MediaFormat[] formats = MediaFormatHelper.parseSdp(sd);
            sessionEventBus.post(new RtspClientStreamEvent.ConnectionResponse(formats));
        }

        private void handleSetupResponse(RtspResponse r, RtpClientStream s) {
            configureRtpClientStream(s, r.getSession().sessionId, TransportHeader.fromString(r.getTransport()));
            sessionEventBus.post(new RtspClientStreamEvent.SetupStreamResponse(s));
        }

        private void handlePlayResponse(RtspResponse r) {
            // TODO: Play RtpClientStream
            RtpClientStream s = null;

            sessionEventBus.post(new RtspClientStreamEvent.PlayStreamResponse(s));
        }

        private void handlePauseResponse(RtspResponse r) {
            // TODO: Pause RtpClientStream
            RtpClientStream s = null;

            sessionEventBus.post(new RtspClientStreamEvent.PauseStreamResponse(s));
        }

        private void handleNonOkResponse() {
            sessionEventBus.post(new RtspClientStreamEvent.StreamNotFound());
        }

        private RtpClientStream initializeRtpClientStream(RtpStream.RtpProtocol p, MediaFormat f) throws SocketException {
            RtpClientStream s = new RtpClientStream(f);
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

        private void configureRtpClientStream(RtpClientStream s, String sessionId, TransportHeader t) {
            switch (s.getRtpProtocol()) {
                case UDP:
                    s.configureUdp(t.serverRtpPorts);
                    break;
                case TCP:
                    s.configureTcp();
                    break;
            }

            s.setSessionId(sessionId);
        }

        private Pair<Integer, Integer> getNewInterleavedChannels() {
            // Determine which RTP channels are used
            List<Integer> takenRtpChannels = new ArrayList<>();
            for (RtpStream s : streams) {
                if (s.getRtpProtocol() == RtpStream.RtpProtocol.TCP) {
                    Pair<Integer, Integer> i = s.getInterleavedRtpChannels();
                    if (i != null) {
                        takenRtpChannels.add(i.first);
                    }
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
}
