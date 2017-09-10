package com.devinbrown.streaminglib.rtsp;

import android.util.Log;

import com.devinbrown.streaminglib.RtspServerStreamEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class RtspServer {
    private static final String TAG = "RtspServer";
    private static int DEFAULT_PORT = 8554;

    private static RtspServer sharedInstance;
    private int port;

    private Thread mainThread;

    private List<RtspInputStream> inputStreams = new ArrayList<>();
    private List<RtspServerSession> sessions = new ArrayList<>();

    private int nextTrackId = 0;

    private RtspServer(int port) {
        this.port = port;
        EventBus.getDefault().register(this);
    }

    /**
     * Used to create additional RTSP Server instance
     *
     * @return a new RtspServer instance
     */
    public static RtspServer newServer(int port) {
        RtspServer r = new RtspServer(port);
        r.port = port;

        return r;
    }

    /**
     * Get the default RtspServer instance
     *
     * @return the default RtspServer instance
     */
    public static RtspServer getDefault() {
        if (sharedInstance == null) sharedInstance = new RtspServer(DEFAULT_PORT);
        return sharedInstance;
    }

    /**
     * Starts the listener in a thread
     */
    public void start() {
        mainThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    listen();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        mainThread.start();
    }

    /**
     * Listen for client connections
     *
     * @throws IOException server socket cannot be created on port
     */
    private void listen() throws IOException {
        try {
            Log.d(TAG, "listen: Create server socket");
            ServerSocket rtspServerSocket = new ServerSocket();
            rtspServerSocket.setReuseAddress(true);
            rtspServerSocket.bind(new InetSocketAddress(port));

            while (!Thread.interrupted()) {
                Log.d(TAG, "listening");

                // Blocking
                EventBus.getDefault().post(new RtspServerEvent.Connection(rtspServerSocket.accept()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<RtspInputStream> getInputStreams() {
        return inputStreams;
    }

    public RtspInputStream getRtspServerInputStreamForControl(String control) {
        RtspInputStream match = null;
        for (RtspInputStream input : inputStreams) {
            if (input.getControl().equalsIgnoreCase(control)) match = input;
        }
        return match;
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void handleEvent(RtspServerEvent.Connection event) {
        Log.d(TAG, "handleEvent: RtspServerEvent.Connection");
        try {
            sessions.add(new RtspServerSession(this, event.socket));
        } catch (IOException e) {
            Log.e(TAG, "handleEvent: Failed to create RtspServerSession: " + e.getMessage());
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void handleEvent(RtspServerStreamEvent.StreamAvailable event) {
        Log.d(TAG, "handleEvent: RtspServerStreamEvent.StreamAvailable");
        RtspInputStream input = event.getRtspInputStream();
        input.setControl("trackID=" + nextTrackId++);
        inputStreams.add(input);
    }
}
