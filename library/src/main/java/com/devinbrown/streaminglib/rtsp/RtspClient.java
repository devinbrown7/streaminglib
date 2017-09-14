package com.devinbrown.streaminglib.rtsp;

import com.devinbrown.streaminglib.RtspClientStreamEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RtspClient {
    private static final String TAG = "RtspClient";
    private static RtspClient sharedInstance;

    private List<RtspClientSession> sessions = new ArrayList<>();

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
            sessions.add(new RtspClientSession(this, event));
        } catch (IOException e) {
            event.eventBus.post(new RtspClientStreamEvent.ConnectionError(e));
        }
    }
}
