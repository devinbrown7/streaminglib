package com.devinbrown.streaminglib.rtsp;

import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;

import com.devinbrown.streaminglib.RtspClientStreamEvent;
import com.devinbrown.streaminglib.media.MediaFormatHelper;
import com.devinbrown.streaminglib.media.RtpMedia;
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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class RtspClient {
    private static final String TAG = "RtspClient";

    private List<Session> sessions = new ArrayList<>();

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

        private EventBus eventBus;
        private URI uri;
        private int cSeq;
        private RtspInputListener rtspInputListener;

        private List<Rtsp.Method> supportedMethods = new ArrayList<>();
        private List<RtpClientStream> streams = new ArrayList<>();
        private SparseArray<RtspClientEvent.SendRequest> pastRequests = new SparseArray<>();

        /**
         * Initiates an RTSP session with the provided URI
         *
         * @param event RtspClientConnectionRequestEvent
         * @throws IOException The session could not be made with the provided URI
         */
        Session(RtspClientStreamEvent.ConnectionRequest event) throws IOException {
            eventBus = event.eventBus;
            uri = event.uri;
            cSeq = 0;
            supportedMethods = new ArrayList<>();

            mSocket = new Socket(uri.getHost(), uri.getPort());
            mInput = mSocket.getInputStream();
            mOutput = mSocket.getOutputStream();
            eventBus.register(this);

            new Thread(new RtspInputListener()).start();
        }

        /**
         * RTSP OPTIONS
         * Gets available streams from the RTSP server
         */
        private void getAvailableMethods() {
            RtspRequest r = RtspRequest.buildOptionsRequest(++cSeq, uri);
            eventBus.post(new RtspClientEvent.SendRequest(r));
        }

        /**
         * RTSP DESCRIBE
         * Gets available streams from the RTSP server
         */
        private void getAvailableStreams() {
            RtspRequest r = RtspRequest.buildDescribeRequest(++cSeq, uri);
            eventBus.post(new RtspClientEvent.SendRequest(r));
        }

        /**
         * RTSP SETUP
         * Setup stream
         */
        private void setupStream(RtpStream.RtpProtocol p, RtpMedia m) {
            try {
                RtpClientStream s = initializeRtpClientStream(p, m);
                streams.add(s);
                RtspRequest r = RtspRequest.buildSetupRequest(++cSeq, m.uri, s);
                eventBus.post(new RtspClientEvent.SendRequest(r, s));
            } catch (SocketException | URISyntaxException e) {
                eventBus.post(new RtspClientStreamEvent.Exception(e));
            }
        }

        /**
         * RTSP PLAY (Stream control)
         * Play the stream
         */
        private void playStream(RtpClientStream s) {
            RtspRequest r = RtspRequest.buildPlayRequest(++cSeq, s.media.uri, s);
            eventBus.post(new RtspClientEvent.SendRequest(r));
        }

        /**
         * RTSP PLAY (Aggregate control)
         * Play the stream
         */
        private void playStream() {
            RtspRequest r = RtspRequest.buildPlayRequest(++cSeq, uri, null);
            eventBus.post(new RtspClientEvent.SendRequest(r));
        }

        /**
         * RTSP PAUSE (Stream control)
         * Pause the stream
         */
        private void pauseStream(RtpClientStream s) {
            RtspRequest r = RtspRequest.buildPauseRequest(++cSeq, s.media.uri, s);
            eventBus.post(new RtspClientEvent.SendRequest(r));
        }

        /**
         * RTSP PAUSE (Aggregate control)
         * Pause the stream
         */
        private void pauseStream() {
            RtspRequest r = RtspRequest.buildPauseRequest(++cSeq, uri, null);
            eventBus.post(new RtspClientEvent.SendRequest(r));
        }

        /**
         * RTSP TEARDOWN (Stream control)
         * Stops the stream
         */
        private void teardownStream(RtpClientStream s) {
            RtspRequest r = RtspRequest.buildTeardownRequest(++cSeq, s.media.uri, s);
            streams.remove(s);
            eventBus.post(new RtspClientEvent.SendRequest(r));
        }

        /**
         * RTSP TEARDOWN (Aggregate control)
         * Stops the stream
         */
        private void teardownStream() {
            RtspRequest r = RtspRequest.buildTeardownRequest(++cSeq, uri, null);
            streams.clear();
            eventBus.post(new RtspClientEvent.SendRequest(r));
        }

        // External EventBus handlers

        @Subscribe(threadMode = ThreadMode.ASYNC)
        public void handleEvent(RtspClientStreamEvent.SetupStreamRequest event) {
            setupStream(event.rtpProtocol, event.media);
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
        public void handleEvent(RtspClientEvent.SendRequest event) {
            Log.d(TAG, "SEND REQUEST:\n" + event.rtspRequest.toString());
            try {
                pastRequests.append(event.rtspRequest.getCseq(), event);
                sendRtspMessage(event.rtspRequest);
            } catch (IOException e) {
                Log.e(TAG, "Problem sending RTSP message: " + e.getMessage());
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
        public void handleEvent(RtspClientEvent.ReceivedResponse event) {
            if (event.rtspRequest != null) {
                RtspStatus status = event.rtspResponse.getStatus();
                if (status == RtspStatus.OK) {
                    handleRtspResponse(event);
                } else {
                    handleNonOkResponse(event);
                }
            } else {
                Log.e(TAG, "handleRtspResponse: No Request, don't know how to interpret this message");
                Log.e(TAG, event.rtspResponse.toString());
            }
        }

        private void handleRtspResponse(RtspClientEvent.ReceivedResponse event) {
            switch (event.rtspRequest.getMethod()) {
                case OPTIONS:
                    handleOptionsResponse(event.rtspResponse);
                    break;
                case DESCRIBE:
                    handleDescribeResponse(event.rtspRequest, event.rtspResponse);
                    break;
                case ANNOUNCE:
                    handleAnnounceResponse(event.rtspResponse);
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
                    handleTeardownResponse(event.rtspResponse);
                    break;
                case GET_PARAMETER:
                    handleGetParameterResponse(event.rtspResponse);
                    break;
                case SET_PARAMETER:
                    handleSetParameterResponse(event.rtspResponse);
                    break;
                case REDIRECT:
                    handleRedirectResponse(event.rtspResponse);
                    break;
                case RECORD:
                    handleRecordResponse(event.rtspResponse);
                    break;
                case INTERLEAVED_DATA:
                    handleInterleavedData(event.rtspRequest);
                    break;
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
            eventBus.post(new RtspClientEvent.UpdatedMethods(this));
        }

        private void handleDescribeResponse(RtspRequest req, RtspResponse res) {
            try {
                SessionDescription sd = SessionDescription.fromString(res.body);
                URI baseUri = extractBaseUri(req, res);
                RtpMedia[] media = MediaFormatHelper.parseSdp(baseUri, sd);
                eventBus.post(new RtspClientStreamEvent.ConnectionResponse(media));
            } catch (URISyntaxException e) {
                eventBus.post(new RtspClientStreamEvent.Exception(e));
            }
        }

        /**
         * Looks for the base URI
         * <p>
         * https://tools.ietf.org/html/rfc2326#appendix-C.1.1
         *
         * @param res RtspResponse
         * @return Base URI
         */
        private URI extractBaseUri(RtspRequest req, RtspResponse res) throws URISyntaxException {
            URI baseUri = null;

            // 1. The RTSP Content-Base field
            String contentBase = res.getContentBase();
            if (contentBase != null) baseUri = new URI(res.getContentBase());

            // 2. The RTSP Content-Location field
            String contentLocation = res.getContentLocation();
            if (baseUri == null && contentLocation != null) {
                baseUri = new URI(res.getContentLocation());
            }

            // 3. The RTSP request URL
            if (baseUri == null) baseUri = req.getUri();

            return baseUri;
        }

        private void handleAnnounceResponse(RtspResponse r) {
        }

        private void handleSetupResponse(RtspResponse r, RtpClientStream s) {
            configureRtpClientStream(s, r.getSession().sessionId, TransportHeader.fromString(r.getTransport()));
            eventBus.post(new RtspClientStreamEvent.SetupStreamResponse(s));
        }

        private void handlePlayResponse(RtspResponse r) {
            // TODO: Play RtpClientStream
            RtpClientStream s = null;
            eventBus.post(new RtspClientStreamEvent.PlayStreamResponse(s));
        }

        private void handlePauseResponse(RtspResponse r) {
            // TODO: Pause RtpClientStream
            RtpClientStream s = null;
            eventBus.post(new RtspClientStreamEvent.PauseStreamResponse(s));
        }

        private void handleTeardownResponse(RtspResponse r) {
        }

        private void handleGetParameterResponse(RtspResponse r) {
        }

        private void handleSetParameterResponse(RtspResponse r) {
        }

        private void handleRedirectResponse(RtspResponse r) {
        }

        private void handleRecordResponse(RtspResponse r) {
        }

        private void handleInterleavedData(RtspRequest r) {
            Log.d(TAG, "handleInterleavedData");
        }

        private void handleNonOkResponse(RtspClientEvent.ReceivedResponse event) {
            // TODO: better error messages
            eventBus.post(new RtspClientStreamEvent.StreamNotFound());
        }

        private RtpClientStream initializeRtpClientStream(RtpStream.RtpProtocol p, RtpMedia m) throws SocketException {
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

        private void configureRtpClientStream(RtpClientStream s, String sessionId, TransportHeader t) {
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

        private RtpClientStream getStreamForChannel(int channel) {
            RtpClientStream match = null;
            for (RtpClientStream s : streams) {
                Pair<Integer, Integer> channels = s.getInterleavedRtpChannels();
                if (channels.first == channel && channels.second == channel) {
                    match = s;
                    break;
                }
            }
            return match;
        }

        private class RtspInputListener implements Runnable {
            private static final String TAG = "RtspInputListener";

            @Override
            public void run() {
                Log.d(TAG, "Starting RtspInputListener");
                while (!Thread.interrupted()) {
                    try {
                        // Read Rtsp RtspResponse from socket
                        Rtsp r = Rtsp.parseRtspInput(mInput);
                        if (r == null) {
                            break;
                        } else if (r instanceof RtspRequest) {
                            Log.d(TAG, "RECEIVED RTSP REQUEST: " + r.toString());
                            RtspRequest request = (RtspRequest) r;
                            // TODO: Handle RTSP Request
                        } else if (r instanceof RtspResponse) {
                            Log.d(TAG, "RECEIVED RTSP RESPONSE: " + r.toString());

                            RtspResponse response = (RtspResponse) r;

                            // Match this response to a request
                            int cseq = response.getCseq();
                            RtspClientEvent.SendRequest sendRequest = pastRequests.get(cseq);
                            RtspRequest matchedRequest = sendRequest.rtspRequest;
                            RtpClientStream matchedStream = sendRequest.stream;

                            eventBus.post(new RtspClientEvent.ReceivedResponse(matchedRequest, response, matchedStream));
                        } else if (r instanceof RtspInterleavedData) {
                            Log.d(TAG, "RECEIVED RTSP INTERLEAVED DATA");

                            RtspInterleavedData rtspInterleavedData = (RtspInterleavedData) r;

                            // Determine if RTP or RTCP by the channel
                            RtpClientStream stream = getStreamForChannel(rtspInterleavedData.channel);
                            RtpStream.RtpPacketType type = stream.getTypeByChannel(rtspInterleavedData.channel);
                            switch (type) {
                                case RTP:
                                    stream.streamEventBus.post(new RtspClientEvent.RtpPacketReceived(rtspInterleavedData.data));
                                    break;
                                case RTCP:
                                    stream.streamEventBus.post(new RtspClientEvent.RtcpPacketReceived(rtspInterleavedData.data));
                                    break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                Log.d(TAG, "Stopping RtspInputListener");
            }
        }
    }
}
