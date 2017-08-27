package com.devinbrown.streaminglib.rtsp;

import android.util.Log;

import com.devinbrown.streaminglib.events.RtspServerEvent;

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

    private Thread mMainThread;

    private List<RtspServerSession> sessions = new ArrayList<>();

    private RtspServer(int port) {
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
        if (sharedInstance == null) {
            sharedInstance = new RtspServer(DEFAULT_PORT);
        }
        return sharedInstance;
    }

    /**
     * Starts the listener in a thread
     */
    public void start() {
        mMainThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    listen();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        mMainThread.start();
    }

    /**
     * Listen for client connections
     *
     * @throws IOException server socket cannot be created on port
     */
    private void listen() throws IOException {
        int serverPort = 8554;
        ServerSocket listenSocket = new ServerSocket();
        listenSocket.setReuseAddress(true);
        listenSocket.bind(new InetSocketAddress(serverPort));

        while (!Thread.interrupted()) {
            Log.d(TAG, "listening");

            // Blocking
            EventBus.getDefault().post(new RtspServerEvent.Connection(listenSocket.accept()));
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onMessageEvent(RtspServerEvent.Connection event) {
        Log.d(TAG, "onMessageEvent: EVENTBUS!");
        try {
            sessions.add(new RtspServerSession(event.socket));
        } catch (IOException e) {
            Log.e(TAG, "onMessageEvent: Failed to create RtspServerSession: " + e.getMessage());
        }
    }
}
