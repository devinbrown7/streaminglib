package com.devinbrown.streaminglib.rtsp;

import android.media.MediaFormat;
import android.util.Log;

import com.devinbrown.streaminglib.events.RtspClientEvent;
import com.devinbrown.streaminglib.events.RtspClientStreamEvent;
import com.devinbrown.streaminglib.media.MediaFormatHelper;
import com.devinbrown.streaminglib.sdp.SessionDescription;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.net.Socket;
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
    public void onMessageEvent(RtspClientStreamEvent.ConnectionRequest event) {
        try {
            Session s = new Session(event);
            sessions.add(s);
            event.eventBus.post(new RtspClientEvent.Connected());
        } catch (IOException e) {
            event.eventBus.post(new RtspClientEvent.ConnectionError(e));
        }
    }

    public class Session extends RtspSession {
        private static final String TAG = "RtspClientSession";

        private EventBus eventBus;
        private URI uri;
        private int cSeq;
        private List<Rtsp.Method> supportedMethods;

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
        }

        // Public methods

        /**
         * Gets available streams from the RTSP server (RTSP DESCRIBE)
         */
        public void getAvailableMethods() {
            // Send describe
            RtspRequest r = RtspRequest.buildOptionsRequest(cSeq, uri);
            Log.d(TAG, "getAvailableMethods: REQUEST: \n" + r.toString());
            eventBus.post(new RtspClientEvent.Request(r));
        }

        /**
         * Gets available streams from the RTSP server (RTSP DESCRIBE)
         */
        public void getAvailableStreams() {
            // Send describe
            RtspRequest r = RtspRequest.buildDescribeRequest(cSeq, uri);
            Log.d(TAG, "getAvailableStreams: REQUEST: \n" + r.toString());
            eventBus.post(new RtspClientEvent.Request(r));
        }

        // EventBus event handlers

        @Subscribe(threadMode = ThreadMode.ASYNC)
        public void onMessageEvent(RtspClientEvent.Request event) {
            // Send Rtsp Client RtspRequest
            Log.d(TAG, "onMessageEvent: Send Rtsp Client RtspRequest");

            try {
                // Send Rtsp Message
                sendRtspMessage(event.rtspRequest);

                // Read Rtsp RtspResponse from socket
                RtspResponse r = RtspResponse.parseResponse(mInput);

                // Post RtspResponse
                eventBus.post(new RtspClientEvent.Response(event.rtspRequest, r));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Subscribe(threadMode = ThreadMode.ASYNC)
        public void onMessageEvent(RtspClientEvent.Response event) {
            // Handle Rtsp Server RtspResponse
            Log.d(TAG, "onMessageEvent: Handle Rtsp Server RtspResponse:\n" + event.rtspResponse);

            RtspStatus status = event.rtspResponse.getStatus();

            if (status != RtspStatus.OK) {
                handleNonOkResponse();
            } else {
                // Handle rtspResponse based on what method it was responding to
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

                        break;
                    case PLAY:

                        break;
                    case PAUSE:

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

        @Subscribe(threadMode = ThreadMode.ASYNC)
        public void handleEvent(RtspClientEvent.Connected event) {
            Log.d(TAG, "connected: RTSP CLIENT: Connected to server");

            getAvailableMethods();
        }

        @Subscribe(threadMode = ThreadMode.ASYNC)
        public void handleEvent(RtspClientEvent.UpdatedMethods event) {
            Log.d(TAG, "connected: RTSP CLIENT: Update methods");

            getAvailableStreams();
        }

        // RTSP Response Handlers

        private void handleOptionsResponse(RtspResponse r) {
            Log.d(TAG, "handleOptionsResponse: ");

            List<Rtsp.Method> methods = new ArrayList<>();
            String[] optionsArray = r.getOptions();
            if (optionsArray != null && optionsArray.length >= 1) {
                for (String s : optionsArray) {
                    methods.add(Rtsp.Method.valueOf(s));
                }
            }
            supportedMethods = methods;
            eventBus.post(new RtspClientEvent.UpdatedMethods());
        }

        private void handleDescribeResponse(RtspResponse r) {
            Log.d(TAG, "handleDescribeResponse: ");

            SessionDescription sd = SessionDescription.fromString(r.body);

            // TODO: Extract the stream info from the SDP
            MediaFormat[] formats = MediaFormatHelper.parseSdp(sd);


            // TODO: Add stream info object to this event
            eventBus.post(new RtspClientEvent.StreamAvailable(formats));
        }

        private void handleNonOkResponse() {
            eventBus.post(new RtspClientEvent.StreamNotFound());
        }
    }
}
