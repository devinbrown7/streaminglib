package com.devinbrown.streaminglib.rtsp;

import android.net.Uri;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;

import com.devinbrown.streaminglib.RtspClientStreamEvent;
import com.devinbrown.streaminglib.UnsupportedRtspMethodException;
import com.devinbrown.streaminglib.media.RtpMedia;
import com.devinbrown.streaminglib.rtp.RtpClientStream;
import com.devinbrown.streaminglib.rtp.RtpStream;
import com.devinbrown.streaminglib.rtsp.headers.TransportHeader;
import com.devinbrown.streaminglib.sdp.SessionDescription;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

abstract class RtspSession {
    private static final String TAG = "RtspSession";

    static final int DEFAULT_PORT = 8554;

    RtspInputListener rtspInputListener;

    List<Rtsp.Method> supportedMethods = new ArrayList<>();
    List<RtpStream> streams = new ArrayList<>();
    SparseArray<RtspSessionEvent.SendRequest> pastRequests = new SparseArray<>();

    InputStream input;
    OutputStream output;
    Socket socket;
    Thread thread;
    EventBus eventBus;
    int cSeq;
    Uri uri;

    SessionDescription sessionDescription;

    abstract RtpStream.StreamType getStreamType();

    abstract RtpStream initializeRtpStream(RtpStream.RtpProtocol p, RtpMedia m) throws SocketException;

    abstract void configureRtpStream(RtpStream s, String sessionId, TransportHeader t);

    private synchronized void sendRtspMessage(RtspMessage r) throws IOException {
        output.write(r.getBytes());
        output.flush();
    }

    /**
     * Reference: https://tools.ietf.org/html/rfc2326#section-10
     */
    private void validateRtspMessage(Rtsp.Method method, RtpStream.StreamType streamType) throws UnsupportedRtspMethodException {
        switch (method) {

            // Supported by Client and Server
            case ANNOUNCE:
            case GET_PARAMETER:
            case OPTIONS:
            case SET_PARAMETER:
                break;

            // Supported by Client
            case DESCRIBE:
            case PAUSE:
            case PLAY:
            case RECORD:
            case SETUP:
            case TEARDOWN:
                if (streamType == RtpStream.StreamType.SERVER) {
                    throw new UnsupportedRtspMethodException("RTSP Method " + method.name() + " cannot be used by " + streamType.name());
                }
                break;

            // Supported by Server
            case REDIRECT:
                if (streamType == RtpStream.StreamType.CLIENT) {
                    throw new UnsupportedRtspMethodException("RTSP Method " + method.name() + " cannot be used by " + streamType.name());
                }
                break;
        }
    }

    /**
     * RTSP OPTIONS
     * Gets available methods
     */
    private void sendRtspOptionsRequest() {
        validateRtspMessage(Rtsp.Method.OPTIONS, getStreamType());
        RtspRequest r = RtspRequest.buildOptionsRequest(++cSeq, uri);
        eventBus.post(new RtspSessionEvent.SendRequest(r));
    }

    /**
     * RTSP DESCRIBE
     * Gets available streams
     */
    private void sendRtspDescribeRequest() {
        validateRtspMessage(Rtsp.Method.DESCRIBE, getStreamType());
        RtspRequest r = RtspRequest.buildDescribeRequest(++cSeq, uri);
        eventBus.post(new RtspSessionEvent.SendRequest(r));
    }

    /**
     * RTSP SETUP
     * Setup stream
     */
    void sendRtspSetupRequest(RtpStream.RtpProtocol p, RtpMedia m) {
        validateRtspMessage(Rtsp.Method.SETUP, getStreamType());
        try {
            RtpStream s = initializeRtpStream(p, m);
            streams.add(s);
            RtspRequest r = RtspRequest.buildSetupRequest(++cSeq, m.uri, s);
            eventBus.post(new RtspSessionEvent.SendRequest(r, s));
        } catch (SocketException | URISyntaxException e) {
            eventBus.post(new RtspClientStreamEvent.Exception(e));
        }
    }

    /**
     * RTSP PLAY (Stream control)
     * Play the stream
     */
    void sendRtspPlayRequest(RtpClientStream s) {
        validateRtspMessage(Rtsp.Method.PLAY, getStreamType());
        RtspRequest r = RtspRequest.buildPlayRequest(++cSeq, s.getRtpMedia().uri, s);
        eventBus.post(new RtspSessionEvent.SendRequest(r));
    }

    /**
     * RTSP PLAY (Aggregate control)
     * Play the stream
     */
    private void sendRtspPlayRequest() {
        validateRtspMessage(Rtsp.Method.PLAY, getStreamType());
        RtspRequest r = RtspRequest.buildPlayRequest(++cSeq, uri, null);
        eventBus.post(new RtspSessionEvent.SendRequest(r));
    }

    /**
     * RTSP PAUSE (Stream control)
     * Pause the stream
     */
    void sendRtspPauseRequest(RtpClientStream s) {
        validateRtspMessage(Rtsp.Method.PAUSE, getStreamType());
        RtspRequest r = RtspRequest.buildPauseRequest(++cSeq, s.getRtpMedia().uri, s);
        eventBus.post(new RtspSessionEvent.SendRequest(r));
    }

    /**
     * RTSP PAUSE (Aggregate control)
     * Pause the stream
     */
    private void sendRtspPauseRequest() {
        validateRtspMessage(Rtsp.Method.PAUSE, getStreamType());
        RtspRequest r = RtspRequest.buildPauseRequest(++cSeq, uri, null);
        eventBus.post(new RtspSessionEvent.SendRequest(r));
    }

    /**
     * RTSP TEARDOWN (Stream control)
     * Stops the stream
     */
    void sendRtspTeardownRequest(RtpClientStream s) {
        validateRtspMessage(Rtsp.Method.TEARDOWN, getStreamType());
        RtspRequest r = RtspRequest.buildTeardownRequest(++cSeq, s.getRtpMedia().uri, s);
        streams.remove(s);
        eventBus.post(new RtspSessionEvent.SendRequest(r));
    }

    /**
     * RTSP TEARDOWN (Aggregate control)
     * Stops the stream
     */
    private void sendRtspTeardownRequest() {
        validateRtspMessage(Rtsp.Method.TEARDOWN, getStreamType());
        RtspRequest r = RtspRequest.buildTeardownRequest(++cSeq, uri, null);
        streams.clear();
        eventBus.post(new RtspSessionEvent.SendRequest(r));
    }

    // Internal EventBus handlers

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void handleEvent(RtspSessionEvent.SendRequest event) {
        Log.d(TAG, "SEND REQUEST:\n" + event.rtspRequest.toString());
        try {
            pastRequests.append(event.rtspRequest.getCseq(), event);
            sendRtspMessage(event.rtspRequest);
        } catch (IOException e) {
            Log.e(TAG, "Problem sending RTSP REQUEST: " + e.getMessage());
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void handleEvent(RtspSessionEvent.SendResponse event) {
        Log.d(TAG, "SEND RESPONSE:\n" + event.rtspResponse.toString());
        try {
            sendRtspMessage(event.rtspResponse);
        } catch (IOException e) {
            Log.e(TAG, "Problem sending RTSP RESPONSE: " + e.getMessage());
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void handleEvent(RtspSessionEvent.SessionConnected event) {
        sendRtspOptionsRequest();
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void handleEvent(RtspSessionEvent.UpdatedMethods event) {
        sendRtspDescribeRequest();
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void handleEvent(RtspSessionEvent.ReceivedRequest event) {
        switch (event.rtspRequest.getMethod()) {
            case OPTIONS:
                handleOptionsRequest(event.rtspRequest);
                break;
            case DESCRIBE:
                handleDescribeRequest(event.rtspRequest);
                break;
            case ANNOUNCE:
                handleAnnounceRequest(event.rtspRequest);
                break;
            case SETUP:
                handleSetupRequest(event.rtspRequest);
                break;
            case PLAY:
                handlePlayRequest(event.rtspRequest);
                break;
            case PAUSE:
                handlePauseRequest(event.rtspRequest);
                break;
            case TEARDOWN:
                handleTeardownRequest(event.rtspRequest);
                break;
            case GET_PARAMETER:
                handleGetParameterRequest(event.rtspRequest);
                break;
            case SET_PARAMETER:
                handleSetParameterRequest(event.rtspRequest);
                break;
            case REDIRECT:
                handleRedirectRequest(event.rtspRequest);
                break;
            case RECORD:
                handleRecordRequest(event.rtspRequest);
                break;
            case INTERLEAVED_DATA:
                handleInterleavedData(event.rtspRequest);
                break;
        }
    }

    // RTSP Request handlers
    abstract void handleOptionsRequest(RtspRequest r);

    abstract void handleDescribeRequest(RtspRequest r);

    abstract void handleAnnounceRequest(RtspRequest r);

    abstract void handleSetupRequest(RtspRequest r);

    abstract void handlePlayRequest(RtspRequest r);

    abstract void handlePauseRequest(RtspRequest r);

    abstract void handleTeardownRequest(RtspRequest r);

    abstract void handleGetParameterRequest(RtspRequest r);

    abstract void handleSetParameterRequest(RtspRequest r);

    abstract void handleRedirectRequest(RtspRequest r);

    abstract void handleRecordRequest(RtspRequest r);

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void handleEvent(RtspSessionEvent.ReceivedResponse event) {
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

    private void handleRtspResponse(RtspSessionEvent.ReceivedResponse event) {
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

    abstract void handleOptionsResponse(RtspResponse r);

    abstract void handleDescribeResponse(RtspRequest req, RtspResponse res);

    abstract void handleAnnounceResponse(RtspResponse r);

    abstract void handleSetupResponse(RtspResponse r, RtpStream s);

    abstract void handlePlayResponse(RtspResponse r);

    abstract void handlePauseResponse(RtspResponse r);

    abstract void handleTeardownResponse(RtspResponse r);

    abstract void handleGetParameterResponse(RtspResponse r);

    abstract void handleSetParameterResponse(RtspResponse r);

    abstract void handleRedirectResponse(RtspResponse r);

    abstract void handleRecordResponse(RtspResponse r);

    abstract void handleInterleavedData(RtspRequest r);

    abstract void handleNonOkResponse(RtspSessionEvent.ReceivedResponse e);

    /**
     * Looks for the base URI
     * <p>
     * https://tools.ietf.org/html/rfc2326#appendix-C.1.1
     *
     * @param res RtspResponse
     * @return Base URI
     */
    Uri extractBaseUri(RtspRequest req, RtspResponse res) throws URISyntaxException {
        Uri baseUri = null;

        // 1. The RTSP Content-Base field
        String contentBase = res.getContentBase();
        if (contentBase != null) baseUri = Uri.parse(res.getContentBase());

        // 2. The RTSP Content-Location field
        String contentLocation = res.getContentLocation();
        if (baseUri == null && contentLocation != null) {
            baseUri = Uri.parse(res.getContentLocation());
        }

        // 3. The RTSP request URL
        if (baseUri == null) baseUri = req.getUri();

        return baseUri;
    }

    private RtpStream getStreamForChannel(int channel) {
        RtpStream match = null;
        for (RtpStream s : streams) {
            Pair<Integer, Integer> channels = s.getInterleavedRtpChannels();
            if (channels.first == channel && channels.second == channel) {
                match = s;
                break;
            }
        }
        return match;
    }

    class RtspInputListener implements Runnable {
        private static final String TAG = "RtspInputListener";

        @Override
        public void run() {
            Log.d(TAG, "Starting RtspInputListener");
            while (!Thread.interrupted()) {
                try {
                    // Read Rtsp RtspResponse from socket
                    Rtsp r = Rtsp.parseRtspInput(input);
                    if (r == null) {
                        break;
                    } else if (r instanceof RtspRequest) {
                        Log.d(TAG, "RECEIVED RTSP REQUEST:\n" + r.toString());

                        RtspRequest request = (RtspRequest) r;

                        eventBus.post(new RtspSessionEvent.ReceivedRequest(request));
                    } else if (r instanceof RtspResponse) {
                        Log.d(TAG, "RECEIVED RTSP RESPONSE:\n" + r.toString());

                        RtspResponse response = (RtspResponse) r;

                        // Match this response to a request
                        int cseq = response.getCseq();
                        RtspSessionEvent.SendRequest sendRequest = pastRequests.get(cseq);
                        RtspRequest matchedRequest = sendRequest.rtspRequest;
                        RtpStream matchedStream = sendRequest.stream;

                        eventBus.post(new RtspSessionEvent.ReceivedResponse(matchedRequest, response, matchedStream));
                    } else if (r instanceof RtspInterleavedData) {
                        Log.d(TAG, "RECEIVED RTSP INTERLEAVED DATA");

                        RtspInterleavedData rtspInterleavedData = (RtspInterleavedData) r;

                        // Determine if RTP or RTCP by the channel
                        RtpStream stream = getStreamForChannel(rtspInterleavedData.channel);
                        RtpStream.RtpPacketType type = stream.getTypeByChannel(rtspInterleavedData.channel);
                        switch (type) {
                            case RTP:
                                stream.getStreamEventBus().post(new RtspSessionEvent.RtpPacketReceived(rtspInterleavedData.data));
                                break;
                            case RTCP:
                                stream.getStreamEventBus().post(new RtspSessionEvent.RtcpPacketReceived(rtspInterleavedData.data));
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
