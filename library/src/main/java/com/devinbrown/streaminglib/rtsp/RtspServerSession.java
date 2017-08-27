package com.devinbrown.streaminglib.rtsp;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.Socket;

public class RtspServerSession extends RtspSession {
    private static final String TAG = "RtspServerSession";

    private EventBus mEventBus;

    class RtspHandlerThread extends HandlerThread {
        Handler mHandler;

        RtspHandlerThread(String name) {
            super(name);
        }

        void postMessage(String m) {
            rtspRequest(m);
        }

        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();
            mHandler = new Handler(getLooper());
        }
    }

    /**
     * Constructor
     */
    public RtspServerSession(final Socket s) throws IOException {
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "New server session");

                mSocket = s;
                try {
                    mInput = s.getInputStream();
                    mOutput = s.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        mThread.start();

        mHandlerThread = new RtspHandlerThread("RtspServerSession.HandlerThread");
        mHandlerThread.start();

        EventBus mEventBus = EventBus.builder().build();
    }

    /**
     * Handles an incoming RTSP message
     *
     * @param s
     */
    private void rtspRequest(String s) {
        Log.d(TAG, "rtspRequest: \n" + s);
    }
}
